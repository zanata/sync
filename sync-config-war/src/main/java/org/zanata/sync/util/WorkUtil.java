package org.zanata.sync.util;

import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobSummary;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.model.WorkSummary;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class WorkUtil {

    public static final WorkSummary toWorkSummary(
        SyncWorkConfig syncWorkConfig, JobStatus syncToRepoJobStatus, JobStatus syncToServerJobStatus) {
        if(syncWorkConfig == null) {
            return new WorkSummary();
        }
        JobSummary syncToRepoJob =
            new JobSummary("", syncWorkConfig.getName(),
                syncWorkConfig.getId().toString(),
                syncWorkConfig.getDescription(),
                JobType.REPO_SYNC,
                syncToRepoJobStatus);

        JobSummary syncToServerJob =
            new JobSummary("", syncWorkConfig.getName(),
                syncWorkConfig.getId().toString(),
                syncWorkConfig.getDescription(),
                JobType.SERVER_SYNC,
                syncToServerJobStatus);

        return new WorkSummary(syncWorkConfig.getId(),
            syncWorkConfig.getName(),
            syncWorkConfig.getDescription(),
            syncToRepoJob,
            syncToServerJob);
    }
}
