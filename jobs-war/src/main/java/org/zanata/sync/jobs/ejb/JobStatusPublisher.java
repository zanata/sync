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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.system.ResourceProducer;
import org.zanata.sync.jobs.system.SysConfig;
import org.zanata.sync.model.JobStatusType;
import com.google.common.collect.ImmutableMap;

import static javax.ws.rs.core.MediaType.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class JobStatusPublisher {
    private static final Logger log =
            LoggerFactory.getLogger(JobStatusPublisher.class);

    @Inject
    @SysConfig(ResourceProducer.CONFIG_WAR_URL_KEY)
    private String configWarUrl;

    private Client client;

    public JobStatusPublisher() {
        // This will create a threadsafe JAX-RS client using pooled connections.
        // Per default this implementation will create no more than than 2
        // concurrent connections per given route and no more 20 connections in
        // total. (see javadoc of PoolingHttpClientConnectionManager)
        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager();

        CloseableHttpClient closeableHttpClient =
                HttpClientBuilder.create().setConnectionManager(cm).build();
        ApacheHttpClient4Engine engine =
                new ApacheHttpClient4Engine(closeableHttpClient);
        client = new ResteasyClientBuilder().httpEngine(engine).build();
    }

    private void putStatus(String jobId, JobStatusType statusType) {
        log.debug("put job status {} -> {}", jobId, statusType);
        try {
            client.target(configWarUrl).path("api").path("job").path("status")
                    .queryParam("id", jobId)
                    .queryParam("status", statusType)
                    .request(APPLICATION_JSON_TYPE)
                    .accept(APPLICATION_JSON_TYPE)
                    .put(Entity.json(null));
        } catch (Exception e) {
            // TODO do we retry or do we just gave up?
            log.error("Error publishing job status", e);
        }
    }

    public Future<Boolean> publish(Map<String, Future<Response>> doneJobs) {
        doneJobs.forEach((jobId, future) -> {
            try {
                Response response = future.get();
                if (response.getStatus() ==
                        Response.Status.OK.getStatusCode()) {
                    putStatus(jobId, JobStatusType.COMPLETED);
                } else {
                    log.debug("job response is not ok: {}",
                            response.getStatus());
                    putStatus(jobId, JobStatusType.ERROR);
                }
            } catch (InterruptedException | ExecutionException ee) {
                log.warn("exception getting future result", ee);
                putStatus(jobId, JobStatusType.ERROR);
            }
        });
        return new AsyncResult<>(true);
    }
}
