package org.zanata.sync.quartz;

import javax.ws.rs.client.Client;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.zanata.sync.events.JobProgressEvent;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.job.JobExecutor;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class SyncJob implements InterruptableJob {

    protected SyncWorkConfig syncWorkConfig;
    private boolean interrupted = false;
    private JobType jobType;

    @Override
    public final void execute(JobExecutionContext context)
        throws JobExecutionException {
        boolean hasError = false;
        try {
            syncWorkConfig =
                    (SyncWorkConfig) context.getJobDetail().getJobDataMap()
                            .get(CronTrigger.SYNC_WORK_CONFIG_KEY);

            jobType = (JobType) context.getJobDetail().getJobDataMap().get(CronTrigger.JOB_TYPE_KEY);

            Client client = (Client) context.getJobDetail().getJobDataMap().get(CronTrigger.REST_CLIENT_KEY);

            new JobExecutor(client).executeJob(syncWorkConfig.getId().toString(), syncWorkConfig, jobType);

        } catch (Exception e) {
            log.error("error happened during job run", e);
            hasError = true;
        } finally {
            if(!interrupted) {
                JobRunCompletedEvent event;
                if (hasError) {
                    event = new JobRunCompletedEvent(syncWorkConfig.getId(),
                        context.getJobRunTime(),
                        context.getFireTime(),
                        jobType, JobStatusType.ERROR);
                } else {
                    event = new JobRunCompletedEvent(syncWorkConfig.getId(),
                        context.getJobRunTime(),
                        context.getFireTime(),
                        jobType, JobStatusType.COMPLETE);
                }
                BeanManagerProvider.getInstance().getBeanManager().fireEvent(event);
            }
        }
    }

    @Override
    public final void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
        Thread.currentThread().interrupt();
        updateProgress(syncWorkConfig.getId(), 0, "job interrupted",
            JobStatusType.INTERRUPTED);
    }

    private void updateProgress(Long id, double completePercent,
            String description, JobStatusType jobStatusType) {
        JobProgressEvent event =
            new JobProgressEvent(id, jobType, completePercent,
                description, jobStatusType);
        BeanManagerProvider.getInstance().getBeanManager().fireEvent(event);
    }

}
