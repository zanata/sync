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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.zanata.sync.dao.SyncWorkConfigDAO;
import org.zanata.sync.dto.WorkSummary;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.model.ZanataAccount;
import org.zanata.sync.service.AccountService;
import org.zanata.sync.service.JobStatusService;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.service.WorkService;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
@Slf4j
public class WorkServiceImpl implements WorkService {

    @Inject
    private SchedulerService schedulerServiceImpl;

    @Inject
    private AccountService accountService;

    @Inject
    private SyncWorkConfigDAO syncWorkConfigDAO;

    @Inject
    private JobStatusService jobStatusService;

    @TransactionAttribute
    @Override
    public void deleteWork(Long id) {

        try {
            if (checkWorkExist(id)) {
                schedulerServiceImpl.deleteJob(id, JobType.REPO_SYNC);
                schedulerServiceImpl.deleteJob(id, JobType.SERVER_SYNC);
                syncWorkConfigDAO.deleteById(id);
            }
        } catch (SchedulerException e) {
            throw Throwables.propagate(e);
        }
    }

    @TransactionAttribute
    @Override
    public void updateOrPersist(SyncWorkConfig syncWorkConfig) {
        syncWorkConfigDAO.persist(syncWorkConfig);
    }

    private boolean checkWorkExist(Long id) {
        return syncWorkConfigDAO.getById(id) != null;
    }

    @Override
    public List<SyncWorkConfig> getAll() {
        return syncWorkConfigDAO.getAll();
    }

    @Override
    public Optional<SyncWorkConfig> load(Long configId) {
        return Optional.ofNullable(syncWorkConfigDAO.getById(configId));
    }

    @Override
    public SyncWorkConfig getById(Long configId) throws WorkNotFoundException {
        Optional<SyncWorkConfig> syncWorkConfig =
                load(configId);
        if (!syncWorkConfig.isPresent()) {
            throw new WorkNotFoundException("id not found:" + configId);
        }
        return syncWorkConfig.get();
    }

    @Override
    public List<WorkSummary> getWorkFor(String username,
            String zanataServer) {
        List<SyncWorkConfig> syncWorkConfigs =
                syncWorkConfigDAO
                        .getByZanataServerAndUsername(zanataServer, username);

        return syncWorkConfigs.stream()
                .map(config -> WorkSummary.toWorkSummary(config,
                        jobStatusService.getLatestJobStatus(config, JobType.REPO_SYNC),
                        jobStatusService.getLatestJobStatus(config,
                                JobType.SERVER_SYNC)))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkSummary> getWorkForCurrentUser() {
        ZanataAccount zanataAccount =
                accountService.getZanataAccountForCurrentUser();
        return getWorkFor(zanataAccount.getUsername(), zanataAccount.getServer());
    }
}
