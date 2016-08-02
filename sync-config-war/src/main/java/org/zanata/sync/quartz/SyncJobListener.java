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
package org.zanata.sync.quartz;

import javax.enterprise.context.Dependent;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.events.JobProgressEvent;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.events.JobStartedEvent;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
// NOTE: if I use ApplicationScope it will fail (on second time onwards) with WELD-001303: No active contexts for scope type javax.enterprise.context.ApplicationScoped
// Although it's dependent scope but it's used by an application scoped bean so there should only be one instance
@Dependent
public class SyncJobListener implements JobListener {
    private static final Logger log =
            LoggerFactory.getLogger(SyncJobListener.class);
    @Override
    public String getName() {
        return "Sync Job Listener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        SyncJobDataMap syncJobDataMap = SyncJobDataMap.fromContext(context);
        Long configId = syncJobDataMap.getConfigId();
        JobType jobType = syncJobDataMap.getJobType();
        log.debug("=== job to be executed: {} {}", configId, jobType);

        JobStartedEvent event = new JobStartedEvent(context.getFireInstanceId(),
                configId, jobType, context.getFireTime(),
                JobStatusType.STARTED, context.getJobDetail().getKey());
        fireCDIEvent(event);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException) {

        SyncJobDataMap syncJobDataMap = SyncJobDataMap.fromContext(context);
        Long configId = syncJobDataMap.getConfigId();
        JobType jobType = syncJobDataMap.getJobType();
        log.debug("=== job was executed for: {} {}", configId, jobType);

        if (jobException != null) {
            JobRunCompletedEvent completedEvent =
                    JobRunCompletedEvent.endedInError(context.getFireInstanceId(),
                            configId,
                            context.getJobRunTime(),
                            context.getFireTime(), jobType);
            fireCDIEvent(completedEvent);
        } else {
            JobProgressEvent event = JobProgressEvent.running(
                    context.getFireInstanceId(), configId,
                    context.getNextFireTime());
            fireCDIEvent(event);
        }
    }

    private static void fireCDIEvent(Object event) {
        BeanManagerProvider.getInstance().getBeanManager()
                .fireEvent(event);
    }
}
