package org.zanata.sync.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.criteria.Predicate;

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
import org.zanata.sync.events.ResourceReadyEvent;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.dto.JobSummary;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.dto.WorkSummary;
import org.zanata.sync.quartz.CronTrigger;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.service.SchedulerService;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

    public void onStartUp(@Observes ResourceReadyEvent resourceReadyEvent) {
        log.info("=====================================================");
        log.info("=====================================================");
        log.info("=================Zanata Sync starts==================");
        log.info("=====================================================");
        log.info("== build: {}-{}",
                appConfiguration.getBuildVersion(),
                appConfiguration.getBuildInfo());
        log.info("== repo directory: {}", appConfiguration.getRepoDir());
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

    // TODO: fire websocket
    public void onJobProgressUpdate(@Observes JobProgressEvent event) {
        log.info(event.toString());

        runningJobs.put(event.getJobKey(), event.getFiringId());

        Optional<SyncWorkConfig> workConfig =
                syncWorkConfigRepository.load(event.getId());
        if (workConfig.isPresent()) {
            JobStatus status = new JobStatus(event.getFiringId(), workConfig.get(), event.getJobType(),
                    JobStatusType.RUNNING, null, null, null);

            jobStatusRepository.saveJobStatus(status);
        }
    }

    public void onJobCompleted(@Observes JobRunCompletedEvent event)
            throws JobNotFoundException, SchedulerException {
        runningJobs.invalidate(event.getJobType().toJobKey(event.getId()));

        Optional<SyncWorkConfig> syncWorkConfigOpt =
                syncWorkConfigRepository.load(event.getId());
        if (syncWorkConfigOpt.isPresent()) {
            SyncWorkConfig syncWorkConfig = syncWorkConfigOpt.get();
            log.debug("Job: " + event.getJobType() + "-" +
                    syncWorkConfig.getName() + " is completed.");

            Date completedTime = event.getCompletedTime();
            Date startTime = event.getStartTime();
            Optional<Trigger> trigger = cronTrigger
                    .getTriggerFor(event.getId(), event.getJobType());
            Date nextRunTime = null;
            if (trigger.isPresent()) {
                nextRunTime = trigger.get().getNextFireTime();
            }
            JobStatus jobStatus =
                    new JobStatus(event.getJobFireId(), syncWorkConfig, event.getJobType(),
                            event.getJobStatusType(), startTime, completedTime,
                            nextRunTime);
            log.info("for work id: {} saved job status: {}",
                    syncWorkConfig.getId(), jobStatus);
            jobStatusRepository.saveJobStatus(jobStatus);
        }
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
    public void deleteJob(Long id, JobType type)
            throws SchedulerException, JobNotFoundException {
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(id);
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(id.toString());
        }
        cronTrigger.deleteJob(id, type);
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
    public void triggerJob(Long id, JobType type)
            throws JobNotFoundException, SchedulerException {
        String firingId = runningJobs.getIfPresent(type.toJobKey(id));
        if (firingId != null) {
            log.info("job is already running. firing id: {}", firingId);
            return;
        }
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(id);
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(id.toString());
        }
        cronTrigger.triggerJob(id, type);
    }

    @Override
    public SyncWorkConfig getWork(String id) throws WorkNotFoundException {
        Optional<SyncWorkConfig> syncWorkConfig =
                syncWorkConfigRepository.load(new Long(id));
        if (!syncWorkConfig.isPresent()) {
            throw new WorkNotFoundException(id);
        }
        return syncWorkConfig.get();
    }

    @Override
    public WorkSummary getWorkSummary(String id) throws WorkNotFoundException {
        SyncWorkConfig syncWorkConfig = getWork(id);
        return WorkSummary.toWorkSummary(syncWorkConfig,
                getLatestJobStatus(syncWorkConfig.getId(), JobType.REPO_SYNC),
                getLatestJobStatus(syncWorkConfig.getId(),
                        JobType.SERVER_SYNC));
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
                    JobRunStatus.fromEntity(status));
        }
        return null;
    }

}
