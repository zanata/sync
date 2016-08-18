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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.util.CronType;
import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SyncWorkForm implements Serializable {

    private Long id;

    @Size(min = 5, max = 100)
    @NotEmpty
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull(groups = ZanataSyncGroup.class)
    private CronType syncToZanataCron;

    @NotNull(groups = ZanataSyncGroup.class)
    private SyncOption syncOption = SyncOption.SOURCE;

    @NotNull(groups = RepoSyncGroup.class)
    private CronType syncToRepoCron;

    @NotNull
    private Long srcRepoAccountId;

    private String zanataWebHookSecret;
    private boolean syncToZanataEnabled = true;

    private boolean syncToRepoEnabled = true;

    @NotEmpty
    private String srcRepoUrl;
    private String srcRepoBranch;

    @VisibleForTesting
    public SyncWorkForm(String name, String description,
            CronType syncToZanataCron,
            SyncOption syncOption, CronType syncToRepoCron,
            String zanataWebHookSecret,
            boolean syncToZanataEnabled,
            boolean syncToRepoEnabled, String srcRepoUrl,
            String srcRepoBranch, Long srcRepoAccountId) {
        this.name = name;
        this.description = description;
        this.syncToZanataCron = syncToZanataCron;
        this.syncOption = syncOption;
        this.syncToRepoCron = syncToRepoCron;
        this.zanataWebHookSecret = zanataWebHookSecret;
        this.syncToZanataEnabled = syncToZanataEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
        this.srcRepoUrl = srcRepoUrl;
        this.srcRepoBranch = srcRepoBranch;
        this.srcRepoAccountId = srcRepoAccountId;
    }

    @SuppressWarnings("unused")
    public SyncWorkForm() {
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

    public SyncOption getSyncOption() {
        return syncOption;
    }

    public CronType getSyncToRepoCron() {
        return syncToRepoCron;
    }

    public boolean isSyncToZanataEnabled() {
        return syncToZanataEnabled;
    }

    public boolean isSyncToRepoEnabled() {
        return syncToRepoEnabled;
    }

    public String getSrcRepoUrl() {
        return srcRepoUrl;
    }

    public Long getSrcRepoAccountId() {
        return srcRepoAccountId;
    }

    public String getSrcRepoBranch() {
        return srcRepoBranch;
    }

    public String getZanataWebHookSecret() {
        return zanataWebHookSecret;
    }
}
