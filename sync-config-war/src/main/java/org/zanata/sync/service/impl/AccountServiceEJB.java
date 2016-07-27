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
package org.zanata.sync.service.impl;

import java.util.Optional;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dao.ZanataAccountDAO;
import org.zanata.sync.dto.LocalAccount;
import org.zanata.sync.dto.RepoAccountDto;
import org.zanata.sync.dto.UserAccount;
import org.zanata.sync.dto.ZanataUserAccount;
import org.zanata.sync.model.RepoAccount;
import org.zanata.sync.model.ZanataAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.AccountService;
import com.google.common.base.Preconditions;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
public class AccountServiceEJB implements AccountService {
    private static final Logger log =
            LoggerFactory.getLogger(AccountServiceEJB.class);
    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private ZanataAccountDAO zanataAccountDAO;

    @Override
    @TransactionAttribute
    public void saveAuthenticatedAccount() {
        UserAccount account = securityTokens.getAccount();
        ZanataAccount entity;
        if (account instanceof ZanataUserAccount) {
            ZanataUserAccount zanataAccount = (ZanataUserAccount) account;
            log.debug("about to save a zanata user account: {}", zanataAccount);
            entity = zanataAccountDAO
                    .getByUsernameAndServer(zanataAccount.getUsername(),
                            zanataAccount.getZanataServer());
            if (entity == null) {
                entity = new ZanataAccount(zanataAccount.getUsername(),
                        zanataAccount.getZanataServer(),
                        zanataAccount.getApiKey());
            }
        } else {
            log.debug("about to save a local user account: {}", account);
            entity =
                    zanataAccountDAO.getByLocalUsername(account.getUsername());
            if (entity == null) {
                entity = new ZanataAccount(account.getUsername());
            }
        }

        if (entity.getId() != null) {
            log.debug(
                    "user's zanata account has already been persisted. No op.");
        } else {
            zanataAccountDAO.persist(entity);
        }
    }

    @Override
    @TransactionAttribute
    public ZanataAccount updateZanataAccount(ZanataUserAccount zanataUserAccount) {
        UserAccount account = securityTokens.getAccount();
        ZanataAccount entity;
        if (account instanceof LocalAccount) {
            LocalAccount localAccount = (LocalAccount) account;
            entity = zanataAccountDAO.getByLocalUsername(localAccount.getUsername());
            Preconditions.checkState(entity != null, "Can not find local account username in the system.");
            entity.updateZanataAccount(zanataUserAccount);
            return entity;
        } else {
            // we don't support update Zanata Account from OAuth login
            throw new IllegalStateException("Can not update Zanata Account when logged in using OAuth");
        }
    }

    @Override
    public ZanataAccount getZanataAccountForCurrentUser() {
        UserAccount account = securityTokens.getAccount();
        ZanataAccount result;
        if (account instanceof ZanataUserAccount) {
            result = zanataAccountDAO
                    .getByUsernameAndServer(account.getUsername(),
                            ((ZanataUserAccount) account).getZanataServer());
        } else {
            result = zanataAccountDAO
                    .getByLocalUsername(account.getUsername());

        }
        Preconditions.checkState(result != null, "Can not get zanata account for current user");
        return result;
    }

    @TransactionAttribute
    @Override
    public RepoAccount saveRepoAccountForCurrentUser(RepoAccountDto dto) {
        ZanataAccount zanataAccount =
                getZanataAccountForCurrentUser();
        Optional<RepoAccount> existing =
                zanataAccount.getRepoAccounts().stream()
                        .filter(repo -> repo.getId().equals(dto.getId()))
                        .findAny();
        if (existing.isPresent()) {
            RepoAccount repoAccount = existing.get();
            repoAccount.update(dto.getRepoType(), dto.getRepoHostname(),
                    dto.getUsername(), dto.getSecret());
            return repoAccount;
        } else {
            RepoAccount newAccount =
                    new RepoAccount(dto.getRepoType(), dto.getRepoHostname(),
                            dto.getUsername(), dto.getSecret(), zanataAccount);
            zanataAccount.getRepoAccounts().add(newAccount);
            return newAccount;
        }
    }
}
