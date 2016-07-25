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
package org.zanata.sync.dao;

import java.util.List;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;

import org.zanata.sync.model.ZanataAccount;
import com.google.common.base.Joiner;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class ZanataAccountDAOImpl implements ZanataAccountDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void persist(ZanataAccount account) {
        entityManager.persist(account);
    }

    @Nullable
    @Override
    public ZanataAccount getByLocalUsername(String localUsername) {
        List<ZanataAccount> resultList = entityManager.createNamedQuery(
                ZanataAccount.FIND_BY_LOCAL_USERNAME_QUERY,
                ZanataAccount.class)
                .setParameter("username", localUsername)
                .setFirstResult(0)
                .setMaxResults(2)
                .getResultList();
        return returnUniqueResult(resultList, localUsername);
    }

    private static ZanataAccount returnUniqueResult(
            List<ZanataAccount> resultList, String... searchCriteria) {
        if (resultList.size() == 1) {
            return resultList.get(0);
        } else if (resultList.size() > 1) {
            throw new NonUniqueResultException(
                    Joiner.on(" + ").join(searchCriteria) +
                            " is not unique. Did you forget to create unique index?");
        }
        return null;
    }

    @Nullable
    @Override
    public ZanataAccount getByUsernameAndServer(String username,
            String server) {
        List<ZanataAccount> resultList = entityManager
                .createNamedQuery(
                        ZanataAccount.FIND_BY_USERNAME_SERVER_QUERY,
                        ZanataAccount.class)
                .setParameter("username", username)
                .setParameter("server", server)
                .setFirstResult(0)
                .setMaxResults(2)
                .getResultList();
        return returnUniqueResult(resultList, username, server);
    }
}
