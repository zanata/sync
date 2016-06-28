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
package org.zanata.sync.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "Job_Status_table")
@Access(AccessType.FIELD)
@NamedQueries(
        {
                @NamedQuery(name = JobStatus.GET_JOB_STATUS_QUERY,
                        query = "from JobStatus status where status.workConfig = :workConfig and status.jobType = :jobType order by endTime desc")
        }
)
public class JobStatus implements Serializable {
    public static final String GET_JOB_STATUS_QUERY = "GetJobStatusQuery";
    public static JobStatus EMPTY = new JobStatus();
    @Id
    @Column(unique = true, nullable = false, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    private JobStatusType status = JobStatusType.NONE;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date nextStartTime;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "workId")
    private SyncWorkConfig workConfig;

    public JobStatus(String id, SyncWorkConfig workConfig, JobType jobType,
            JobStatusType status,
            Date startTime, Date endTime, Date nextStartTime) {
        this.id = id;
        this.workConfig = workConfig;
        this.status = status;
        this.jobType = jobType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.nextStartTime = nextStartTime;
    }

    public static JobStatus started(String id, SyncWorkConfig workConfig,
            JobType jobType, Date startTime, Date nextFireTime) {
        return new JobStatus(id, workConfig, jobType, JobStatusType.STARTED,
                startTime, null, nextFireTime);
    }

    public void setStatus(JobStatusType status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("status", status)
                .add("jobType", jobType)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .add("nextStartTime", nextStartTime)
                .add("workConfig", workConfig)
                .toString();
    }

    public void changeState(Date endTime, Date nextFireTime,
            JobStatusType statusType) {
        this.endTime = endTime;
        this.nextStartTime = nextFireTime;
        this.status = statusType;
    }
}
