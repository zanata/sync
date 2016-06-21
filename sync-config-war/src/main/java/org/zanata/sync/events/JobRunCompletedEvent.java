package org.zanata.sync.events;

import java.util.Date;

import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.util.DateUtil;
import lombok.Getter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
public class JobRunCompletedEvent {
    private String jobFireId;
    private Long id;
    private Date startTime;
    private JobType jobType;
    private long runDuration;
    private JobStatusType jobStatusType;

    public JobRunCompletedEvent(String jobFireId, Long id,
            long runDuration, Date startTime, JobType jobType,
            JobStatusType jobStatusType) {
        this.jobFireId = jobFireId;
        this.id = id;
        this.runDuration = runDuration;
        this.startTime = startTime;
        this.jobType = jobType;
        this.jobStatusType = jobStatusType;
    }

    public Date getCompletedTime() {
        return DateUtil.addMilliseconds(startTime, runDuration);
    }
}
