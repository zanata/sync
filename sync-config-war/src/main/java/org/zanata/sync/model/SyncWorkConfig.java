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
package org.zanata.sync.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.util.CronType;
import com.google.common.base.MoreObjects;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Table(name = "Sync_Work_Config_table")
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(
                name = SyncWorkConfig.FIND_BY_ZANATA_ACCOUNT_QUERY,
                query = "from SyncWorkConfig where zanataAccount.username = :username and zanataAccount.server = :server"
        ),
        @NamedQuery(
                name = SyncWorkConfig.GET_ALL_QUERY,
                query = "from SyncWorkConfig order by createdDate"
        )
})
public class SyncWorkConfig {
    public static final String FIND_BY_ZANATA_ACCOUNT_QUERY =
            "FindByZanataAccountQuery";
    public static final String GET_ALL_QUERY = "GetAllQuery";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    private CronType syncToZanataCron;
    private CronType syncToRepoCron;

    @Enumerated(EnumType.STRING)
    private SyncOption syncToZanataOption;


    private boolean syncToServerEnabled = true;

    private boolean syncToRepoEnabled = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "zanataAccount")
    private ZanataAccount zanataAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repoAccount")
    private RepoAccount repoAccount;

    private String srcRepoUrl;
    private String srcRepoBranch;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();

    @OneToMany(mappedBy = "workConfig")
    private List<JobStatus> jobStatusHistory = Collections.emptyList();

    public SyncWorkConfig() {
    }

    // TODO may not need the id parameter
    public SyncWorkConfig(Long id, String name, String description,
            CronType syncToZanataCron, CronType syncToRepoCron,
            SyncOption syncToZanataOption,
            boolean syncToServerEnabled, boolean syncToRepoEnabled,
            String srcRepoUrl,
            String srcRepoBranch, ZanataAccount zanataAccount,
            RepoAccount repoAccount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncToZanataOption = syncToZanataOption;
        this.syncToServerEnabled = syncToServerEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
        this.syncToZanataCron = syncToZanataCron;
        this.syncToRepoCron = syncToRepoCron;
        this.srcRepoUrl = srcRepoUrl;
        this.srcRepoBranch = srcRepoBranch;
        this.zanataAccount = zanataAccount;
        this.repoAccount = repoAccount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
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

    public ZanataAccount getZanataAccount() {
        return zanataAccount;
    }

    public String getSrcRepoUrl() {
        return srcRepoUrl;
    }

    public String getSrcRepoBranch() {
        return srcRepoBranch;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public List<JobStatus> getJobStatusHistory() {
        return jobStatusHistory;
    }

    public RepoAccount getRepoAccount() {
        return repoAccount;
    }
}
