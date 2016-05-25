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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.events.ResourceReadyEvent;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class DatabaseResourceProducer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseResourceProducer.class);

    private DataSource datasource;

    @Inject
    private Event<ResourceReadyEvent> resourceReadyEvent;

    public void onStartUp(@Observes @Initialized ServletContext servletContext) {
        try {
            datasource = (DataSource) new InitialContext().lookup("java:jboss/datasources/ExampleDS");
        } catch (Exception e) {
            log.error("Error while initialising the database connection pool", e);
            throw new IllegalStateException("Error while initialising the database connection pool", e);
        }
        log.info("Database connection pool initialized successfully");
        resourceReadyEvent.fire(new ResourceReadyEvent());
    }


    @Produces
    @RequestScoped
    // TODO should we make this dependent scope?
    protected Connection getConnection() {
        try {
            return datasource.getConnection();
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    protected void onConnectionDispose(@Disposes Connection conn) {
        doInTryCatch(conn, c -> c.setAutoCommit(true));
        doInTryCatch(conn, Connection::close);
    }

    public <T> T doInTransaction(WorkWithStatement<T> work) {
        Connection conn = null;
        T result = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (Statement statement = conn.createStatement()) {
                result = work.run(statement);
            }
            conn.commit();
            return result;

        } catch (Exception e) {
            doInTryCatch(conn, Connection::rollback);
            return result;
        } finally {
            doInTryCatch(conn, c -> c.setAutoCommit(true));
            doInTryCatch(conn, Connection::close);
        }
    }

    private static <T> void doInTryCatch(T nullableParam,
            WorkMayThrowEx<T> work) {
        try {
            if (nullableParam != null) {
                work.run(nullableParam);
            }
        } catch (SQLException e) {
            log.error("sql exception", e);
            throw Throwables.propagate(e);
        }
    }

    private interface WorkMayThrowEx<T> {
        void run(T param) throws SQLException;
    }

    public interface WorkWithStatement<T> {
        T run(Statement statement);
    }
}
