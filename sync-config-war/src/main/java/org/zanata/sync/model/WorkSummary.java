package org.zanata.sync.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkSummary implements Serializable {
    private Long id;
    private String name;
    private String description;
    private JobSummary syncToRepoJob;
    private JobSummary syncToTransServerJob;

    public static WorkSummary toWorkSummary(
            SyncWorkConfig syncWorkConfig, JobStatus syncToRepoJobStatus,
            JobStatus syncToServerJobStatus) {

        JobSummary syncToRepoJob =
                new JobSummary(
                        JobType.REPO_SYNC.toJobKey(syncWorkConfig.getId())
                                .toString(), syncWorkConfig.getId(),
                        syncWorkConfig.getName(),
                        syncWorkConfig.getDescription(),
                        JobType.REPO_SYNC,
                        syncToRepoJobStatus);

        JobSummary syncToServerJob =
                new JobSummary(
                        JobType.SERVER_SYNC.toJobKey(syncWorkConfig.getId())
                                .toString(), syncWorkConfig.getId(),
                        syncWorkConfig.getName(),
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
