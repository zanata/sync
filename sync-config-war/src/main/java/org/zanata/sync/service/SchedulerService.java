package org.zanata.sync.service;

import java.util.List;
import java.util.Optional;

import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.dto.JobSummary;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.dto.WorkSummary;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface SchedulerService {
    JobStatus getLatestJobStatus(Long id, JobType type)
            throws SchedulerException, JobNotFoundException;

    List<JobSummary> getJobs() throws SchedulerException;

    List<WorkSummary> getAllWorkSummary() throws SchedulerException;

    List<SyncWorkConfig> getAllWork() throws SchedulerException;

    void scheduleWork(SyncWorkConfig syncWorkConfig)
            throws SchedulerException;

    void rescheduleWork(SyncWorkConfig syncWorkConfig)
            throws SchedulerException;

    void cancelRunningJob(Long id, JobType type)
            throws UnableToInterruptJobException, JobNotFoundException;

    void deleteJob(Long id, JobType type)
            throws SchedulerException;

    void disableJob(Long id, JobType type) throws SchedulerException;

    void enableJob(Long id, JobType type) throws SchedulerException;

    void triggerJob(Long id, JobType type)
            throws JobNotFoundException, SchedulerException;

    SyncWorkConfig getWorkById(Long id) throws WorkNotFoundException;

    List<WorkSummary> getWorkFor(String username);

    Optional<JobStatus> getJobStatusByFiringId(String jobFiringId);

    List<JobStatus> getAllJobStatus(Long configId) throws WorkNotFoundException;
}
