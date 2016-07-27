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

import javax.validation.constraints.Size;

import org.zanata.sync.model.RepoAccount;
import org.zanata.sync.validation.SupportedRepo;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RepoAccountDto {
    private Long id;
    private String username;

    @Size(min = 3, max = 255)
    private String repoHostname;

    @SupportedRepo
    private String repoType;
    private String secret;

    public RepoAccountDto() {
    }

    private RepoAccountDto(Long id, String username, String repoHostname,
            String repoType, String secret) {
        this.id = id;
        this.username = username;
        this.repoHostname = repoHostname;
        this.repoType = repoType;
        this.secret = secret;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRepoHostname() {
        return repoHostname;
    }

    public String getRepoType() {
        return repoType;
    }

    public String getSecret() {
        return secret;
    }

    public static RepoAccountDto fromEntity(RepoAccount repoAccount) {
        return new RepoAccountDto(repoAccount.getId(),
                repoAccount.getUsername(), repoAccount.getRepoHostname(),
                repoAccount.getRepoType(), repoAccount.getSecret());
    }
}
