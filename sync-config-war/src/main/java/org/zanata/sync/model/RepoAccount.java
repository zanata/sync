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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.zanata.sync.util.EncryptionUtil;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class RepoAccount implements HasSensitiveFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String repoType;
    @NotNull
    private String repoHostname;
    private String username;
    private String secret;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "zanataAccount")
    private ZanataAccount zanataAccount;

    public String getRepoHostname() {
        return repoHostname;
    }

    public String getUsername() {
        return username;
    }

    public String getSecret() {
        return secret;
    }

    public ZanataAccount getZanataAccount() {
        return zanataAccount;
    }

    public String getRepoType() {
        return repoType;
    }

    public Long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public RepoAccount() {
    }

    public RepoAccount(String repoType, String repoHostname,
            String username, String secret,
            ZanataAccount zanataAccount) {
        this.repoType = repoType;
        this.repoHostname = repoHostname;
        this.username = username;
        this.secret = secret;
        this.zanataAccount = zanataAccount;
    }

    public void update(String repoType, String repoHostname, String username,
            String secret) {
        this.repoType = repoType;
        this.repoHostname = repoHostname;
        this.username = username;
        this.secret = secret;
    }

    @PrePersist
    @PreUpdate
    public void preSave() {
        encryptValues();
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void postLoad() {
        decryptValues();
    }

    @Override
    public void encryptValues() {
        if (!Strings.isNullOrEmpty(secret)) {
            EncryptionUtil encryptionUtil =
                    BeanProvider.getContextualReference(EncryptionUtil.class);
            secret = encryptionUtil.encrypt(secret);
        }
    }

    @Override
    public void decryptValues() {
        if (!Strings.isNullOrEmpty(secret)) {
            EncryptionUtil encryptionUtil =
                    BeanProvider.getContextualReference(EncryptionUtil.class);
            secret = encryptionUtil.decrypt(secret);
        }
    }
}
