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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkSummary implements Serializable {
    private Long id;
    private String name;
    private String description;
    private JobSummary syncToRepoJob;
    private JobSummary syncToTransServerJob;

    public static WorkSummary toWorkSummary(
            SyncWorkConfig syncWorkConfig, JobStatus syncToRepoJobStatus,
            JobStatus syncToServerJobStatus) {

        JobSummary syncToRepoJob =
                new JobSummary(
                        JobType.REPO_SYNC.toJobKey(syncWorkConfig.getId())
                                .toString(), syncWorkConfig.getId(),
                        syncWorkConfig.getName(),
                        syncWorkConfig.getDescription(),
                        JobType.REPO_SYNC,
                        JobRunStatus.fromEntity(syncToRepoJobStatus));

        JobSummary syncToServerJob =
                new JobSummary(
                        JobType.SERVER_SYNC.toJobKey(syncWorkConfig.getId())
                                .toString(), syncWorkConfig.getId(),
                        syncWorkConfig.getName(),
                        syncWorkConfig.getDescription(),
                        JobType.SERVER_SYNC,
                        JobRunStatus.fromEntity(syncToServerJobStatus));

        return new WorkSummary(syncWorkConfig.getId(),
                syncWorkConfig.getName(),
                syncWorkConfig.getDescription(),
                syncToRepoJob,
                syncToServerJob);
    }
}
