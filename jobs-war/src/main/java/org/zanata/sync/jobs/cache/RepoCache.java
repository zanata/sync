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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import com.google.common.hash.Hashing;

public interface RepoCache {
    Logger log = LoggerFactory.getLogger(RepoCache.class);

    /**
     * Will try to get the repo cache if available and copy to {@code dest}.
     *
     * @param url
     *         repo url
     * @param dest
     *         destination path if repo cache can be found
     * @return true if cache is available and the copy is successful
     */
    boolean getAndCopyToIfPresent(String url, Path dest);

    void put(String url, Path src);

    default void get(String url, Path dest, Callable<Path> loader) {
        if (!getAndCopyToIfPresent(url, dest)) {
            try {
                Path source = loader.call();
                put(url, source);
            } catch (Exception e) {
                log.error("error calling the cache loader", e);
                throw new RepoSyncException(e.getMessage());
            }
        }
    }

    /**
     * Get the path of cached repo.
     *
     * @param url
     *         repo url
     * @return optional path if the repo is cached
     */
    Optional<Path> getCachedRepo(String url);

    /**
     * Discards any cached value for url.
     */
    void invalidate(String url);

    /**
     * Discards any cached values for urls.
     */
    void invalidateAll(Iterable<String> urls);

    /**
     * Discards all entries in the cache.
     */
    void invalidateAll();

    /**
     * Returns the approximate number of entries in this cache.
     */
    long size();

    /**
     * Disk usage for this key
     *
     * @return number of bytes used by storing the value fir this key.
     */
    long diskUsage(String url);

    /**
     * Disk usage for the entire cache.
     *
     * @return number of bytes used for the entire cache.
     */
    long allDiskUsage();

    /**
     * Performs any pending maintenance operations needed by the cache. Exactly
     * which activities are performed -- if any -- is implementation-dependent.
     */
    void cleanUp();

    /**
     * For a given url, return the cache directory for it.
     * @param cacheDir root cache directory
     * @param url the repo url
     * @return where to store this repo cache
     */
    default Path cacheDirForRepo(Path cacheDir, String url) {
        String hash = Key.of(url).getHash();
        return Paths.get(cacheDir.toString(), hash);
    }

    default Optional<Path> getCachedRepo(Path rootCacheDir, String url) {
        Path expected = cacheDirForRepo(rootCacheDir, url);
        boolean exists = Files.exists(expected);
        if (exists && Files.isDirectory(expected)) {
            try (Stream<Path> subPaths = Files.list(expected)) {
                if (subPaths.count() > 0) {
                    return Optional.of(expected);
                }
            } catch (IOException e) {
                log.warn("error reading cached repo folder: " + expected, e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    class Key {
        private final String url;
        private final String hash;

        private Key(String url) {
            this.url = url;
            this.hash = urlToHash(url);
        }

        public static Key of(String url) {
            return new Key(url);
        }

        private static String urlToHash(String url) {
            return Hashing.sha1().hashString(url, Charsets.UTF_8).toString();
        }

        public String getUrl() {
            return url;
        }

        String getHash() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(url, key.url) &&
                    Objects.equals(hash, key.hash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, hash);
        }
    }
}
