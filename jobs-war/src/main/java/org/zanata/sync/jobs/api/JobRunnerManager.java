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

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
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
import org.zanata.sync.jobs.ejb.JobStatusPublisher;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

/**
 * It's singleton per JVM. It's okay to scale the app as long as each deployment
 * don't share anything in here.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
@LocalBean
public class JobRunnerManager {
    private static final Logger log =
            LoggerFactory.getLogger(JobRunnerManager.class);
    private Cache<String, Future<Response>> runningJobs =
            CacheBuilder.newBuilder().build();

    @Resource
    private TimerService timerService;

    @Inject
    private JobStatusPublisher jobStatusPublisher;

    private ReentrantLock lock = new ReentrantLock();

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
            log.debug("running {}. Next timeout {} - remaining {}",
                    timer.getInfo(),
                    timer.getNextTimeout(), timer.getTimeRemaining());
        }
        boolean acquired = false;
        try {
            acquired = lock.tryLock();
            if (!acquired) {
                // another timer thread is calling this method, quit
                return;
            }
            if (runningJobs.size() == 0) {
                return;
            }
            ImmutableMap.Builder<String, Future<Response>> doneJobsBuilder =
                    ImmutableMap.builder();
            runningJobs.asMap().forEach((jobId, future) -> {
                if (future.isDone()) {
                    doneJobsBuilder.put(jobId, future);
                }
            });
            Map<String, Future<Response>> doneJobs = doneJobsBuilder.build();
            jobStatusPublisher.publish(doneJobs);
            runningJobs.invalidateAll(doneJobs.keySet());
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
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
