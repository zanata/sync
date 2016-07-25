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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.util.CronType;
import org.zanata.sync.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class WorkDetail {
    private Long id;
    private String name;
    private String description;

    private CronType syncToZanataCron;
    private CronType syncToRepoCron;

    private SyncOption syncToZanataOption;

    private String srcRepoPluginName;

    private boolean syncToServerEnabled = true;
    private boolean syncToRepoEnabled = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.ISO_JS_DATE_FMT)
    private Date createdDate;
    private List<JobRunStatus> jobRunHistory;

    private String srcRepoUrl;
    private String srcRepoUsername;
    private String srcRepoSecret;
    private String srcRepoBranch;

    @SuppressWarnings("unused")
    public WorkDetail() {
    }

    private WorkDetail(Long id, String name, String description,
            CronType syncToZanataCron, CronType syncToRepoCron,
            SyncOption syncToZanataOption,
            String srcRepoPluginName,
            boolean syncToServerEnabled,
            boolean syncToRepoEnabled, Date createdDate,
            List<JobRunStatus> jobRunHistory, String srcRepoUrl,
            String srcRepoUsername, String srcRepoSecret, String srcRepoBranch) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncToZanataCron = syncToZanataCron;
        this.syncToRepoCron = syncToRepoCron;
        this.syncToZanataOption = syncToZanataOption;
        this.srcRepoPluginName = srcRepoPluginName;
        this.syncToServerEnabled = syncToServerEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
        this.createdDate = createdDate;
        this.jobRunHistory = jobRunHistory;
        this.srcRepoUrl = srcRepoUrl;
        this.srcRepoUsername = srcRepoUsername;
        this.srcRepoSecret = srcRepoSecret;
        this.srcRepoBranch = srcRepoBranch;
    }

    public static WorkDetail fromEntity(SyncWorkConfig workConfig,
            List<JobStatus> jobStatusList) {
        List<JobRunStatus> statuses = jobStatusList.stream()
                .map(jobStatus -> JobRunStatus
                        .fromEntity(jobStatus, workConfig.getId(),
                                jobStatus.getJobType()))
                .collect(Collectors.toList());
        return new WorkDetail(workConfig.getId(), workConfig.getName(),
                workConfig.getDescription(), workConfig.getSyncToZanataCron(),
                workConfig.getSyncToRepoCron(),
                workConfig.getSyncToZanataOption(),
                workConfig.getRepoAccount().getRepoType(),
                workConfig.isSyncToServerEnabled(),
                workConfig.isSyncToRepoEnabled(),
                workConfig.getCreatedDate(), statuses,
                workConfig.getSrcRepoUrl(),
                workConfig.getRepoAccount().getUsername(),
                workConfig.getRepoAccount().getSecret(),
                workConfig.getSrcRepoBranch());
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

    public CronType getSyncToZanataCron() {
        return syncToZanataCron;
    }

    public CronType getSyncToRepoCron() {
        return syncToRepoCron;
    }

    public SyncOption getSyncToZanataOption() {
        return syncToZanataOption;
    }

    public boolean isSyncToServerEnabled() {
        return syncToServerEnabled;
    }

    public boolean isSyncToRepoEnabled() {
        return syncToRepoEnabled;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public List<JobRunStatus> getJobRunHistory() {
        return jobRunHistory;
    }

    public String getSrcRepoPluginName() {
        return srcRepoPluginName;
    }

    public String getSrcRepoUrl() {
        return srcRepoUrl;
    }

    public String getSrcRepoUsername() {
        return srcRepoUsername;
    }

    public String getSrcRepoSecret() {
        return srcRepoSecret;
    }

    public String getSrcRepoBranch() {
        return srcRepoBranch;
    }
}
