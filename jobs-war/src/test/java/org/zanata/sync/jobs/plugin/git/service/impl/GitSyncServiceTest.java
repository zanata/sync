package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;

import static org.assertj.core.api.Assertions.assertThat;

public class GitSyncServiceTest {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncServiceTest.class);
    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private GitSyncService syncService;
    private Path dest;

    @Before
    public void setUp() throws Exception {
        // set up a repo on local file system and use it as a remote to test
        UsernamePasswordCredential credential = new UsernamePasswordCredential(
                "", "");
        syncService =
                new GitSyncService(null);

        dest = temporaryFolder.newFolder().toPath();
    }

    @Test
    public void canCloneGitRepo() throws IOException {
        assertThat(dest.toFile().listFiles()).isNullOrEmpty();
        syncService.cloneRepo(createJobDetail("master"), dest);

        assertThat(dest.toFile().listFiles()).isNotEmpty();
    }

    @Test
    public void canCheckOutBranch() {
        syncService.cloneRepo(createJobDetail("junit"), dest);

        assertThat(dest.toFile().listFiles()).isNotEmpty();
    }

    @Test
    public void canPushToBranch() throws Exception {
        SyncJobDetail jobDetail = createJobDetail("junit");
        syncService.cloneRepo(jobDetail, dest);

        // add a new file
        File newFile = new File(dest.toFile(), "test.txt");
        try (PrintWriter printWriter = new PrintWriter(
                new FileWriter(newFile, true))) {
            printWriter.print(new Date());
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // commit the change
        syncService.syncTranslationToRepo(jobDetail, dest);


        assertThat(remoteGitRepoRule.getFilesInWorkTree("junit")).contains("test.txt");
        assertThat(remoteGitRepoRule.getCommitMessages("junit").get(0)).contains("Zanata Sync");
    }

    @Test
    public void canCloneInternalRepo() {
        String sslTrustStore = System.getProperty("javax.net.ssl.trustStore");
        Assume.assumeThat(
                "alternative trust store is provided as system property",
                sslTrustStore,
                CoreMatchers.notNullValue());

        SyncJobDetail jobDetail = SyncJobDetail.Builder.builder()
                .setZanataUsername("pahuang")
                .setSrcRepoUrl(
                        "https://gitlab.cee.redhat.com/pahuang/zanata-itos.git")
                .build();

        syncService.cloneRepo(jobDetail, dest);
    }

    @Test
    public void experiment() throws IOException, GitAPIException {
        Git git = Git.open(new File("/home/pahuang/work/test/test-repo"));
        List<Ref> refs =
                git.branchList().setListMode(ListBranchCommand.ListMode.ALL)
                        .call();

        List<String> refNames =
                refs.stream().map(Ref::getName).collect(Collectors.toList());

        System.out.println(refNames);
    }

    private SyncJobDetail createJobDetail(String branch) {
        return SyncJobDetail.Builder.builder()
                .setZanataUsername("pahuang")
                .setSrcRepoBranch(branch)
                .setSrcRepoUrl(remoteGitRepoRule.getRemoteUrl())
                .build();

    }
}
