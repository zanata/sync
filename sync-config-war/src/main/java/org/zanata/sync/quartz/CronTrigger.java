/**
 *
 */
package org.zanata.sync.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.plugin.TranslationServerExecutor;
import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.exception.UnableLoadPluginException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.plugin.zanata.Plugin;
import org.zanata.sync.service.PluginsService;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
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
    private JobConfigListener triggerListener;

    @PostConstruct
    public void start() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        if (scheduler.getListenerManager().getJobListeners().isEmpty()) {
            scheduler.getListenerManager()
                    .addTriggerListener(triggerListener);
        }
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

        if(StringUtils.isEmpty(cronExp) || !isEnabled) {
            builder.storeDurably();
        }
        return builder.build();
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
        try {
            String cronExp;
            Class<SyncJob> jobClass = SyncJob.class;
            if (type.equals(JobType.REPO_SYNC)
                    && !Strings.isNullOrEmpty(syncWorkConfig.getSyncToRepoCron())) {
                cronExp = syncWorkConfig.getSyncToRepoCron();
            } else if (type.equals(JobType.SERVER_SYNC)
                    && !Strings.isNullOrEmpty(syncWorkConfig.getSyncToZanataCron())) {
                cronExp = syncWorkConfig.getSyncToZanataCron();
            } else {
                // impossible
                return;
            }

            JobDetail jobDetail =
                buildJobDetail(syncWorkConfig, jobKey, jobClass, cronExp,
                    isEnabled);

            jobDetail.getJobDataMap().put("value", syncWorkConfig);
            jobDetail.getJobDataMap().put("jobType", type);

            jobDetail.getJobDataMap()
                    .put(RepoExecutor.class.getSimpleName(), pluginsService
                            .getNewSourceRepoPlugin(
                                    syncWorkConfig.getSrcRepoPluginName(),
                                    syncWorkConfig.getSrcRepoPluginConfig()));

            jobDetail.getJobDataMap()
                .put(TranslationServerExecutor.class.getSimpleName(),
                        new Plugin(syncWorkConfig.getTransServerPluginConfig())
                    );

            if (scheduler.getListenerManager().getJobListeners().isEmpty()) {
                scheduler.getListenerManager()
                        .addTriggerListener(triggerListener);
            }

            if (!StringUtils.isEmpty(cronExp) && isEnabled) {
                Trigger trigger = buildTrigger(cronExp, syncWorkConfig.getId(),
                    type, isEnabled);
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                scheduler.addJob(jobDetail, false);
            }
        } catch (UnableLoadPluginException e) {
            log.error("Unable to load plugin", e.getMessage());
        }
    }

    public JobStatus getTriggerStatus(Long id,
        JobRunCompletedEvent event) throws SchedulerException {
        JobKey key = event.getJobType().toJobKey(id);

        if (scheduler.checkExists(key)) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(key);

            if (!triggers.isEmpty()) {
                Trigger trigger = triggers.get(0);
                Date endTime = event.getCompletedTime();
                Trigger.TriggerState state =
                        scheduler.getTriggerState(trigger.getKey());

                return new JobStatus(event.getJobStatusType(),
                        trigger.getPreviousFireTime(), endTime,
                        trigger.getNextFireTime());
            }
        }
        return JobStatus.EMPTY;
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

    public void deleteJob(Long id, JobType type) throws SchedulerException {
        JobKey jobKey = type.toJobKey(id);
        scheduler.deleteJob(jobKey);
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
        deleteJob(syncWorkConfig.getId(), type);
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
        if (!StringUtils.isEmpty(cronExp) && isEnabled) {
            builder.withSchedule(
                CronScheduleBuilder.cronSchedule(cronExp));
        }
        return builder.build();
    }

}
