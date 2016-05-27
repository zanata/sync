package org.zanata.sync.plugin.git.service.impl;

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
import org.zanata.sync.common.model.UsernamePasswordCredential;

public class GitSyncServiceTest {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncServiceTest.class);
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private GitSyncService syncService;
    private File dest;

    @Before
    public void setUp() throws Exception {
        String username = JunitAssumptions.assumeGitUsernameExists();
        String password = JunitAssumptions.assumeGitPasswordExists();
//        String username = "";
//        String password = "";
        syncService =
                new GitSyncService(new UsernamePasswordCredential(
                        username, password));
        dest = temporaryFolder.newFolder();
    }

    @Test
    public void canCloneGitRepo() throws IOException {
        Assertions.assertThat(dest.listFiles()).isNullOrEmpty();

        syncService.cloneRepo("https://github.com/zanata/zanata-api.git", null,
                dest);

        Assertions.assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void canCheckOutBranch() {
        syncService.cloneRepo("https://github.com/huangp/test-repo.git", "junit", dest);

        Assertions.assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void willPullIfFolderIsAlreadyAGitRepo() {
        // fist clone will clone and checkout to new branch
        syncService.cloneRepo("https://github.com/huangp/test-repo.git", "junit", dest);

        // second run will do git pull and check out to the same branch
        syncService.cloneRepo("https://github.com/huangp/test-repo.git", "junit", dest);

        Assertions.assertThat(dest.listFiles()).isNotEmpty();
    }

    @Test
    public void canPushToBranch() throws Exception {
        syncService.cloneRepo("https://github.com/huangp/test-repo.git", "junit", dest);

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
                        syncService.getCredentials().getUsername(),
                        syncService.getCredentials().getSecret());
        pushCommand.setCredentialsProvider(user);
        pushCommand.call();

        Set<String> uncommittedChanges =
                git.status().call().getUncommittedChanges();
        Assertions.assertThat(uncommittedChanges).isEmpty();
    }
}
