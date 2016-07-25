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
package org.zanata.sync.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ejb.TransactionAttribute;

import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface JobStatusService {

    JobStatus getLatestJobStatus(SyncWorkConfig config,
            JobType jobType);

    @TransactionAttribute
    Optional<JobStatus> updateJobStatus(String jobId, @Nullable
            Date endTime,
            @Nullable Date nextFireTime, JobStatusType statusType);

    @TransactionAttribute
    void saveJobStatus(JobStatus status);

    List<JobStatus> getAllJobStatus(SyncWorkConfig config);

    Optional<JobStatus> getJobStatusByFiringId(String jobFiringId);
}
