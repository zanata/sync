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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.AccountService;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class SyncWorkConfigBuilderImpl implements SyncWorkConfigBuilder {

    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private AccountService accountService;

    @Override
    public SyncWorkConfig buildObject(SyncWorkForm syncWorkForm,
            ZanataAccount zanataAccount, RepoAccount repoAccount) {

        return new SyncWorkConfig(syncWorkForm.getId(),
                syncWorkForm.getName(),
                syncWorkForm.getDescription(),
                syncWorkForm.getSyncToZanataCron(),
                syncWorkForm.getSyncToRepoCron(),
                syncWorkForm.getSyncOption(),
                syncWorkForm.isSyncToZanataEnabled(),
                syncWorkForm.isSyncToRepoEnabled(),
                syncWorkForm.getSrcRepoUrl(),
                syncWorkForm.getSrcRepoBranch(), zanataAccount, repoAccount);
    }

}
