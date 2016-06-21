package org.zanata.sync.events;

import org.quartz.JobKey;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
public class JobProgressEvent {
    private String firingId;
    private Long id;
    private JobType jobType;
    private JobStatusType jobStatusType;
    private JobKey jobKey;

    public static JobProgressEvent running(String fireInstanceId,
            SyncWorkConfig workConfig,
            JobType jobType) {
        return new JobProgressEvent(fireInstanceId, workConfig.getId(), jobType,
                JobStatusType.RUNNING,jobType.toJobKey(workConfig.getId()));
    }

    public String getFiringId() {
        return firingId;
    }

    public Long getId() {
        return id;
    }

    public JobType getJobType() {
        return jobType;
    }

    public JobKey getJobKey() {
        return jobKey;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("firingId", firingId)
                .add("id", id)
                .add("jobType", jobType)
                .add("jobStatusType", jobStatusType)
                .add("jobKey", jobKey)
                .toString();
    }
}
