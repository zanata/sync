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
package org.zanata.sync.dto;

import java.io.Serializable;

import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class WorkSummary implements Serializable {
    private Long id;
    private String name;
    private String description;
    private JobSummary syncToRepoJob;
    private JobSummary syncToTransServerJob;

    public WorkSummary(Long id, String name, String description,
            JobSummary syncToRepoJob,
            JobSummary syncToTransServerJob) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncToRepoJob = syncToRepoJob;
        this.syncToTransServerJob = syncToTransServerJob;
    }

    public WorkSummary() {
    }

    public static WorkSummary toWorkSummary(
            SyncWorkConfig syncWorkConfig, JobStatus syncToRepoJobStatus,
            JobStatus syncToServerJobStatus) {

        JobSummary syncToRepoJob =
                new JobSummary(
                        JobType.REPO_SYNC.toJobKey(syncWorkConfig.getId())
                                .toString(),
                        syncWorkConfig.getId(),
                        syncWorkConfig.isSyncToRepoEnabled(),
                        JobType.REPO_SYNC,
                        JobRunStatus.fromEntity(syncToRepoJobStatus,
                                syncWorkConfig.getId(), JobType.REPO_SYNC));

        JobSummary syncToServerJob =
                new JobSummary(
                        JobType.SERVER_SYNC.toJobKey(syncWorkConfig.getId())
                                .toString(),
                        syncWorkConfig.getId(),
                        syncWorkConfig.isSyncToServerEnabled(),
                        JobType.SERVER_SYNC,
                        JobRunStatus.fromEntity(syncToServerJobStatus,
                                syncWorkConfig.getId(), JobType.SERVER_SYNC));

        return new WorkSummary(syncWorkConfig.getId(),
                syncWorkConfig.getName(),
                syncWorkConfig.getDescription(),
                syncToRepoJob,
                syncToServerJob);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public JobSummary getSyncToRepoJob() {
        return syncToRepoJob;
    }

    public JobSummary getSyncToTransServerJob() {
        return syncToTransServerJob;
    }
}
