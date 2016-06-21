package org.zanata.sync.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;


/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
public class JobStatusDAO {
    private static final Logger log =
            LoggerFactory.getLogger(JobStatusDAO.class);
    @PersistenceContext
    private EntityManager entityManager;


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
    public void saveJobStatus(JobStatus jobStatus) {
        JobStatus entity =
                entityManager.find(JobStatus.class, jobStatus.getId());
        if (entity == null) {
            entityManager.persist(jobStatus);
        } else {
            entityManager.merge(jobStatus);
        }
//        log.info("JobStatus saved." + config.getName() + ":" + type);
    }

}
