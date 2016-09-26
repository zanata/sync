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
package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.jobs.cache.RepoCache;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Decorator
@RepoPlugin
public class CacheableRepoSyncService implements RepoSyncService {
    private static final Logger log =
            LoggerFactory.getLogger(CacheableRepoSyncService.class);
    @Inject
    @Delegate
    @RepoPlugin
    private GitSyncService delegate;

    @Inject
    private RepoCache repoCache;

    @Override
    public void cloneRepo(SyncJobDetail jobDetail, Path workingDir)
            throws RepoSyncException {
//        log.info("--- decorating");
        if (!repoCache
                .getAndCopyToIfPresent(jobDetail.getSrcRepoUrl(), workingDir)) {
            delegate.cloneRepo(jobDetail, workingDir);
            repoCache.put(jobDetail.getSrcRepoUrl(), workingDir);
        }
        try (Git git = Git.open(workingDir.toFile())) {
            GitSyncService.doGitFetch(git);
            GitSyncService.checkOutBranch(git, getBranchOrDefault(jobDetail.getSrcRepoBranch()));
            GitSyncService.cleanUpCurrentBranch(git);
        } catch (IOException | GitAPIException e) {
            throw new RepoSyncException("failed clone repo:" + jobDetail, e);
        }
        // this will cause stack overflow (the delegate will go into another decorator and causes infinite looping
//        Callable<Path> loader = () -> {
//            log.info("--- calling loader");
//            delegate.cloneRepo(jobDetail, workingDir);
//            return workingDir;
//        };
//        repoCache.get(jobDetail.getSrcRepoUrl(), workingDir, loader);
    }

    @Override
    public void syncTranslationToRepo(SyncJobDetail jobDetail, Path workingDir)
            throws RepoSyncException {
        delegate.syncTranslationToRepo(jobDetail, workingDir);
        repoCache.put(jobDetail.getSrcRepoUrl(), workingDir);
    }

    @Override
    public String supportedRepoType() {
        return delegate.supportedRepoType();
    }
}
