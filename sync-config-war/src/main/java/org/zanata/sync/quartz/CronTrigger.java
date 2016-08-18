/**
 *
 */
package org.zanata.sync.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.service.PluginsService;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
@Slf4j
public class CronTrigger {

    private Scheduler scheduler;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private PluginsService pluginsService;

    @Inject
    private JobTriggerListener triggerListener;

    @Inject
    private SyncJobListener syncJobListener;

    @PostConstruct
    public void start() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        ListenerManager listenerManager = scheduler.getListenerManager();
        listenerManager.addJobListener(syncJobListener);
        listenerManager.addTriggerListener(triggerListener);
        scheduler.start();
    }

    public void scheduleMonitorForRepoSync(SyncWorkConfig syncWorkConfig)
            throws SchedulerException {
        scheduleMonitor(syncWorkConfig, JobType.REPO_SYNC);
    }

    public void scheduleMonitorForServerSync(SyncWorkConfig syncWorkConfig)
            throws SchedulerException {
        scheduleMonitor(syncWorkConfig, JobType.SERVER_SYNC);
    }

    private JobDetail buildJobDetail(SyncWorkConfig syncWorkConfig, JobKey key,
            Class<? extends Job> jobClass, String cronExp, boolean isEnabled) {
        JobBuilder builder = JobBuilder
                .newJob(jobClass)
                .withIdentity(key)
                .withDescription(syncWorkConfig.toString());

        // TODO pahuang revisit this (if cron expression is empty (manual job) or job is disabled, we store it durably. Maybe we only want this when job is enabled but manual?
        if (!shouldRunAsCronJob(cronExp, isEnabled)) {
            builder.storeDurably();
        }
        return builder.build();
    }

    private static boolean shouldRunAsCronJob(String cronExp,
            boolean isEnabled) {
        return !Strings.isNullOrEmpty(cronExp) && isEnabled;
    }

    private boolean isJobEnabled(SyncWorkConfig syncWorkConfig, JobType jobType) {
        if(jobType.equals(JobType.SERVER_SYNC)) {
            return syncWorkConfig.isSyncToServerEnabled();
        } else if(jobType.equals(JobType.REPO_SYNC)) {
            return syncWorkConfig.isSyncToRepoEnabled();
        }
        return false;
    }

    private void scheduleMonitor(
            SyncWorkConfig syncWorkConfig, JobType type)
                    throws SchedulerException {
        JobKey jobKey = type.toJobKey(syncWorkConfig.getId());
        boolean isEnabled = isJobEnabled(syncWorkConfig, type);

        if (scheduler.checkExists(jobKey)) {
            return;
        }
        String cronExp = null;
        Class<SyncJob> jobClass = SyncJob.class;
        if (type.equals(JobType.REPO_SYNC)
                && syncWorkConfig.getSyncToRepoCron() != null) {
            cronExp = syncWorkConfig.getSyncToRepoCron().getExpression();
        } else if (type.equals(JobType.SERVER_SYNC)
                && syncWorkConfig.getSyncToZanataCron() != null) {
            cronExp = syncWorkConfig.getSyncToZanataCron().getExpression();
        }

        JobDetail jobDetail =
            buildJobDetail(syncWorkConfig, jobKey, jobClass, cronExp,
                isEnabled);

        SyncJobDataMap syncJobDataMap = SyncJobDataMap.fromJobDetail(jobDetail);
        syncJobDataMap.storeConfigId(syncWorkConfig.getId()).storeJobType(type);

        if (scheduler.getListenerManager().getJobListeners().isEmpty()) {
            scheduler.getListenerManager()
                    .addTriggerListener(triggerListener);
        }

        if (shouldRunAsCronJob(cronExp, isEnabled)) {
            Trigger trigger = buildTrigger(cronExp, syncWorkConfig.getId(),
                type, isEnabled);
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.addJob(jobDetail, false);
        }
    }

    public Optional<Trigger> getTriggerFor(Long workId, JobType jobType) {
        JobKey jobKey = jobType.toJobKey(workId);
        try {
            List<? extends Trigger> triggers =
                    scheduler.getTriggersOfJob(jobKey);
            if (!triggers.isEmpty()) {
                return Optional.of(triggers.get(0));
            }
            return Optional.empty();
        } catch (SchedulerException e) {
            log.error("error getting triggers for job: {}", jobKey);
            return Optional.empty();
        }

    }

    public List<JobDetail> getJobs() throws SchedulerException {
        List<JobDetail> jobs = new ArrayList<>();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
            JobType jobType = JobType.valueOf(jobKey.getName());
            Long workId = new Long(jobKey.getGroup());
            JobDetail jobDetail =
                    scheduler.getJobDetail(jobType.toJobKey(workId));
            jobs.add(jobDetail);
        }
        return jobs;
    }

    public void cancelRunningJob(Long id, JobType type)
        throws UnableToInterruptJobException {
        JobKey jobKey = type.toJobKey(id);
        scheduler.interrupt(jobKey);
    }

    public boolean deleteJob(JobKey jobKey) throws SchedulerException {
        return scheduler.deleteJob(jobKey);
    }

    public void disableJob(Long id, JobType type) throws SchedulerException {
        JobKey jobKey = type.toJobKey(id);
        scheduler.pauseJob(jobKey);
    }

    public void enableJob(Long id, JobType type) throws SchedulerException {
        JobKey jobKey = type.toJobKey(id);
        scheduler.resumeJob(jobKey);
    }

    public void deleteAndReschedule(SyncWorkConfig syncWorkConfig, JobType type)
            throws SchedulerException {
        deleteJob(type.toJobKey(syncWorkConfig.getId()));
        scheduleMonitor(syncWorkConfig, type);
    }

    public void triggerJob(Long id, JobType type) throws SchedulerException {
        JobKey key = type.toJobKey(id);
        scheduler.triggerJob(key);
    }

    private Trigger buildTrigger(String cronExp,
        Long id, JobType type, boolean isEnabled) {
        TriggerBuilder builder = TriggerBuilder
            .newTrigger()
            .withIdentity(type.toTriggerKey(id));
        if (shouldRunAsCronJob(cronExp, isEnabled)) {
            builder.withSchedule(
                CronScheduleBuilder.cronSchedule(cronExp));
        }
        return builder.build();
    }

}
