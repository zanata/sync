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
package org.zanata.sync.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

import org.zanata.sync.dao.JobStatusDAO;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.common.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.service.JobStatusService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
public class JobStatusServiceEJB implements JobStatusService {

    @Inject
    private JobStatusDAO jobStatusDAO;

    @Override
    public JobStatus getLatestJobStatus(SyncWorkConfig config,
            JobType jobType) {
        return jobStatusDAO.getLatestJobStatus(config, jobType);
    }

    @Override
    @TransactionAttribute
    public Optional<JobStatus> updateJobStatus(String jobId, @Nullable
            Date endTime,
            @Nullable Date nextFireTime, JobStatusType statusType) {
        return jobStatusDAO.updateJobStatus(jobId, endTime, nextFireTime, statusType);
    }

    @Override
    @TransactionAttribute
    public void saveJobStatus(JobStatus status) {
        jobStatusDAO.saveJobStatus(status);
    }

    @Override
    public List<JobStatus> getAllJobStatus(SyncWorkConfig config) {
        return jobStatusDAO.getJobStatusList(config);
    }

    @Override
    public Optional<JobStatus> getJobStatusByFiringId(String jobFiringId) {
        return jobStatusDAO.findById(jobFiringId);
    }
}
