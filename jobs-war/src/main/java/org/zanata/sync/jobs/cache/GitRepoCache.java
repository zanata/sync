package org.zanata.sync.jobs.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.system.RepoCacheDir;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Alternative
@ApplicationScoped
public class GitRepoCache implements RepoCache {
    private static final Logger log =
            LoggerFactory.getLogger(GitRepoCache.class);

    private Path cacheDir;

    @Inject
    public GitRepoCache(@RepoCacheDir Path cacheDir) {
        this.cacheDir = cacheDir;
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unused")
    public GitRepoCache() {
    }

    @Override
    public boolean getAndCopyToIfPresent(String url, Path dest) {
        Optional<Path> cachedRepo = getCachedRepo(url);
        if (cachedRepo.isPresent()) {
            String cachedRepoUri = String.format("file://%s", cachedRepo.get());
            CloneCommand cloneCommand =
                    Git.cloneRepository().setDirectory(dest.toFile())
                            .setURI(cachedRepoUri)
                            .setCloneAllBranches(true);
            try (Git git = cloneCommand.call()) {
                StoredConfig config = git.getRepository().getConfig();
                // set the remote back to original
                config.setString("remote", "origin", "url", url);
                config.save();
                log.info("using cached repo: {}", cachedRepo.get());
                return true;
            } catch (Exception e) {
                log.warn("error copying cached repo", e);
            }
        }
        return false;
    }

    @Override
    public void put(String url, Path src) {
        Path dest = cacheDirForRepo(cacheDir, url);
        try {
            Files.createDirectories(dest);
        } catch (IOException e) {
            log.warn("error creating the cache repo", e);
            return;
        }
        try (Git source = Git.open(src.toFile())) {
            // first we init the cache repo
            Git.init().setBare(true).setDirectory(dest.toFile()).call();

            // add the cache repo as a remote
            Repository repo = source.getRepository();
            StoredConfig config = repo.getConfig();
            config.setString("remote", "cache", "url",
                    String.format("file://%s", dest));
            config.save();

            source.push().setRemote("cache").setPushAll().call();
        } catch (IOException | GitAPIException e) {
            log.warn("error storing repo as cache", e);
        }
    }

    @Override
    public Optional<Path> getCachedRepo(String url) {
        return getCachedRepo(cacheDir, url);
    }

    @Override
    public void invalidate(String url) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public void invalidateAll(Iterable<String> urls) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public void invalidateAll() {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public long size() {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return 0;
    }

    @Override
    public long diskUsage(String url) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return 0;
    }

    @Override
    public long allDiskUsage() {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return 0;
    }

    @Override
    public void cleanUp() {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }
}
