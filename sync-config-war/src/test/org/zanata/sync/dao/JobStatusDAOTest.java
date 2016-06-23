package org.zanata.sync.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zanata.sync.EntityManagerRule;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;

public class JobStatusDAOTest {
    @Rule
    public EntityManagerRule entityManagerRule = new EntityManagerRule();

    private JobStatusDAO dao;

    @Before
    public void setUp() {
        dao = new JobStatusDAO(entityManagerRule.getEm());
    }

    @Test
    public void canSaveNewStatusAndUpdate() {
        SyncWorkConfig syncWorkConfig =
                new SyncWorkConfig(null, "name", null, null, null,
                        SyncOption.SOURCE, "git", null, true, true, "username",
                        "{}", "{}");
        entityManagerRule.getEm().persist(syncWorkConfig);
        JobStatus jobStatus =
                new JobStatus("id", syncWorkConfig, JobType.REPO_SYNC,
                        JobStatusType.RUNNING, null, null, null);
        dao.saveJobStatus(jobStatus);

        JobStatus jobStatusWithSameId =
                new JobStatus("id", syncWorkConfig, JobType.REPO_SYNC,
                        JobStatusType.ERROR, null, null, null);
        dao.saveJobStatus(jobStatusWithSameId);

        JobStatus status = entityManagerRule.getEm().find(JobStatus.class, "id");
        assertThat(status.getStatus())
                .isEqualTo(JobStatusType.ERROR);
    }
}