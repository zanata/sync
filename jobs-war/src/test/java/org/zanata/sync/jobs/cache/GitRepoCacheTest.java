package org.zanata.sync.jobs.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.sync.jobs.plugin.git.service.impl.RemoteGitRepoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class GitRepoCacheTest {

    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private GitRepoCache repoCache;

    @Before
    public void setUp() {
        repoCache = new GitRepoCache(Paths.get("/tmp", "repo-cache"));
    }

    @Test
    public void canStoreRepoCache() throws IOException {
        repoCache.put(remoteGitRepoRule.getRemoteUrl(),
                remoteGitRepoRule.getRemoteRepoPath());

        Optional<Path> cachedRepo =
                repoCache.getCachedRepo(remoteGitRepoRule.getRemoteUrl());

        assertThat(cachedRepo.isPresent()).isTrue();
        Path path = cachedRepo.get();
        Set<String> subFileNames =
                Files.list(path).map(p -> p.toFile().getName())
                        .collect(Collectors.toSet());
        // the normal git bare stuff
        assertThat(subFileNames)
                .contains("branches", "HEAD", "hooks", "logs", "refs",
                        "objects", "config");
    }

}
