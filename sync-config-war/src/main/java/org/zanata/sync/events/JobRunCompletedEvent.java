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
package org.zanata.sync.events;

import java.util.Date;

import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.util.DateUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class JobRunCompletedEvent {
    private String jobFireId;
    private Long configId;
    private Date endTime;
    private JobType jobType;
    private JobStatusType jobStatusType;

    private JobRunCompletedEvent(String jobFireId, Long configId,
            Date endTime, JobType jobType,
            JobStatusType jobStatusType) {
        this.jobFireId = jobFireId;
        this.configId = configId;
        this.endTime = endTime;
        this.jobType = jobType;
        this.jobStatusType = jobStatusType;
    }

    public static JobRunCompletedEvent endedInError(String jobFireId,
            Long configId,
            long runDuration, Date startTime, JobType jobType) {
        return new JobRunCompletedEvent(jobFireId, configId,
                getEndTime(startTime, runDuration),
                jobType, JobStatusType.ERROR);
    }

    public static JobRunCompletedEvent finished(String jobFireId,
            Long configId, JobType jobType, JobStatusType status) {
        return new JobRunCompletedEvent(jobFireId, configId, new Date(),
                jobType, status);
    }

    private static Date getEndTime(Date startTime, long runDuration) {
        return DateUtil.addMilliseconds(startTime, runDuration);
    }

    public String getJobFireId() {
        return jobFireId;
    }

    public Long getConfigId() {
        return configId;
    }

    public Date getEndTime() {
        return endTime;
    }

    public JobType getJobType() {
        return jobType;
    }

    public JobStatusType getJobStatusType() {
        return jobStatusType;
    }
}
