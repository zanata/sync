package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class GitSyncServiceTest {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncServiceTest.class);
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private GitSyncService syncService;
    private File dest;
    private File remoteRepo;

    @Before
    public void setUp() throws Exception {
        // set up a repo on local file system and use it as a remote to test
        UsernamePasswordCredential credential = new UsernamePasswordCredential(
                "", "");
        syncService =
                new GitSyncService();
        syncService.setCredentials(credential);
        dest = temporaryFolder.newFolder();
        remoteRepo = temporaryFolder.newFolder();
        initGitRepo(remoteRepo);

        syncService.setUrl("file://" + remoteRepo.getAbsolutePath());
        syncService.setWorkingDir(dest);
    }

    private static void initGitRepo(File repoRoot) {
        try (BufferedWriter writer =
                Files.newWriter(new File(repoRoot, "readme.txt"),
                        Charsets.UTF_8)) {
            writer.write("hello, world");
            Git.init().setDirectory(repoRoot).call();
            Git.open(repoRoot).add().addFilepattern(".").call();
            Git.open(repoRoot).commit()
                    .setCommitter("JUnit", "junit@example.com")
                    .setMessage("Init commit").call();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
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

        Ref ref = Git.open(remoteRepo).checkout().setName("junit").call();
        Iterable<RevCommit> logs = Git.open(remoteRepo).log().call();
        ImmutableList<RevCommit> revCommits = ImmutableList.copyOf(logs);
        List<String> logMessage = revCommits.stream()
                .map(RevCommit::getShortMessage).collect(
                        Collectors.toList());
        assertThat(logMessage.get(0)).contains("pushing translation");
    }
}
