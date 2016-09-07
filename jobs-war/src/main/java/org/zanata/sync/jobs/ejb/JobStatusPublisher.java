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
package org.zanata.sync.jobs.ejb;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.system.ConfigWarUrl;
import org.zanata.sync.jobs.system.RestClient;
import org.zanata.sync.model.JobStatusType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * This is used by the stateless EJB. So its lifecycle will be bound to the EJB
 * which I think it's pooled.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class JobStatusPublisher {
    private static final Logger log =
            LoggerFactory.getLogger(JobStatusPublisher.class);

    @Inject
    @ConfigWarUrl
    private String configWarUrl;

    @Inject
    @RestClient
    private Client client;

    void putStatus(String jobId, JobStatusType statusType) {
        log.debug("put job status {} -> {}", jobId, statusType);
        Response response = null;
        try {
            response = client.target(configWarUrl).path("api").path("job")
                    .path("status")
                    .queryParam("id", jobId)
                    .queryParam("status", statusType)
                    .request(APPLICATION_JSON_TYPE)
                    .accept(APPLICATION_JSON_TYPE)
                    .put(Entity.json(null));
            log.info("put job status {} -> {} done", jobId, statusType);
        } catch (Exception e) {
            // TODO do we retry or do we just gave up?
            log.error("Error publishing job status", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.debug("error closing response", e);
                    // ignored
                }
            }
        }
    }

    void publishStatus(String jobId, Response response) {
        log.debug("publish job status {} -> {}", jobId, response.getStatus());
        if (response.getStatus() ==
                Response.Status.OK.getStatusCode()) {
            putStatus(jobId, JobStatusType.COMPLETED);
        } else {
            log.info("job response is not ok: {}",
                    response.getStatus());
            putStatus(jobId, JobStatusType.ERROR);
        }
    }
}
