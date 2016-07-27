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

import java.io.Serializable;

import org.zanata.sync.model.JobType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class JobSummary implements Serializable {
    private String jobKey;
    private Long workId;
    private boolean enabled;
    private JobType type;
    private JobRunStatus lastJobStatus;

    public JobSummary(String jobKey, Long workId,
            boolean enabled, JobType type,
            JobRunStatus lastJobStatus) {
        this.jobKey = jobKey;
        this.workId = workId;
        this.enabled = enabled;
        this.type = type;
        this.lastJobStatus = lastJobStatus;
    }

    public JobSummary() {
    }

    public String getJobKey() {
        return jobKey;
    }

    public Long getWorkId() {
        return workId;
    }

    public JobType getType() {
        return type;
    }

    public JobRunStatus getLastJobStatus() {
        return lastJobStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
