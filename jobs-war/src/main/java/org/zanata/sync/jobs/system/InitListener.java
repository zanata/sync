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
package org.zanata.sync.jobs.system;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebListener
public class InitListener implements ServletContextListener {
    private static final Logger log =
            LoggerFactory.getLogger(InitListener.class);
    @Inject
    @ConfigWarUrl
    private String configWarUrl;

    @Inject
    @JAXRSClientConnectionPoolSize
    private int poolSize;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // we should check all the system state here to make sure they are all set
        log.info("==== system config ====");
        log.info("==== config war: {}", configWarUrl);
        log.info("==== JAXRS client connection pool size: {}", poolSize);
        log.info("==== system config ====");

        configAppHealthCheck(configWarUrl);
        writeOutCustomKeyStore();
    }

    private static void configAppHealthCheck(String configWarUrl) {
        if (configWarUrl.startsWith("http://localhost")) {
            log.info("=== skip config war health check for localhost ===");
            return;
        }
        Client client = ResteasyClientBuilder.newClient();
        try {
            Response response =
                    client.target(configWarUrl).path("api").path("job")
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .head();
            Preconditions.checkState(
                    response.getStatus() == Response.Status.OK.getStatusCode(),
                    configWarUrl + " is not running");
        } finally {
            client.close();
        }
    }

    /**
     * If we have packaged a custom key store, we will write it out. This is due
     * to openshift not supporting replicate file system for scalable app.
     */
    private static void writeOutCustomKeyStore() {
        URL cacerts = Thread.currentThread().getContextClassLoader()
                .getResource("cacerts");
        if (cacerts == null) {
            log.info("no packaged custom key store");
            return;
        }


        try (InputStream cacertsIS = cacerts.openStream()) {
            String trustStorePath =
                    System.getProperty("javax.net.ssl.trustStore");
            if (Strings.isNullOrEmpty(trustStorePath)) {
                log.warn(
                        "[javax.net.ssl.trustStore] is not set but found packaged key store! Ignored.");
                return;
            }
            Files.copy(cacertsIS, Paths.get(trustStorePath),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
