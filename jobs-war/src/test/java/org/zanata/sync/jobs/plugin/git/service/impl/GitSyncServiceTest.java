package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private File dest;

    @Before
    public void setUp() throws Exception {
        // set up a repo on local file system and use it as a remote to test
        UsernamePasswordCredential credential = new UsernamePasswordCredential(
                "", "");
        syncService =
                new GitSyncService();
        syncService.setCredentials(credential);

        dest = temporaryFolder.newFolder();
        syncService.setUrl(remoteGitRepoRule.getRemoteUrl());
        syncService.setWorkingDir(dest);
        syncService.setZanataUser("pahuang");
    }

    @Test
    public void canCloneGitRepo() throws IOException {
        assertThat(dest.listFiles()).isNullOrEmpty();
        syncService.setBranch("master");
        syncService.cloneRepo();

        assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void canCheckOutBranch() {
        syncService.setBranch("junit");
        syncService.cloneRepo();

        assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void canPushToBranch() throws Exception {
        syncService.setBranch("junit");
        syncService.cloneRepo();

        // add a new file
        File newFile = new File(dest, "test.txt");
        try (PrintWriter printWriter = new PrintWriter(
                new FileWriter(newFile, true))) {
            printWriter.print(new Date());
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // commit the change
        syncService.syncTranslationToRepo();


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


        syncService
                .setUrl("https://gitlab.cee.redhat.com/pahuang/zanata-itos.git");
        syncService.setBranch("master");

        syncService.cloneRepo();
    }
}
