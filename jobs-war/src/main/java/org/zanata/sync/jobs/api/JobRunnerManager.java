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
package org.zanata.sync.jobs.api;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.Either;
import org.zanata.sync.jobs.ejb.JobRunner;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
@LocalBean
public class JobRunnerManager {
    private static final Logger log =
            LoggerFactory.getLogger(JobRunnerManager.class);
    private Cache<String, Future<Response>> runningJobs = CacheBuilder.newBuilder().build();

    @Resource
    private TimerService timerService;

    @Inject
    private JobStatusPublisher jobStatusPublisher;

    @PostConstruct
    public void init() {
        // in milliseconds
        // TODO make these value configurable via system property
        long initialDuration = 1000;
        long intervalDuration = 5000;
        boolean persistent = true;
        TimerConfig timerConfig = new TimerConfig("JobRunnerManager",
                persistent);
        timerService.createIntervalTimer(
                initialDuration, intervalDuration,
                timerConfig);
        log.info("========= created timer for {}", timerConfig.getInfo());
    }

    @Timeout
    public void onTimeout(Timer timer) {
        if (log.isDebugEnabled()) {
            log.debug("running {}. Next timeout {} - remaining {}", timer.getInfo(),
                    timer.getNextTimeout(), timer.getTimeRemaining());
        }
        Set<String> doneJobs = Sets.newLinkedHashSet();
        runningJobs.asMap().forEach((jobId, future) -> {
            if (future.isDone()) {
                doneJobs.add(jobId);
            }
        });
        // TODO post back status
        for (String doneJob : doneJobs) {
            Future<Response> responseFuture = runningJobs.getIfPresent(doneJob);
            if (responseFuture != null) {
                try {
                    Response response = responseFuture.get();
                    if (response.getStatus() ==
                            Response.Status.OK.getStatusCode()) {
                        jobStatusPublisher.publishJobStatusSuccess(doneJob);
                    } else {
                        log.debug("job response is not ok: {}",
                                response.getStatus());
                        jobStatusPublisher.publishJobStatusError(doneJob);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("exception getting future result", e);
                    jobStatusPublisher.publishJobStatusError(doneJob);
                }
            }
        }
        runningJobs.invalidateAll(doneJobs);
    }

    @EJB
    private JobRunner jobRunner;

    public void syncToZanata(
            Either<RepoSyncService, Response> srcRepoPlugin,
            Either<ZanataSyncService, Response> zanataSyncService,
            String id) {
        Future<Response> responseFuture =
                jobRunner.syncToZanata(srcRepoPlugin, zanataSyncService, id);
        runningJobs.put(id, responseFuture);
    }

    public void syncToSourceRepo(String id,
            Either<RepoSyncService, Response> srcRepoPlugin,
            Either<ZanataSyncService, Response> zanataSyncService) {
        Future<Response> responseFuture =
                jobRunner.syncToSrcRepo(id, srcRepoPlugin, zanataSyncService);
        runningJobs.put(id, responseFuture);
    }
}
