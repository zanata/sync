package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;

import static org.assertj.core.api.Assertions.assertThat;

public class NativeGitSyncServiceTest {
    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private NativeGitSyncService git;
    private File remoteRepo;
    private File workDir;

    @Before
    public void setUp() throws Exception {
        git = new NativeGitSyncService();

        remoteRepo = remoteGitRepoRule.getRemoteRepo();
        workDir = temporaryFolder.newFolder();

        git.setCredentials(new UsernamePasswordCredential("admin", "pass"));
        git.setUrl("file://" + remoteRepo.getAbsolutePath());
        git.setWorkingDir(workDir);
    }

    @Test
    public void canClone() throws Exception {
        git.cloneRepo();

        assertThat(workDir.listFiles()).hasSize(1);
        assertThat(workDir.listFiles()[0].getName())
                .isEqualTo(remoteRepo.getName());
    }

    @Test
    @Ignore("need real password to test")
    public void canCloneHttps() {
        git.setUrl("https://gitlab.cee.redhat.com/zanata/zanata-itos.git");
        git.setCredentials(new UsernamePasswordCredential("user", "not real"));

        git.cloneRepo();

        assertThat(workDir.listFiles()).hasSize(1);
    }

}
