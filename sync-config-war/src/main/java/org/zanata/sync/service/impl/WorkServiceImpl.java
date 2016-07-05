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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.zanata.sync.dao.Repository;
import org.zanata.sync.dao.SyncWorkConfigDAO;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.service.WorkService;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
@Slf4j
public class WorkServiceImpl implements WorkService {

    @Inject
    private SchedulerService schedulerServiceImpl;

    @Inject
    private Repository<SyncWorkConfig, Long> syncWorkConfigRepository;

    @Override
    public void deleteWork(Long id) {

        try {
            if (checkWorkExist(id)) {
                schedulerServiceImpl.deleteJob(id, JobType.REPO_SYNC);
                schedulerServiceImpl.deleteJob(id, JobType.SERVER_SYNC);
                syncWorkConfigRepository.delete(id);
            }
        } catch (SchedulerException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void updateOrPersist(SyncWorkConfig syncWorkConfig) {
        syncWorkConfigRepository.persist(syncWorkConfig);
    }

    private boolean checkWorkExist(Long id) {
        return syncWorkConfigRepository.load(id).isPresent();
    }
}
