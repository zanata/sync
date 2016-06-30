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

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebListener
public class InitListener implements ServletContextListener {
    private static final Logger log =
            LoggerFactory.getLogger(InitListener.class);
    @Inject
    @SysConfig(ResourceProducer.CONFIG_WAR_URL_KEY)
    private String configWarUrl;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // we should check all the system configurations here to make sure they are all set
        Preconditions.checkNotNull(configWarUrl,
                "You must set system property:" +
                        ResourceProducer.CONFIG_WAR_URL_KEY);
        if (configWarUrl.matches("//localhost")) {
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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
