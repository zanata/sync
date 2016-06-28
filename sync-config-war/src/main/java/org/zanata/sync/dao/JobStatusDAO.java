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
package org.zanata.sync.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import com.google.common.annotations.VisibleForTesting;


/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
public class JobStatusDAO {
    private static final Logger log =
            LoggerFactory.getLogger(JobStatusDAO.class);
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public JobStatusDAO() {
    }

    @VisibleForTesting
    JobStatusDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<JobStatus> getJobStatusList(SyncWorkConfig config,
            JobType type) {
        return entityManager
                .createNamedQuery(JobStatus.GET_JOB_STATUS_QUERY, JobStatus.class)
                .setParameter("workConfig", config)
                .setParameter("jobType", type)
                .getResultList();
    }

    public JobStatus getLatestJobStatus(SyncWorkConfig config,
            JobType jobType) {
        List<JobStatus> resultList = entityManager
                .createNamedQuery(JobStatus.GET_JOB_STATUS_QUERY, JobStatus.class)
                .setParameter("workConfig", config)
                .setParameter("jobType", jobType)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();
        if (resultList.isEmpty()) {
            return JobStatus.EMPTY;
        }
        return resultList.get(0);
    }

    @TransactionAttribute
    public void updateJobStatus(String jobId, @Nullable Date endTime,
            @Nullable Date nextFireTime, JobStatusType statusType) {
        JobStatus entity =
                entityManager.find(JobStatus.class, jobId);
        if (entity != null) {
            entity.changeState(endTime, nextFireTime, statusType);
            log.debug("JobStatus for {} updated. endTime: {}, nextFireTime: {}, status: {}", jobId,
                    endTime, nextFireTime, statusType);
        } else {
            log.warn("job {} not found", jobId);
        }
    }

    @TransactionAttribute
    public void saveJobStatus(JobStatus status) {
        entityManager.persist(status);
    }

    public Optional<JobStatus> findById(String jobFiringId) {
        JobStatus jobStatus = entityManager.find(JobStatus.class, jobFiringId);
        return Optional.ofNullable(jobStatus);
    }
}
