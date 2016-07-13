package org.zanata.sync.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.criteria.Predicate;
import javax.websocket.Session;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.dao.JobStatusDAO;
import org.zanata.sync.dao.Repository;
import org.zanata.sync.dto.JobRunStatus;
import org.zanata.sync.events.JobProgressEvent;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.events.JobStartedEvent;
import org.zanata.sync.events.ResourceReadyEvent;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.dto.JobSummary;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.dto.WorkSummary;
import org.zanata.sync.quartz.CronTrigger;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.util.JSONObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {
    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private PluginsService pluginsServiceImpl;

    @Inject
    private Repository<SyncWorkConfig, Long> syncWorkConfigRepository;

    @Inject
    private JobStatusDAO jobStatusRepository;

    @Inject
    private CronTrigger cronTrigger;

    private Cache<JobKey, String> runningJobs =
            CacheBuilder.newBuilder().build();

    private Map<String, Session> webSocketSessions =
            Collections.synchronizedMap(
                    Maps.newHashMap());

    @Inject
    private JSONObjectMapper objectMapper;

    public void onStartUp(@Observes ResourceReadyEvent resourceReadyEvent) {
        log.info("=====================================================");
        log.info("=====================================================");
        log.info("=================Zanata Sync starts==================");
        log.info("=====================================================");
        log.info("== build version: {}", appConfiguration.getBuildVersion());
        log.info("== build revision: {}", appConfiguration.getBuildInfo());
        log.info("== fields to encrypt: {}",
                appConfiguration.getFieldsNeedEncryption());
        log.info("=====================================================");
        log.info("=====================================================");

        pluginsServiceImpl.init();

        log.info("Initialising jobs...");

        try {
            List<SyncWorkConfig> syncWorkConfigs =
                    syncWorkConfigRepository.getAll();
            for (SyncWorkConfig syncWorkConfig : syncWorkConfigs) {
                scheduleWork(syncWorkConfig);
            }
            log.info("Initialised {} jobs", syncWorkConfigs.size());
        } catch (SchedulerException e) {
            throw Throwables.propagate(e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        log.warn("=======================================");
        log.warn("======= application shutting down =====");
        log.warn("=======================================");
    }

    public void onJobProgressUpdate(@Observes JobProgressEvent event) {
        log.info(event.toString());

        Optional<SyncWorkConfig> workConfig =
                syncWorkConfigRepository.load(event.getConfigId());
        if (workConfig.isPresent()) {
            Optional<JobStatus> jobStatus = jobStatusRepository
                    .updateJobStatus(event.getFiringId(), null,
                            event.getNextFireTime(),
                            JobStatusType.RUNNING);
            fireWebSocketEvent(jobStatus, event.getConfigId());
        }
    }

    public void onJobStarted(@Observes JobStartedEvent event) {
        log.info(event.toString());

        runningJobs.put(event.getJobKey(), event.getFiringId());

        Optional<SyncWorkConfig> workConfig =
                syncWorkConfigRepository.load(event.getConfigId());
        if (workConfig.isPresent()) {

            JobStatus status = JobStatus
                    .started(event.getFiringId(), workConfig.get(),
                            event.getJobType(),
                            event.getStartTime(), null);

            jobStatusRepository.saveJobStatus(status);
        }
    }

    public void onJobCompleted(@Observes JobRunCompletedEvent event)
            throws JobNotFoundException, SchedulerException {
        JobKey jobKey = event.getJobType().toJobKey(event.getConfigId());
        runningJobs.invalidate(jobKey);

        Optional<SyncWorkConfig> syncWorkConfigOpt =
                syncWorkConfigRepository.load(event.getConfigId());
        if (syncWorkConfigOpt.isPresent()) {
            SyncWorkConfig syncWorkConfig = syncWorkConfigOpt.get();
            log.info("Job: " + event.getJobType() + "-" +
                    syncWorkConfig.getName() + " is completed.");

            Date endTime = event.getEndTime();
            Date nextFireTime =
                    getNextFireTime(syncWorkConfig.getId(), event.getJobType())
                            .orElse(null);
            Optional<JobStatus> jobStatus = jobStatusRepository
                    .updateJobStatus(event.getJobFireId(), endTime,
                            nextFireTime, event.getJobStatusType());
            fireWebSocketEvent(jobStatus, event.getConfigId());
        }
    }

    private void fireWebSocketEvent(Optional<JobStatus> jobStatus,
            Long configId) {
        if (jobStatus.isPresent()) {
            JobStatus status = jobStatus.get();
            JobRunStatus jobRunStatus = JobRunStatus
                    .fromEntity(status, configId,
                            status.getJobType());
            webSocketSessions.forEach((id, session) -> {
                if (session.isOpen()) {
                    String asJson = objectMapper.toJSON(jobRunStatus);
                    log.info("----- sending result for {}", jobRunStatus);
                    session.getAsyncRemote().sendText(
                            asJson, (sendResult) -> {
                                log.info("---- websocket send result ok:{} for status: {}", sendResult.isOK(), jobRunStatus);
                            });
                }
            });
        }
    }

    private Optional<Date> getNextFireTime(Long workId, JobType jobType) {
        Optional<Trigger> trigger = cronTrigger
                .getTriggerFor(workId, jobType);
        if (trigger.isPresent()) {
            return Optional.ofNullable(trigger.get().getNextFireTime());
        }
        return Optional.empty();
    }

    @Override
    public JobStatus getLatestJobStatus(Long id, JobType type) {
        Optional<SyncWorkConfig> syncWorkConfigOpt =
                syncWorkConfigRepository.load(id);
        if (syncWorkConfigOpt.isPresent()) {
            SyncWorkConfig syncWorkConfig = syncWorkConfigOpt.get();
            return jobStatusRepository.getLatestJobStatus(syncWorkConfig, type);
        }
        return JobStatus.EMPTY;
    }

    @Override
    public List<JobSummary> getJobs() throws SchedulerException {
        List<JobDetail> runningJobs = cronTrigger.getJobs();
        return runningJobs.stream().map(this::convertToJobSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkSummary> getAllWorkSummary() throws SchedulerException {
        List<WorkSummary> results = getAllWork().stream()
                .map(config -> WorkSummary.toWorkSummary(config,
                        getLatestJobStatus(config.getId(), JobType.REPO_SYNC),
                        getLatestJobStatus(config.getId(),
                                JobType.SERVER_SYNC)))
                .collect(Collectors.toList());
        return results;
    }

    @Override
    public void scheduleWork(SyncWorkConfig syncWorkConfig)
            throws SchedulerException {
        cronTrigger.scheduleMonitorForRepoSync(syncWorkConfig);
        cronTrigger.scheduleMonitorForServerSync(syncWorkConfig);
    }

    @Override
    public void rescheduleWork(SyncWorkConfig syncWorkConfig)
            throws SchedulerException {

        cronTrigger.deleteAndReschedule(syncWorkConfig, JobType.REPO_SYNC);
        cronTrigger.deleteAndReschedule(syncWorkConfig, JobType.SERVER_SYNC);
    }

    @Override
    public void cancelRunningJob(Long id, JobType type)
            throws UnableToInterruptJobException, JobNotFoundException {
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(id);
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(id.toString());
        }
        cronTrigger.cancelRunningJob(id, type);
    }

    @Override
    public void deleteJob(Long id, JobType type) throws SchedulerException {
        boolean foundAndDeleted = cronTrigger.deleteJob(type.toJobKey(id));
        if (foundAndDeleted) {
            log.info("config id {} and job type {} found and deleted", id, type);
        }
    }

    @Override
    public void disableJob(Long id, JobType type) throws SchedulerException {
        cronTrigger.disableJob(id, type);
    }

    @Override
    public void enableJob(Long id, JobType type) throws SchedulerException {
        cronTrigger.enableJob(id, type);
    }

    @Override
    public boolean triggerJob(Long id, JobType type)
            throws JobNotFoundException, SchedulerException {
        String firingId = runningJobs.getIfPresent(type.toJobKey(id));
        if (firingId != null) {
            log.info("job is already running. firing id: {}", firingId);
            return false;
        }
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(id);
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(id.toString());
        }
        cronTrigger.triggerJob(id, type);
        return true;
    }

    @Override
    public SyncWorkConfig getWorkById(Long id) throws WorkNotFoundException {
        Optional<SyncWorkConfig> syncWorkConfig =
                syncWorkConfigRepository.load(id);
        if (!syncWorkConfig.isPresent()) {
            throw new WorkNotFoundException("id not found:" + id);
        }
        return syncWorkConfig.get();
    }

    @Override
    public List<WorkSummary> getWorkFor(String username) {
        List<SyncWorkConfig> syncWorkConfigs = syncWorkConfigRepository
                .findByCriteria((cb, root) -> new Predicate[]{
                        cb.equal(root.get("zanataUsername"), username) });
        return syncWorkConfigs.stream()
                .map(config -> WorkSummary.toWorkSummary(config,
                        getLatestJobStatus(config.getId(), JobType.REPO_SYNC),
                        getLatestJobStatus(config.getId(),
                                JobType.SERVER_SYNC)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<JobStatus> getJobStatusByFiringId(String jobFiringId) {
        return jobStatusRepository.findById(jobFiringId);
    }

    @Override
    public List<JobStatus> getAllJobStatus(Long configId)
            throws WorkNotFoundException {
        Optional<SyncWorkConfig> config = syncWorkConfigRepository.load(configId);
        if (config.isPresent()) {
            return jobStatusRepository.getJobStatusList(config.get());
        }
        throw new WorkNotFoundException("id not found:" + configId);
    }

    @Override
    public void addWebSocketSession(Session session) {
        webSocketSessions.put(session.getId(), session);
    }

    @Override
    public void removeWebSocketSession(Session session) {
        webSocketSessions.remove(session.getId());
    }

    @Override
    public void publishEvent(JobRunStatus status) {
        webSocketSessions.forEach((id, session) -> {
            if (session.isOpen()) {
                String asJson = objectMapper.toJSON(status);
                session.getAsyncRemote().sendText(
                        asJson);
            }
        });
    }

    @Override
    public List<SyncWorkConfig> getAllWork() throws SchedulerException {
        return syncWorkConfigRepository.getAll();
    }

    private JobSummary convertToJobSummary(JobDetail jobDetail) {
        if (jobDetail != null) {
            SyncWorkConfig syncWorkConfig =
                    syncWorkConfigRepository
                            .load(new Long(jobDetail.getKey().getGroup()))
                            .get();
            JobType type = JobType.valueOf(jobDetail.getKey().getName());

            JobStatus status = getLatestJobStatus(syncWorkConfig.getId(), type);

            return new JobSummary(jobDetail.getKey().toString(),
                    syncWorkConfig.getId(), syncWorkConfig.getName(),
                    syncWorkConfig.getDescription(), type,
                    JobRunStatus.fromEntity(status, syncWorkConfig.getId(), type));
        }
        return null;
    }

}
