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
package org.zanata.sync.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.App;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.plugin.Plugin;
import org.zanata.sync.service.PluginsService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class PluginsServiceImpl implements PluginsService {
    private static final Logger log =
            LoggerFactory.getLogger(PluginsServiceImpl.class);

    private static Map<String, Plugin> sourceRepoPluginMap;

    @Inject
    @RepoPlugin
    private Instance<Plugin> repoExecutors;

    /**
     * Initiate all plugins available
     */
    @Override
    @PostConstruct
    public void init() {
        ImmutableMap.Builder<String, Plugin>
                pluginBuilder = ImmutableMap.builder();

        repoExecutors.forEach(repoExecutor -> {
            RepoPlugin annotation =
                    repoExecutor.getClass().getAnnotation(RepoPlugin.class);

            pluginBuilder.put(
                    annotation.value(), repoExecutor);
        });

        sourceRepoPluginMap = pluginBuilder.build();
    }

    @Override
    public List<Plugin> getAvailableSourceRepoPlugins() {
        return ImmutableList.copyOf(sourceRepoPluginMap.values());
    }

    @Produces
    @App
    public SupportedRepoTypes supportedSourceRepoTypes() {
        return new SupportedRepoTypes(sourceRepoPluginMap.keySet());
    }

    public static class SupportedRepoTypes {
        private final Set<String> types;

        public SupportedRepoTypes(Set<String> types) {
            this.types = ImmutableSet.copyOf(types);
        }

        public Set<String> getTypes() {
            return types;
        }
    }
}
