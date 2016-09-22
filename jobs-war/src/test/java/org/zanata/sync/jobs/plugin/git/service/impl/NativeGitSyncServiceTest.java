package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.sync.common.model.SyncJobDetail;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class NativeGitSyncServiceTest {
    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private NativeGitSyncService git;
    private Path workDir;

    @Before
    public void setUp() throws Exception {
        git = new NativeGitSyncService();

        workDir = temporaryFolder.newFolder().toPath();

    }

    private SyncJobDetail createJobDetail(String branch) {
        return SyncJobDetail.Builder.builder()
                .setZanataUsername("pahuang")
                .setSrcRepoBranch(branch)
                .setSrcRepoUrl(remoteGitRepoRule.getRemoteUrl())
                .build();

    }

    @Test
    public void canClone() throws Exception {
        git.cloneRepo(createJobDetail(null), workDir);

        assertThat(workDir.toFile().listFiles()).hasSize(2)
                .as("the folder should contain one text file and one .git folder");
        assertThat(workDir.toFile().listFiles())
                .extracting(File::getName)
                .hasSameElementsAs(remoteGitRepoRule.getFilesInWorkTree("master"));
    }

    @Test
    public void canCloneAndCheckoutToANewBranch() throws Exception {
        git.cloneRepo(createJobDetail("work"), workDir);

        Git git = Git.open(workDir.toFile());
        List<Ref> refs = git.branchList().call();
        List<String> refNames =
                refs.stream().map(Ref::getName).collect(Collectors.toList());
        assertThat(refNames).contains("refs/heads/work");
    }

    @Test
    @Ignore("need real password to test")
    public void canCloneHttps() {
        SyncJobDetail jobDetail = SyncJobDetail.Builder.builder()
                .setZanataUsername("pahuang")
                .setSrcRepoUsername("user")
                .setSrcRepoSecret("unreal")
                .setSrcRepoUrl("https://gitlab.cee.redhat.com/zanata/zanata-itos.git")
                .build();
        git.cloneRepo(jobDetail, workDir);

        assertThat(workDir.toFile().listFiles())
                .haveAtLeastOne(new Condition<>(
                        file -> file.getName().equals(".git"), "has .git folder"));
    }

}
