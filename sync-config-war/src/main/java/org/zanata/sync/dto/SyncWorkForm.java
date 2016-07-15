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
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.util.CronType;
import org.zanata.sync.validation.SupportedRepo;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SyncWorkForm implements Serializable {
    public final static String repoSettingsPrefix = "sourceRepoSettings.";

    private Long id;

    @Size(min = 5, max = 100)
    @NotEmpty
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull(groups = ZanataSyncGroup.class)
    private CronType syncToZanataCron;

    @NotNull(groups = RepoSyncGroup.class)
    private SyncOption syncOption = SyncOption.SOURCE;

    @NotNull(groups = RepoSyncGroup.class)
    private CronType syncToRepoCron;

    // TODO change this to srcRepoType instead
    @SupportedRepo(groups = RepoSyncGroup.class)
    @NotEmpty(groups = RepoSyncGroup.class)
    @Size(max = 50, groups = RepoSyncGroup.class)
    private String srcRepoPluginName;

    @Size(max = 16)
    private String encryptionKey;

    private Map<String, String> srcRepoPluginConfig = new HashMap<>();


    private boolean syncToZanataEnabled = true;

    private boolean syncToRepoEnabled = true;

    public SyncWorkForm(String name, String description,
            CronType syncToZanataCron,
            SyncOption syncOption, CronType syncToRepoCron,
            String srcRepoPluginName, String encryptionKey,
            Map<String, String> srcRepoPluginConfig,
            boolean syncToZanataEnabled,
            boolean syncToRepoEnabled) {
        this.name = name;
        this.description = description;
        this.syncToZanataCron = syncToZanataCron;
        this.syncOption = syncOption;
        this.syncToRepoCron = syncToRepoCron;
        this.srcRepoPluginName = srcRepoPluginName;
        this.encryptionKey = encryptionKey;
        this.srcRepoPluginConfig = srcRepoPluginConfig;
        this.syncToZanataEnabled = syncToZanataEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
    }

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

    public String getSrcRepoPluginName() {
        return srcRepoPluginName;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public Map<String, String> getSrcRepoPluginConfig() {
        return srcRepoPluginConfig;
    }

    public boolean isSyncToZanataEnabled() {
        return syncToZanataEnabled;
    }

    public boolean isSyncToRepoEnabled() {
        return syncToRepoEnabled;
    }
}
