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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

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
@JsonInclude(JsonInclude.Include.ALWAYS)
@Entity
@Table(name = "Job_Status_table")
@Access(AccessType.FIELD)
public class JobStatus implements Serializable {
    public static JobStatus EMPTY = new JobStatus();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private JobStatusType status = JobStatusType.NONE;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= DateUtil.DATE_TIME_FORMAT)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= DateUtil.DATE_TIME_FORMAT)
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= DateUtil.DATE_TIME_FORMAT)
    private Date nextStartTime;

    //This field is being ignored. See {@link SyncWorkConfigRepresenter}
    // TODO check if this field is used
    @JsonIgnore
    @Transient
    private JobProgress currentProgress = null;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workId")
    private SyncWorkConfig workConfig;

    public JobStatus(SyncWorkConfig workConfig, JobType jobType,
            JobStatusType status,
            Date startTime, Date endTime, Date nextStartTime) {
        this.workConfig = workConfig;
        this.status = status;
        this.jobType = jobType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.nextStartTime = nextStartTime;
    }

    public void updateCurrentProgress(JobProgress currentProgress) {
        this.currentProgress = currentProgress;
    }
}
