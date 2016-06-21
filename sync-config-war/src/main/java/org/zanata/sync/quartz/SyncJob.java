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

import javax.ws.rs.client.Client;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.zanata.sync.jobs.RemoteJobExecutor;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.util.AutoCloseableDependentProvider;
import lombok.extern.slf4j.Slf4j;

import static org.zanata.sync.util.AutoCloseableDependentProvider.forBean;

/**
 * This is the class that represents the cron job.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class SyncJob implements InterruptableJob {

    @Override
    public final void execute(JobExecutionContext context)
            throws JobExecutionException {
        try (AutoCloseableDependentProvider<Client> provider = forBean(
                Client.class)) {
            SyncJobDataMap syncJobDataMap = SyncJobDataMap.fromContext(context);
            SyncWorkConfig syncWorkConfig = syncJobDataMap.getWorkConfig();
            JobType jobType = syncJobDataMap.getJobType();

            Client client = provider.getBean();
            new RemoteJobExecutor(client)
                    .executeJob(syncWorkConfig.getId(), syncWorkConfig,
                            jobType);

        } catch (Exception e) {
            log.error("error happened during job run", e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public final void interrupt() throws UnableToInterruptJobException {
        // TODO we need to interrupt remote job execution
        throw new UnableToInterruptJobException("not supported yet");
//        interrupted = true;
//        Thread.currentThread().interrupt();
//        updateProgress(syncWorkConfig.getId(), 0, "job interrupted",
//            JobStatusType.INTERRUPTED);
    }

    /*private void updateProgress(Long id, double completePercent,
            String description, JobStatusType jobStatusType) {
        JobProgressEvent event =
            new JobProgressEvent(id, jobType, completePercent,
                description, jobStatusType);
        BeanManagerProvider.getInstance().getBeanManager().fireEvent(event);
    }*/

}
