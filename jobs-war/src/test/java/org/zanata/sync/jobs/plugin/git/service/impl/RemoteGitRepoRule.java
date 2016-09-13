package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.rules.TemporaryFolder;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RemoteGitRepoRule extends TemporaryFolder {
    private File remoteRepo;

    @Override
    protected void before() throws Throwable {
        super.before();
        remoteRepo = newFolder();
        initGitRepo();
    }

    private void initGitRepo() {
        addFile("messages.properties", "greeting=hello, world");
        try {
            Git.init().setDirectory(remoteRepo).call();
        } catch (GitAPIException e) {
            throw Throwables.propagate(e);
        }
        commitFiles("init commit");
    }

    public void addFile(String fileName, String content) {
        File file = new File(remoteRepo, fileName);
        try (BufferedWriter writer =
                Files.newWriter(file, Charsets.UTF_8)) {
            writer.write(content);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void commitFiles(String message) {
        try {
            Git git = Git.open(remoteRepo);
            git.add().addFilepattern(".").call();
            git.commit().setCommitter("JUnit", "junit@example.com")
                    .setMessage(message).call();
        } catch (GitAPIException | IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public List<String> getCommitMessages() {
        try {
            Git git = Git.open(remoteRepo);
            Iterable<RevCommit> commits = git.log().setMaxCount(10).call();
            return ImmutableList.copyOf(commits).stream().map(
                    RevCommit::getFullMessage).collect(Collectors.toList());
        } catch (IOException | GitAPIException e) {
            throw Throwables.propagate(e);
        }
    }

    public File getRemoteRepo() {
        return remoteRepo;
    }
}
