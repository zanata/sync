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
package org.zanata.sync.db;

import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.events.ResourceReadyEvent;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class DatabaseResourceProducer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseResourceProducer.class);

    private DataSource datasource;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Event<ResourceReadyEvent> resourceReadyEvent;

    public void onStartUp(@Observes @Initialized ServletContext servletContext) {
        try {
            datasource = (DataSource) new InitialContext().lookup("java:jboss/datasources/ExampleDS");
        } catch (Exception e) {
            log.error("Error while initialising the database connection pool", e);
            throw new IllegalStateException("Error while initialising the database connection pool", e);
        }
        log.info("Database connection pool initialized successfully: {}", entityManager);
        resourceReadyEvent.fire(new ResourceReadyEvent());
    }


}
