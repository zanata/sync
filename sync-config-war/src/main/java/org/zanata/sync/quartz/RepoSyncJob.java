/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.sync.quartz;

import java.io.File;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.plugin.TranslationServerExecutor;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RepoSyncJob extends SyncJob {
    private static final Logger log =
        LoggerFactory.getLogger(RepoSyncJob.class);

    @Override
    protected JobType getJobType() {
        return JobType.REPO_SYNC;
    }

    @Override
    protected void doSync(RepoExecutor srcExecutor,
        TranslationServerExecutor transServerExecutor)
        throws JobExecutionException {
        if (srcExecutor == null || transServerExecutor == null) {
            log.info("No plugin in job. Skipping. {}",
                syncWorkConfig.toString());
            return;
        }
        if (syncWorkConfig.getSyncToRepoConfig() == null) {
            log.info("SyncToRepo is disabled. Skipping."
                    + syncWorkConfig.toString());
            return;
        }

        try {
            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 0,
                "Sync to repository starts", JobStatusType.RUNNING);
            File destDir = getDestDirectory(syncWorkConfig.getId().toString());

            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 20,
                "Cloning repository to " + destDir, JobStatusType.RUNNING);
            srcExecutor.cloneRepo(destDir);

            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 40,
                "Pulling files from translation server to " + destDir,
                JobStatusType.RUNNING);

            transServerExecutor.pullFromServer(destDir,
                syncWorkConfig.getSyncToRepoConfig().getOption());

            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 60,
                "Commits to repository from " + destDir, JobStatusType.RUNNING);
            srcExecutor.pushToRepo(destDir,
                syncWorkConfig.getSyncToRepoConfig().getOption());

            updateProgress(syncWorkConfig.getId(), 80,
                "Cleaning directory: " + destDir, JobStatusType.RUNNING);
        } catch (Exception e) {
            // TODO shouldn't we catch the exception then fire the event and log the job status?
            throw new JobExecutionException(e);
        }
    }
}
