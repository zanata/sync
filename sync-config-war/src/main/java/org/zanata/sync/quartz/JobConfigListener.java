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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.Dependent;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// NOTE: if I use ApplicationScope it will fail (on second time onwards) with WELD-001303: No active contexts for scope type javax.enterprise.context.ApplicationScoped
// Although it's dependent scope but it's used by an application scoped bean so there should only be one instance
@Dependent
public class JobConfigListener implements TriggerListener {
    public static final String LISTENER_NAME = "JobConfigListener";

    private static Map<RunningJobKey, AtomicInteger> runningJobs = Maps.newConcurrentMap();

    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        SyncWorkConfig syncWorkConfig = getJobConfigJob(context);
        JobType jobType = getJobTypeFromContext(context);
        RunningJobKey key = new RunningJobKey(syncWorkConfig.getId(), jobType);

        runningJobs.putIfAbsent(key, new AtomicInteger(0));
        runningJobs.get(key).incrementAndGet();
    }

    private static JobType getJobTypeFromContext(JobExecutionContext context) {
        return (JobType) context.getJobDetail().getJobDataMap().get("jobType");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger,
        JobExecutionContext context) {
        Long id = getJobConfigJob(context).getId();
        JobType jobType = getJobTypeFromContext(context);
        RunningJobKey key = new RunningJobKey(id, jobType);

        if (runningJobs.get(key).get() > 1) {
            log.warn(
                    "job {} execution vetoed: {}. A previous scheduled job is still running",
                    getJobConfigJob(context).getName(), key);
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

        SyncWorkConfig syncWorkConfig = getJobConfigJob(context);
        runningJobs.remove(new RunningJobKey(syncWorkConfig.getId(), getJobTypeFromContext(context)));
    }

    private static SyncWorkConfig getJobConfigJob(JobExecutionContext context) {
        return (SyncWorkConfig) context.getJobDetail().getJobDataMap().get("value");
    }
}
