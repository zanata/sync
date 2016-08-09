package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.BufferedWriter;
import java.io.File;

import org.eclipse.jgit.api.Git;
import org.junit.rules.TemporaryFolder;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
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
        initGitRepo(remoteRepo);
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

    public File getRemoteRepo() {
        return remoteRepo;
    }
}
