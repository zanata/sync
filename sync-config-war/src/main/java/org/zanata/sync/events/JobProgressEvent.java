package org.zanata.sync.events;

import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@AllArgsConstructor
public class JobProgressEvent {
    private Long id;
    private JobType jobType;
    private double completePercent;
    private String description;
    private JobStatusType jobStatusType;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("jobType", jobType)
                .add("completePercent", completePercent)
                .add("description", description)
            .add("jobStatusType", jobStatusType)
            .toString();
    }
}
