/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.dto;

import java.util.Date;

import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Preconditions;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class JobRunStatus {
    private static final String TIMESTAMP_FMT = "yyyy-MM-dd HH:mm:ss";
    private Long workId;
    private String id;

    private JobStatusType status = JobStatusType.NONE;

    private JobType jobType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIMESTAMP_FMT)
    private Date startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIMESTAMP_FMT)
    private Date endTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIMESTAMP_FMT)
    private Date nextStartTime;

    @SuppressWarnings("unused")
    protected JobRunStatus() {
    }

    private JobRunStatus(Long workId, String id, JobStatusType status,
            JobType jobType, Date startTime, Date endTime,
            Date nextStartTime) {
        this.workId = workId;
        this.id = id;
        this.status = status;
        this.jobType = jobType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.nextStartTime = nextStartTime;
    }

    public static JobRunStatus fromEntity(JobStatus entity, Long configId, JobType jobType) {
        if (entity == JobStatus.EMPTY) {
            return notYetStarted(configId, jobType);
        }
        return new JobRunStatus(entity.getWorkConfig().getId(), entity.getId(),
                entity.getStatus(),
                entity.getJobType(), entity.getStartTime(), entity.getEndTime(),
                entity.getNextStartTime());
    }

    private static JobRunStatus notYetStarted(Long configId, JobType jobType) {
        return new JobRunStatus(configId, null, JobStatusType.NONE, jobType,
                null, null, null);
    }

    public Long getWorkId() {
        return workId;
    }

    public String getId() {
        return id;
    }

    public JobStatusType getStatus() {
        return status;
    }

    public JobType getJobType() {
        return jobType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getNextStartTime() {
        return nextStartTime;
    }
}
