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

import java.util.Optional;
import javax.ws.rs.client.Client;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.RemoteJobExecutor;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.service.WorkService;
import org.zanata.sync.util.AutoCloseableDependentProvider;

import static org.zanata.sync.util.AutoCloseableDependentProvider.forBean;

/**
 * This is the class that represents the cron job.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SyncJob implements InterruptableJob {
    private static final Logger log = LoggerFactory.getLogger(SyncJob.class);

    @Override
    public final void execute(JobExecutionContext context)
            throws JobExecutionException {
        try (AutoCloseableDependentProvider<Client> provider = forBean(
                Client.class)) {
            SyncJobDataMap syncJobDataMap = SyncJobDataMap.fromContext(context);
            Long configId = syncJobDataMap.getConfigId();
            JobType jobType = syncJobDataMap.getJobType();

            Client client = provider.getBean();
            WorkService workService =
                    BeanProvider.getContextualReference(WorkService.class);
            Optional<SyncWorkConfig> workConfig = workService.load(configId);
            if (workConfig.isPresent()) {
                new RemoteJobExecutor(client)
                        .executeJob(context.getFireInstanceId(), workConfig.get(),
                                jobType);
            } else {
                log.warn("can not load config for id {}", configId);
            }
        } catch (Exception e) {
            log.error("error happened during job run", e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public final void interrupt() throws UnableToInterruptJobException {
        // TODO we need to interrupt remote job execution
        throw new UnableToInterruptJobException("not supported yet");
    }

}
