package org.zanata.sync.model;

import java.io.Serializable;
import java.util.Date;

import org.zanata.sync.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobStatus implements Serializable {
    public static JobStatus EMPTY = new JobStatus();

    private JobStatusType status = JobStatusType.NONE;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= DateUtil.DATE_TIME_FORMAT)
    private Date lastStartTime;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= DateUtil.DATE_TIME_FORMAT)
    private Date lastEndTime;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= DateUtil.DATE_TIME_FORMAT)
    private Date nextStartTime;

    //This field is being ignored. See {@link SyncWorkConfigRepresenter}
    @JsonIgnore
    private JobProgress currentProgress = null;

    public JobStatus(JobStatusType status, Date lastStartTime, Date lastEndTime,
            Date nextStartTime) {
        this.status = status;
        this.lastStartTime = lastStartTime;
        this.lastEndTime = lastEndTime;
        this.nextStartTime = nextStartTime;
    }

    public void updateCurrentProgress(JobProgress currentProgress) {
        this.currentProgress = currentProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobStatus)) return false;

        JobStatus jobStatus = (JobStatus) o;

        if (status != jobStatus.status) return false;
        if (lastStartTime != null ?
            !lastStartTime.equals(jobStatus.lastStartTime) :
            jobStatus.lastStartTime != null) return false;
        if (lastEndTime != null ? !lastEndTime.equals(jobStatus.lastEndTime) :
            jobStatus.lastEndTime != null) return false;
        return !(nextStartTime != null ?
            !nextStartTime.equals(jobStatus.nextStartTime) :
            jobStatus.nextStartTime != null);

    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result =
            31 * result +
                (lastStartTime != null ? lastStartTime.hashCode() : 0);
        result =
            31 * result + (lastEndTime != null ? lastEndTime.hashCode() : 0);
        result =
            31 * result +
                (nextStartTime != null ? nextStartTime.hashCode() : 0);
        return result;
    }
}
