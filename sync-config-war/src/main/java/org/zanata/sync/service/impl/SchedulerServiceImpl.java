package org.zanata.sync.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.dao.JobStatusDAO;
import org.zanata.sync.dao.Repository;
import org.zanata.sync.events.JobProgressEvent;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.events.ResourceReadyEvent;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.interceptor.WithRequestScope;
import org.zanata.sync.model.JobProgress;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusList;
import org.zanata.sync.model.JobSummary;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.model.WorkSummary;
import org.zanata.sync.quartz.CronTrigger;
import org.zanata.sync.quartz.RunningJobKey;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.util.WorkUtil;
import com.google.common.base.Throwables;
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

    private Map<RunningJobKey, JobProgress> progressMap =
        Collections.synchronizedMap(Maps.newHashMap());

    @WithRequestScope
    public void onStartUp(@Observes ResourceReadyEvent resourceReadyEvent) {
        log.info("=====================================================");
        log.info("=====================================================");
        log.info("=================Zanata Sync starts==================");
        log.info("=====================================================");
        log.info("== build: {}-{}",
                appConfiguration.getBuildVersion(),
                appConfiguration.getBuildInfo());
        log.info("== data directory: {}", appConfiguration.getDataPath());
        log.info("== repo directory: {}", appConfiguration.getRepoDir());
        log.info("== fields to encrypt: {}",
                appConfiguration.getFieldsNeedEncryption());
        log.info("== database path: {}", appConfiguration.getDBFilePath());
        log.info("=====================================================");
        log.info("=====================================================");

        pluginsServiceImpl.init();

        log.info("Initialising jobs...");

        try {
            List<SyncWorkConfig> syncWorkConfigs = syncWorkConfigRepository.getAll();
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
        Logger jobTypeLog =
                LoggerFactory.getLogger(event.getJobType().name());
        jobTypeLog.info(event.toString());
        log.info(event.toString());
        JobProgress progress = new JobProgress(event.getCompletePercent(),
                event.getDescription(), event.getJobStatusType());
        progressMap
            .put(new RunningJobKey(event.getId(), event.getJobType()),
                progress);
    }

    @WithRequestScope
    public void onJobCompleted(@Observes JobRunCompletedEvent event)
        throws JobNotFoundException, SchedulerException {
        progressMap.remove(new RunningJobKey(event.getId(), event.getJobType()));
        Optional<SyncWorkConfig> syncWorkConfigOpt =
                syncWorkConfigRepository.load(event.getId());
        if (syncWorkConfigOpt.isPresent()) {
            SyncWorkConfig syncWorkConfig = syncWorkConfigOpt.get();
            log.debug("Job: " + event.getJobType() + "-" +
                            syncWorkConfig.getName() + " is completed.");

            JobStatus jobStatus = getStatus(event);
            jobStatusRepository.saveJobStatus(syncWorkConfig,
                    event.getJobType(), jobStatus);
        }
    }

    @WithRequestScope
    @Override
    public JobStatus getLatestJobStatus(Long id, JobType type) {
        Optional<SyncWorkConfig> syncWorkConfigOpt =
                syncWorkConfigRepository.load(id);
        if (syncWorkConfigOpt.isPresent()) {
            SyncWorkConfig syncWorkConfig = syncWorkConfigOpt.get();

            JobStatusList statusList =
                    jobStatusRepository.getJobStatusList(syncWorkConfig, type);

            if (statusList != null && !statusList.isEmpty()) {
                JobStatus jobStatus = statusList.get(0);
                setJobProgress(jobStatus, id, type);
                return jobStatus;
            }
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
            .map(config -> WorkUtil.toWorkSummary(config,
                getLatestJobStatus(config.getId(), JobType.REPO_SYNC),
                getLatestJobStatus(config.getId(), JobType.SERVER_SYNC)))
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
    @WithRequestScope
    public void cancelRunningJob(Long id, JobType type)
        throws UnableToInterruptJobException, JobNotFoundException {
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(id);
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(id.toString());
        }
        cronTrigger.cancelRunningJob(id, type);
    }

    @WithRequestScope
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

    @WithRequestScope
    @Override
    public void triggerJob(Long id, JobType type)
        throws JobNotFoundException, SchedulerException {
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(id);
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(id.toString());
        }
        cronTrigger.triggerJob(id, type);
    }

    @WithRequestScope
    @Override
    public SyncWorkConfig getWork(String id) throws WorkNotFoundException {
        Optional<SyncWorkConfig> syncWorkConfig =
                syncWorkConfigRepository.load(new Long(id));
        if(!syncWorkConfig.isPresent()) {
            throw new WorkNotFoundException(id);
        }
        return syncWorkConfig.get();
    }

    @Override
    public WorkSummary getWorkSummary(String id) throws WorkNotFoundException {
        SyncWorkConfig syncWorkConfig = getWork(id);
        return WorkUtil.toWorkSummary(syncWorkConfig,
                getLatestJobStatus(syncWorkConfig.getId(), JobType.REPO_SYNC),
                getLatestJobStatus(syncWorkConfig.getId(),
                        JobType.SERVER_SYNC));
    }

    @WithRequestScope
    @Override
    public List<SyncWorkConfig> getAllWork() throws SchedulerException {
        return syncWorkConfigRepository.getAll();
    }

    private JobStatus getStatus(JobRunCompletedEvent event)
        throws SchedulerException, JobNotFoundException {
        Optional<SyncWorkConfig> workConfigOptional =
                syncWorkConfigRepository.load(event.getId());
        if (!workConfigOptional.isPresent()) {
            throw new JobNotFoundException(event.getId().toString());
        }
        return cronTrigger.getTriggerStatus(event.getId(), event);
    }

    private JobSummary convertToJobSummary(JobDetail jobDetail) {
        if (jobDetail != null) {
            SyncWorkConfig syncWorkConfig =
                    syncWorkConfigRepository
                            .load(new Long(jobDetail.getKey().getGroup())).get();
            JobType type = JobType.valueOf(jobDetail.getKey().getName());

            JobStatus status = getLatestJobStatus(syncWorkConfig.getId(), type);
            setJobProgress(status, syncWorkConfig.getId(), type);

            return new JobSummary(jobDetail.getKey().toString(),
                    syncWorkConfig.getId().toString(), syncWorkConfig.getName(),
                    syncWorkConfig.getDescription(), type, status);
        }
        return new JobSummary();
    }

    private void setJobProgress(JobStatus jobStatus, long id, JobType jobType) {
        RunningJobKey key = new RunningJobKey(id, jobType);
        jobStatus.updateCurrentProgress(progressMap.get(key));
    }
}
