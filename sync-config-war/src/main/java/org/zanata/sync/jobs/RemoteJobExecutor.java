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

import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import com.google.common.collect.Maps;

/**
 *
 * This class abstracts the communication between this server and the job server.
 * Payload in the job server expects following fields:
 * <pre>
 *  <ul>
 *      <li>srcRepoUrl</li>
 *      <li>srcRepoUsername</li>
 *      <li>srcRepoSecret</li>
 *      <li>srcRepoBranch</li>
 *      <li>syncToZanataOption=source|trans|both</li>
 *      <li>srcRepoType=git|anything else we may support in the future</li>
 *      <li>zanataUrl</li>
 *      <li>zanataUsername</li>
 *      <li>zanataSecret</li>
 *   </ul>
 * </pre>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class RemoteJobExecutor {
    private static final Logger log =
            LoggerFactory.getLogger(RemoteJobExecutor.class);
    private static final String JOB_SERVER_URL =
            System.getProperty("jobs.server", "http://localhost:8080/jobs");

    private Client client;

    @Inject
    public RemoteJobExecutor(Client client) {
        this.client = client;
    }

    public void executeJob(String id, SyncWorkConfig workConfig, JobType jobType) {
        Map<String ,String> jobDetail = Maps.newHashMap();
        jobDetail.put("srcRepoUrl", workConfig.getSrcRepoUrl());
        jobDetail.put("srcRepoUsername", workConfig.getRepoAccount().getUsername());
        jobDetail.put("srcRepoSecret", workConfig.getRepoAccount().getSecret());
        jobDetail.put("srcRepoBranch", workConfig.getSrcRepoBranch());
        jobDetail.put("syncToZanataOption", workConfig.getSyncToZanataOption().name());
        jobDetail.put("srcRepoType", workConfig.getRepoAccount().getRepoType());
        jobDetail.put("zanataUsername", workConfig.getZanataAccount().getUsername());
        jobDetail.put("zanataSecret", workConfig.getZanataAccount().getSecret());
        log.debug("about to execute job remotely with: {}", jobDetail);
        Response response;
        switch (jobType) {
            case REPO_SYNC:
                response = client.target(JOB_SERVER_URL)
                        .path("api").path("job").path("2repo").path("start").path(id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .post(Entity.entity(jobDetail, MediaType.APPLICATION_JSON_TYPE));
                break;
            case SERVER_SYNC:
                response = client.target(JOB_SERVER_URL)
                        .path("api").path("job").path("2zanata").path("start").path(id)
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
