package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;

public class GitSyncServiceTest {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncServiceTest.class);
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private GitSyncService syncService;
    private File dest;
    private UsernamePasswordCredential credential;

    @Before
    public void setUp() throws Exception {
        // TODO set up a repo on local file system and use it as a remote to test
        String username = JunitAssumptions.assumeGitUsernameExists();
        String password = JunitAssumptions.assumeGitPasswordExists();
//        String username = "";
//        String password = "";
        credential = new UsernamePasswordCredential(
               username, password);
        syncService =
                new GitSyncService();
        syncService.setCredentials(credential);
        dest = temporaryFolder.newFolder();
        syncService.setWorkingDir(dest);
    }

    @Test
    public void canCloneGitRepo() throws IOException {
        Assertions.assertThat(dest.listFiles()).isNullOrEmpty();
        syncService.setUrl("https://github.com/zanata/zanata-api.git");
        syncService.cloneRepo();

        Assertions.assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void canCheckOutBranch() {
        syncService.setUrl("https://github.com/huangp/test-repo.git");
        syncService.setBranch("junit");
        syncService.cloneRepo();

        Assertions.assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void willPullIfFolderIsAlreadyAGitRepo() {
        // fist clone will clone and checkout to new branch
        syncService.setUrl("https://github.com/huangp/test-repo.git");
        syncService.setBranch("junit");
        syncService.cloneRepo();

        // second run will do git pull and check out to the same branch
        syncService.cloneRepo();

        Assertions.assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void canPushToBranch() throws Exception {
        syncService.setUrl("https://github.com/huangp/test-repo.git");
        syncService.setBranch("junit");
        syncService.cloneRepo();

        // add a new file
        File newFile = new File(dest, "test.txt");
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(newFile, true))) {
            printWriter.print(new Date());
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // commit the change
        Git git = Git.open(dest);
        git.add().addFilepattern(".").call();

        log.info("commit changed files");
        git.commit()
                .setCommitter("JUnit test", "zanata-users@redhat.com")
                .setMessage("test commit should be ignored or deleted")
                .call();

        // push to remote repo
        log.info("push to remote repo");
        PushCommand pushCommand = git.push();
        UsernamePasswordCredentialsProvider user =
                new UsernamePasswordCredentialsProvider(
                        credential.getUsername(),
                        credential.getSecret());
        pushCommand.setCredentialsProvider(user);
        pushCommand.call();

        Set<String> uncommittedChanges =
                git.status().call().getUncommittedChanges();
        Assertions.assertThat(uncommittedChanges).isEmpty();
    }
}
