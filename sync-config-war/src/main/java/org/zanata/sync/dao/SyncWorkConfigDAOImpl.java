/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.zanata.sync.model.SyncWorkConfig;


/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class SyncWorkConfigDAOImpl implements SyncWorkConfigDAO {

    @Inject
    private SyncWorkConfigSerializer serializer;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void persist(SyncWorkConfig config) {
        entityManager.persist(config);
    }

    @Override
    public List<SyncWorkConfig> getByZanataServerAndUsername(String server,
            String username) {
        return entityManager
                .createNamedQuery(SyncWorkConfig.FIND_BY_ZANATA_ACCOUNT_QUERY,
                        SyncWorkConfig.class)
                .setParameter("server", server)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public void deleteById(Long id) {
        SyncWorkConfig syncWorkConfig =
                entityManager.find(SyncWorkConfig.class, id);
        if (syncWorkConfig != null) {
            entityManager.remove(syncWorkConfig);
        }
    }

    @Override
    public SyncWorkConfig getById(Long id) {
        return entityManager.find(SyncWorkConfig.class, id);
    }

    @Override
    public List<SyncWorkConfig> getAll() {
        return entityManager.createNamedQuery(SyncWorkConfig.GET_ALL_QUERY,
                SyncWorkConfig.class)
                .getResultList();
    }
}
