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
public class TransServerSyncJob extends SyncJob {
    private static final Logger log =
            LoggerFactory.getLogger(TransServerSyncJob.class);

    @Override
    protected JobType getJobType() {
        return JobType.SERVER_SYNC;
    }

    @Override
    protected void doSync(RepoExecutor repoExecutor,
            TranslationServerExecutor transServerExecutor)
            throws JobExecutionException {
        if (repoExecutor == null || transServerExecutor == null) {
            log.info("No plugin in job. Skipping." + syncWorkConfig.toString());
            return;
        }
        if (!syncWorkConfig.isSyncToServerEnabled()) {
            log.info("SyncToServer is disabled. Skipping." + syncWorkConfig.toString());
            return;
        }

        try {
            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 0,
                "Sync to server starts", JobStatusType.RUNNING);
            File destDir = getDestDirectory(syncWorkConfig.getId().toString());

            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 25,
                "Cloning repository to " + destDir, JobStatusType.RUNNING);
            repoExecutor.cloneRepo(destDir);

            if (interrupted) {
                return;
            }
            updateProgress(syncWorkConfig.getId(), 50,
                "Pushing files to server from " + destDir,
                JobStatusType.RUNNING);
            transServerExecutor.pushToServer(destDir,
                syncWorkConfig.getSyncToZanataOption());

            updateProgress(syncWorkConfig.getId(), 75,
                "Cleaning directory: " + destDir, JobStatusType.RUNNING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException(e);
        }
    }

}
