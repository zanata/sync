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

import org.quartz.JobKey;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class JobStartedEvent {
    private String firingId;
    private Long configId;
    private JobType jobType;
    private Date startTime;
    private JobStatusType jobStatusType;
    private JobKey jobKey;

    public JobStartedEvent(String firingId, Long configId,
            JobType jobType, Date startTime,
            JobStatusType jobStatusType, JobKey jobKey) {
        this.firingId = firingId;
        this.configId = configId;
        this.jobType = jobType;
        this.startTime = startTime;
        this.jobStatusType = jobStatusType;
        this.jobKey = jobKey;
    }

    public String getFiringId() {
        return firingId;
    }

    public Long getConfigId() {
        return configId;
    }

    public JobType getJobType() {
        return jobType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public JobStatusType getJobStatusType() {
        return jobStatusType;
    }

    public JobKey getJobKey() {
        return jobKey;
    }
}
