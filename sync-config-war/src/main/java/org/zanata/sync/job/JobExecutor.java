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
package org.zanata.sync.job;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

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
public class JobExecutor {
    private static final String JOB_SERVER_URL = System.getProperty("job.server", "http://localhost:8080/jobs");
    private Client client;

    public JobExecutor(Client client) {
        this.client = client;
    }

    public void executeJob(String id, SyncWorkConfig workConfig, JobType jobType) {
        Map<String ,String> jobDetail = Maps.newHashMap();
        Map<String, String> srcRepoPluginConfig =
                workConfig.getSrcRepoPluginConfig();
        jobDetail.put("srcRepoUrl", srcRepoPluginConfig.get("url"));
        jobDetail.put("srcRepoUsername", srcRepoPluginConfig.get("username"));
        jobDetail.put("srcRepoSecret", srcRepoPluginConfig.get("apiKey"));
        jobDetail.put("srcRepoBranch", srcRepoPluginConfig.get("branch"));
        jobDetail.put("syncToZanataOption", workConfig.getSyncToZanataOption().name());
        jobDetail.put("srcRepoType", workConfig.getSrcRepoPluginName());

        Map<String, String> transServerConfig =
                workConfig.getTransServerPluginConfig();
        jobDetail.put("zanataUsername", transServerConfig.get("username"));
        jobDetail.put("zanataSecret", transServerConfig.get("apiKey"));
        switch (jobType) {
            case SERVER_SYNC:
                client.target(JOB_SERVER_URL)
                        .path("api").path("job").path("2repo").path("start").path(id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .post(Entity.entity(jobDetail, MediaType.APPLICATION_JSON_TYPE));
                break;
            case REPO_SYNC:
                client.target(JOB_SERVER_URL)
                        .path("api").path("job").path("2zanata").path("start").path(id)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .post(Entity.entity(jobDetail, MediaType.APPLICATION_JSON_TYPE));
                break;
        }
    }
}
