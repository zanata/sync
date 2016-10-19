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

import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.ZanataWebHookEvent;
import org.zanata.sync.events.JobProgressEvent;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.events.JobStartedEvent;
import org.zanata.sync.jobs.RemoteJobExecutor;
import org.zanata.sync.common.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.service.WebHookService;
import org.zanata.sync.util.AutoCloseableDependentProvider;

import static org.zanata.sync.util.AutoCloseableDependentProvider.forBean;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class WebHookServiceImpl implements WebHookService {
    private static final Logger log =
            LoggerFactory.getLogger(WebHookServiceImpl.class);
    private static final int MAX_LENGTH = 254;

    @Inject
    private Event<JobStartedEvent> jobStartedEvent;

    @Inject
    private Event<JobRunCompletedEvent> jobRunCompletedEvent;

    @Inject
    private Event<JobProgressEvent> jobProgressEvent;

    @Override
    public void processZanataWebHook(SyncWorkConfig config,
            ZanataWebHookEvent event) {
        // validate stuff
        String zanataUsernameInConfig = config.getZanataAccount().getUsername();
        if (!zanataUsernameInConfig.equals(event.getUsername())) {
            log.warn(
                    "the zanata username in config [{}] is not the same as the event trigger [{}], aborting webhook event",
                    zanataUsernameInConfig, event.getUsername());
            return;
        }

        Date startTime = new Date();
        long timestamp = startTime.getTime();

        String firingId = eventToFireId(event, timestamp);
        Long configId = config.getId();
        JobStartedEvent startedEvent = new JobStartedEvent(
                firingId,
                configId, JobType.REPO_SYNC, startTime,
                JobStatusType.STARTED, JobType.REPO_SYNC.toJobKey(
                configId));

        jobStartedEvent.fire(startedEvent);

        try (AutoCloseableDependentProvider<RemoteJobExecutor> provider =
                forBean(RemoteJobExecutor.class)) {
            RemoteJobExecutor jobExecutor = provider.getBean();
            try {
                jobExecutor.executeJob(firingId, config, JobType.REPO_SYNC, event.getLocaleId());

                JobProgressEvent progressEvent = JobProgressEvent.running(
                        firingId, configId, null);
                jobProgressEvent.fire(progressEvent);
            } catch (RuntimeException e) {
                log.error("error executing job remotely", e);
                JobRunCompletedEvent completedEvent =
                        JobRunCompletedEvent.endedInError(firingId,
                                configId,
                                new Date().getTime() - timestamp,
                                startTime, JobType.REPO_SYNC);
                jobRunCompletedEvent.fire(completedEvent);
            }

        } catch (Exception e) {
            log.error("error getting RemoteJobExecutor", e);
        }
    }

    private static String eventToFireId(ZanataWebHookEvent event,
            long timestamp) {
        String eventInfo =
                String.format("%d-%s-%s-%s", timestamp, event.getUsername(),
                        event.getProjectSlug(), event.getLocaleId());
        if (eventInfo.length() > MAX_LENGTH) {
            log.info("{} generated fireId is too long and will be truncated",
                    event);
            return eventInfo.substring(0, MAX_LENGTH);
        }
        return eventInfo;
    }
}
