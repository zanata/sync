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
package org.zanata.sync.jobs.common;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * This will keep an internal cache to store path to reentrant lock. For each
 * path, when it's checked out, it will be locked until the release method is
 * called.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class LockablePath {
    private static final Logger log =
            LoggerFactory.getLogger(LockablePath.class);

    private Cache<String, ReentrantLock> directoryLocks = CacheBuilder
            .newBuilder().concurrencyLevel(1).build();


    public void checkoutPath(Path workingDir)
            throws ExecutionException {
        ReentrantLock reentrantLock =
                directoryLocks.get(workingDir.toString(), ReentrantLock::new);
        log.info("{} checked out", workingDir);
        reentrantLock.lock();
    }


    public void release(Path workspace) {
        if (workspace != null) {
            ReentrantLock lock =
                    directoryLocks.getIfPresent(workspace.toString());
            if (lock != null) {
                log.info("release {}", workspace);
                lock.unlock();
            }
        }
    }
}
