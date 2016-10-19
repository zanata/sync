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
package org.zanata.sync.jobs.ejb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.model.JobStatusType;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.jobs.common.LockablePath;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import org.zanata.sync.jobs.plugin.zanata.service.impl.ZanataSyncServiceImpl;
import org.zanata.sync.jobs.system.RepoCacheDir;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
@Asynchronous
public class JobRunner {
    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);
    @Inject
    private JobStatusPublisher jobStatusPublisher;

    @Inject
    @RepoPlugin
    private Instance<RepoSyncService> repoSyncServices;

    @Inject
    @RepoCacheDir
    private Path repoCacheRoot;

    @Inject
    private LockablePath lockablePath;


    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Future<Void> syncToSrcRepo(String id,
            String firingId, SyncJobDetail jobDetail) {
        log.debug("running sync to repo job for id: {}", id);

        Path workspace = null;
        try {
            workspace = Files.createDirectories(
                    Paths.get(repoCacheRoot.toString(), id));
            lockablePath.checkoutPath(workspace);
            ZanataSyncService zanataSyncService =
                    new ZanataSyncServiceImpl(jobDetail);
            RepoSyncService repoSyncService =
                    getRepoFor(jobDetail.getSrcRepoType());

            repoSyncService.cloneRepo(jobDetail, workspace);
            zanataSyncService.pullFromZanata(workspace);
            repoSyncService
                    .syncTranslationToRepo(jobDetail, workspace);
            jobStatusPublisher.putStatus(firingId,
                    jobDetail.getInitiatedFromHostURL(), JobStatusType.COMPLETED);
        } catch (Exception e) {
            log.error("Failed to sync to source repo", e);
            jobStatusPublisher.putStatus(firingId,
                    jobDetail.getInitiatedFromHostURL(), JobStatusType.ERROR);
        } finally {
            lockablePath.release(workspace);
        }
        return new AsyncResult<>(null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Future<Void> syncToZanata(String id, String firingId,
            SyncJobDetail jobDetail) {
        log.debug("running sync to zanata job for id: {}", id);
        Path workspace = null;
        try {
            workspace = Files.createDirectories(
                    Paths.get(repoCacheRoot.toString(), id));
            ZanataSyncService zanataSyncService =
                    new ZanataSyncServiceImpl(jobDetail);
            RepoSyncService repoSyncService =
                    getRepoFor(jobDetail.getSrcRepoType());

            repoSyncService.cloneRepo(jobDetail, workspace);
            zanataSyncService.pushToZanata(workspace);

            jobStatusPublisher.putStatus(firingId,
                    jobDetail.getInitiatedFromHostURL(), JobStatusType.COMPLETED);
        } catch (Exception e) {
            log.error("Failed to sync to Zanata", e);
            jobStatusPublisher.putStatus(firingId,
                    jobDetail.getInitiatedFromHostURL(), JobStatusType.ERROR);
        } finally {
            lockablePath.release(workspace);
        }
        return new AsyncResult<>(null);
    }

    private RepoSyncService getRepoFor(String repoType) {
        for (RepoSyncService repoSyncService : repoSyncServices) {
            if (repoSyncService.supportedRepoType().equals(repoType)) {
                return repoSyncService;
            }
        }
        throw new RepoSyncException(
                "can not find service for repo type:" + repoType);
    }

}
