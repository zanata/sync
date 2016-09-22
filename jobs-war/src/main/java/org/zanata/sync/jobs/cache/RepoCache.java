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

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.io.Charsets;
import com.google.common.hash.Hashing;

public interface RepoCache {


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

    void get(String url, Path dest, Callable<Path> loader);

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

        public String getHash() {
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
