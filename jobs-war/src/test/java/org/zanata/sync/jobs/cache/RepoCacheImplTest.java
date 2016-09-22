package org.zanata.sync.jobs.cache;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.plugin.git.service.impl.GitSyncService;
import org.zanata.sync.jobs.plugin.git.service.impl.RemoteGitRepoRule;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

public class RepoCacheImplTest {
    private static final Logger log =
            LoggerFactory.getLogger(RepoCacheImplTest.class);

    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private RepoCacheImpl repoCache;
    private Path repoPath;

    @Before
    public void setUp() throws IOException {
        Path cacheDir = tempFolder.newFolder().toPath();
        repoCache = new RepoCacheImpl(cacheDir);
        repoPath = new File(
                URI.create(remoteGitRepoRule.getRemoteUrl()).toURL().getFile())
                .toPath();

    }

    @Test
    public void canStoreDirectoryAsCache() throws Exception {
        repoCache.put(remoteGitRepoRule.getRemoteUrl(), repoPath);

        Optional<Path> cachedRepo =
                repoCache.getCachedRepo(remoteGitRepoRule.getRemoteUrl());


        assertThat(cachedRepo.isPresent()).isTrue();

        Path theCache = cachedRepo.get();

        verifyDirsAreEqual(theCache, repoPath, p -> true);
    }

    @Test
    public void canStoreMultipleTimes() throws IOException {
        repoCache.put(remoteGitRepoRule.getRemoteUrl(), repoPath);
        repoCache.put(remoteGitRepoRule.getRemoteUrl(), repoPath);

        Optional<Path> cachedRepo =
                repoCache.getCachedRepo(remoteGitRepoRule.getRemoteUrl());
        Path theCache = cachedRepo.get();

        verifyDirsAreEqual(theCache, repoPath, path -> true);
    }

    @Test
    public void canLoadIfNotAvailable() throws IOException {
        Path dest = tempFolder.newFolder().toPath();

        assertThat(Files.list(dest).collect(Collectors.toList())).isEmpty();

        repoCache.get(remoteGitRepoRule.getRemoteUrl(), dest, () -> {
            Git.cloneRepository().setURI(remoteGitRepoRule.getRemoteUrl())
                    .setDirectory(dest.toFile()).call();
            return dest;
        });

        assertThat(Files.list(dest).collect(Collectors.toList())).isNotEmpty();

        // .git directory will be different as it will contain remote information
        verifyDirsAreEqual(dest, repoPath, path -> !path.toString().contains("/.git/"));
    }

    private static void verifyDirsAreEqual(Path one, Path other, Predicate<Path> filePredicate) throws IOException {
        Predicate<Path> excludePredicate = filePredicate.negate();

        Files.walkFileTree(one, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs)
                    throws IOException {

                FileVisitResult result = super.visitFile(file, attrs);

                if (excludePredicate.test(file)) {
                    log.debug("=== skipping file {}", file);
                    return result;
                }

                // get the relative file name from path "one"
                Path relativize = one.relativize(file);
                // construct the path for the counterpart file in "other"
                Path fileInOther = other.resolve(relativize);
                log.debug("=== comparing: {} to {}", file, fileInOther);

                assertThat(file).hasBinaryContent(Files.readAllBytes(fileInOther));
                return result;
            }
        });
    }

}
