/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.jobs.cache;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.system.RepoCacheDir;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * TODO we may need to put a lock for each url and only allow one thread to access one folder
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class RepoCacheImpl implements RepoCache {
    private static final Logger log =
            LoggerFactory.getLogger(RepoCacheImpl.class);
    private Path cacheDir;

    @Inject
    public RepoCacheImpl(@RepoCacheDir Path cacheDir) {
        this.cacheDir = cacheDir;
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unused")
    public RepoCacheImpl() {
    }

    @Override
    public boolean getAndCopyToIfPresent(String url, Path dest) {
        Optional<Path> cachedRepo = getCachedRepo(url);
        if (!cachedRepo.isPresent()) {
            return false;
        }
        log.info("found repo cache for {}", url);
        Path source = cachedRepo.get();
        try {
            long size = copyDir(source, dest);
            log.info("copy cached repo to {} (size:{})", dest, size);
            return true;
        } catch (Exception e) {
            log.warn("error copying cached repo", e);
            return false;
        }
    }

    private long copyDir(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        AtomicLong totalSize = new AtomicLong(0);
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir,
                            BasicFileAttributes attrs)
                            throws IOException {
                        Path targetdir =
                                target.resolve(source.relativize(dir));
                        try {
                            if (Files.isDirectory(targetdir) &&
                                    Files.exists(targetdir)) {
                                return CONTINUE;
                            }
                            Files.copy(dir, targetdir,
                                    StandardCopyOption.REPLACE_EXISTING,
                                    StandardCopyOption.COPY_ATTRIBUTES);
                        } catch (FileAlreadyExistsException e) {
                            if (!Files.isDirectory(targetdir)) {
                                throw e;
                            }
                        }
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs)
                            throws IOException {
                        if (Files.isRegularFile(file)) {
                            totalSize.accumulateAndGet(Files.size(file),
                                    (l, r) -> l + r);
                        }
                        Path targetFile =
                                target.resolve(source.relativize(file));

                        // only copy to target if it doesn't exist or it exist but the content is different
                        if (!Files.exists(targetFile) ||
                                !com.google.common.io.Files
                                        .equal(file.toFile(),
                                                targetFile.toFile())) {
                            Files.copy(file,
                                    targetFile,
                                    StandardCopyOption.REPLACE_EXISTING,
                                    StandardCopyOption.COPY_ATTRIBUTES);
                        }
                        return CONTINUE;
                    }
                });
        return totalSize.get();
    }

    @Override
    public void put(String url, Path src) {
        try {
            long totalSize = copyDir(src, cacheDirForRepo(cacheDir, url));
            log.info("stored cache for [{}]. total size: {}", url, totalSize);
        } catch (Exception e) {
            log.warn("failed storing repo cache", e);
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
    public void invalidateAll(Iterable<String> keys) {
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
    public long diskUsage(String key) {
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
