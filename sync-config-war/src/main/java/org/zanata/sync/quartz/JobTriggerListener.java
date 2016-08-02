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
package org.zanata.sync.quartz;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.Dependent;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// This is to stop too a particular job runs too frequently (e.g. one is still running while another is scheduled to run
// NOTE: if I use ApplicationScope it will fail (on second time onwards) with WELD-001303: No active contexts for scope type javax.enterprise.context.ApplicationScoped
// Although it's dependent scope but it's used by an application scoped bean so there should only be one instance
@Dependent
public class JobTriggerListener implements TriggerListener {
    private static final String LISTENER_NAME = "JobTriggerListener";

    private static Cache<JobKey, AtomicInteger> runningJobsCache =
            CacheBuilder.newBuilder().build();

    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        JobKey jobKey = trigger.getJobKey();
        try {
            runningJobsCache.get(jobKey, () -> new AtomicInteger(0))
                    .getAndIncrement();
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger,
            JobExecutionContext context) {
        JobKey jobKey = trigger.getJobKey();
        AtomicInteger runCount = runningJobsCache.getIfPresent(jobKey);
        if (runCount != null && runCount.get() > 1) {
            log.warn(
                    "job {} execution vetoed: {}. A previous scheduled job is still running",
                    getJobConfigJob(context), jobKey);
            return true;
        }
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        runningJobsCache.invalidate(trigger.getJobKey());
    }

    private static Long getJobConfigJob(JobExecutionContext context) {
        return SyncJobDataMap.fromContext(context).getConfigId();
    }
}
