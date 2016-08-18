package org.zanata.sync.dao;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zanata.sync.EntityManagerRule;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.RepoAccount;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.model.ZanataAccount;

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
        ZanataAccount zanataAccount = new ZanataAccount("localUser");
        RepoAccount repoAccount =
                new RepoAccount("git", "https://github.com", null, null,
                        zanataAccount);
        zanataAccount.getRepoAccounts().add(repoAccount);
        entityManagerRule.getEm().persist(zanataAccount);
        SyncWorkConfig syncWorkConfig =
                new SyncWorkConfig(null, "name", null, null, null, null,
                        SyncOption.SOURCE, true, true,
                        "https://github.com/zanata/zanata-server.git",
                        null, zanataAccount, repoAccount);
        entityManagerRule.getEm().persist(syncWorkConfig);
        JobStatus jobStatus =
                new JobStatus("id", syncWorkConfig, JobType.REPO_SYNC,
                        JobStatusType.RUNNING, null, null, null);
        dao.saveJobStatus(jobStatus);

        JobStatus status = entityManagerRule.getEm().find(JobStatus.class, "id");
        assertThat(status.getStatus()).isEqualTo(JobStatusType.RUNNING);

        dao.updateJobStatus("id", new Date(), null, JobStatusType.COMPLETED);
        status = entityManagerRule.getEm().find(JobStatus.class, "id");
        assertThat(status.getStatus()).isEqualTo(JobStatusType.COMPLETED);
    }
}
