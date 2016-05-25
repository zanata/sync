package org.zanata.sync.quartz;

import java.util.Objects;

import org.zanata.sync.model.JobType;
import com.google.common.base.MoreObjects;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class RunningJobKey {
    private final Long workId;
    private final JobType jobType;

    public RunningJobKey(Long workId, JobType jobType) {
        this.workId = workId;
        this.jobType = jobType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningJobKey that = (RunningJobKey) o;
        return Objects.equals(workId, that.workId) &&
            jobType == that.jobType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, jobType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("workId", workId)
            .add("jobType", jobType)
            .toString();
    }
}
