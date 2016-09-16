package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

public class NativeGitSyncServiceTest {
    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private NativeGitSyncService git;
    private File workDir;

    @Before
    public void setUp() throws Exception {
        git = new NativeGitSyncService();

        workDir = temporaryFolder.newFolder();

        git.setCredentials(new UsernamePasswordCredential("admin", "pass"));
        git.setUrl(remoteGitRepoRule.getRemoteUrl());
        git.setWorkingDir(workDir);

    }

    @Test
    public void canClone() throws Exception {
        git.cloneRepo();

        assertThat(workDir.listFiles()).hasSize(2)
                .as("the folder should contain one text file and one .git folder");
        assertThat(workDir.listFiles())
                .extracting(File::getName)
                .hasSameElementsAs(remoteGitRepoRule.getFilesInWorkTree("master"));
    }

    @Test
    public void canCloneAndCheckoutToANewBranch() throws Exception {
        git.setBranch("work");
        git.cloneRepo();

        Git git = Git.open(workDir);
        List<Ref> refs = git.branchList().call();
        List<String> refNames =
                refs.stream().map(Ref::getName).collect(Collectors.toList());
        assertThat(refNames).contains("refs/heads/work");
    }

    @Test
    @Ignore("need real password to test")
    public void canCloneHttps() {
        git.setUrl("https://gitlab.cee.redhat.com/zanata/zanata-itos.git");
        git.setCredentials(new UsernamePasswordCredential("user", "not real"));

        git.cloneRepo();

        assertThat(workDir.listFiles())
                .haveAtLeastOne(new Condition<>(
                        file -> file.getName().equals(".git"), "has .git folder"));
    }

}
