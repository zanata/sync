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

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;

import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.component.AppConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.integration.servlet.LiquibaseServletListener;

/**
 * We need to set jdbc url before liquibase starts using it.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CustomLiquibaseServletListener extends LiquibaseServletListener {
    private static final Logger log =
            LoggerFactory.getLogger(CustomLiquibaseServletListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            InitialContext context = new InitialContext();

            ServletValueContainer servletValueContainer =
                    new ServletValueContainer(
                            servletContextEvent.getServletContext(), context);
            LiquibaseConfiguration.getInstance().init(servletValueContainer);

            String dataSourceStr =
                    (String) servletValueContainer.getValue(
                        "liquibase.datasource");
            Preconditions.checkState(!Strings.isNullOrEmpty(dataSourceStr),
                "liquibase.datasource is not set");

            ComboPooledDataSource dataSource = (ComboPooledDataSource) context.lookup(dataSourceStr);
            dataSource.setDriverClass(Driver.class.getName());

            String dbFile = AppConfiguration.getDBFilePathFromSystemProp();

            log.info("h2 database file path: {}", dbFile);
            dataSource.setJdbcUrl("jdbc:h2:" + dbFile + ";AUTO_SERVER=TRUE");
        } catch (Exception e) {
            log.error("Error while initialising the database connection pool", e);
            throw new IllegalStateException("Error while initialising the database connection pool", e);
        }
        super.contextInitialized(servletContextEvent);
    }
}
