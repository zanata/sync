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
package org.zanata.sync.jobs;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.HostURL;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;

/**
 *
 * This class abstracts the communication between this server and the job server.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class RemoteJobExecutor {
    private static final Logger log =
            LoggerFactory.getLogger(RemoteJobExecutor.class);
    private static final String JOB_SERVER_URL =
            System.getProperty("jobs.server", "http://localhost:8080/jobs");

    private Client client;
    private String hostURL;

    @Inject
    public RemoteJobExecutor(Client client, @HostURL String hostURL) {
        this.client = client;
        this.hostURL = hostURL;
    }

    public void executeJob(String fireInstanceId, SyncWorkConfig workConfig,
            JobType jobType, String localeId) {
        SyncJobDetail jobDetail = SyncJobDetail.Builder.builder()
                .setSrcRepoType(workConfig.getRepoAccount().getRepoType())
                .setSrcRepoUrl(workConfig.getSrcRepoUrl())
                .setSrcRepoBranch(workConfig.getSrcRepoBranch())
                .setSrcRepoSecret(workConfig.getRepoAccount().getSecret())
                .setSrcRepoUsername(workConfig.getRepoAccount().getUsername())
                .setZanataUrl(workConfig.getZanataAccount().getServer())
                .setZanataUsername(workConfig.getZanataAccount().getUsername())
                .setZanataSecret(workConfig.getZanataAccount().getSecret())
                .setSyncToZanataOption(workConfig.getSyncToZanataOption())
                .setLocaleId(localeId)
                .setProjectConfigs(workConfig.getProjectConfigs())
                .setInitiatedFromHostURL(hostURL)
                .build();

        log.debug("about to execute job remotely with: {}", jobDetail);
        Response response;
        switch (jobType) {
            case REPO_SYNC:
                response = client.target(JOB_SERVER_URL)
                        .path("api").path("job").path("2repo").path("start")
                        .path(workConfig.getId().toString())
                        .queryParam("firingId", fireInstanceId)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .post(Entity.entity(jobDetail, MediaType.APPLICATION_JSON_TYPE));
                break;
            case SERVER_SYNC:
                response = client.target(JOB_SERVER_URL)
                        .path("api").path("job").path("2zanata").path("start")
                        .path(workConfig.getId().toString())
                        .queryParam("firingId", fireInstanceId)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .post(Entity.entity(jobDetail, MediaType.APPLICATION_JSON_TYPE));
                break;
            default:
                throw new IllegalStateException("impossible. Unknown job type:" + jobType);
        }
        log.info("remote job executed result: {}", response.getStatusInfo());
        if (response.getStatus() > 300) {
            Object entity = response.getEntity();
            String message = entity != null ? entity.toString()
                    : "remote job returned status:" + response.getStatusInfo();
            throw new RuntimeException(message);
        }
    }
}
