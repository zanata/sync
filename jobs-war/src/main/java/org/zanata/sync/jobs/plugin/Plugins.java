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
package org.zanata.sync.jobs.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class Plugins {

    private final Properties properties;

    public Plugins() {
        try (InputStream stream =
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(
                                "supportedSrcRepoTypes.properties")) {
            properties = new Properties();
            properties.load(stream);

        } catch (IOException e) {
            throw new RepoSyncException(
                    "failed to load supported src repo config file", e);
        }
    }

    public Class<? extends RepoSyncService> getClassForSrcRepo(String srcRepoType) {
        String srcRepoPluginClassName = properties.getProperty(srcRepoType);
        if (srcRepoPluginClassName == null) {
            throw new RepoSyncException(
                    "source repo type [" + srcRepoType + "] is unsupported");
        }

        try {
            Class<?> srcRepoPluginClass = Class.forName(srcRepoPluginClassName);
            return (Class<? extends RepoSyncService>) srcRepoPluginClass;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new RepoSyncException(
                    "unable to load class for " + srcRepoType, e);
        }
    }
}
