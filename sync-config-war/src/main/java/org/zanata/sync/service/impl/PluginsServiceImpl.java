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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.App;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.exception.UnableLoadPluginException;
import org.zanata.sync.service.PluginsService;
import com.google.common.base.Throwables;
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
    @Inject @DeltaSpike
    private ServletContext servletContext;

    private static Map<String, RepoExecutor> sourceRepoPluginMap;

    /**
     * Initiate all plugins available
     */
    @Override
    public void init() {
        Set<String> libJars = servletContext.getResourcePaths("/WEB-INF/lib");
        Set<URL> pluginJars = libJars.stream()
                .filter(jar -> jar.toLowerCase().contains("plugin") ||
                        jar.toLowerCase().contains("common"))
                .map(jar -> {
                    try {
                        return servletContext.getResource(jar);
                    } catch (MalformedURLException e) {
                        log.error("error getting resource", e);
                        return null;
                    }
                })
                .filter(url -> url != null)
                .collect(Collectors.toSet());

        AnnotationDB db = new AnnotationDB();
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] urls = pluginJars.toArray(new URL[]{});
            db.scanArchives(urls);

            sourceRepoPluginMap = buildPluginMap(db, cl, RepoPlugin.class);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static <P> Map<String, RepoExecutor> buildPluginMap(
            AnnotationDB db, ClassLoader cl, Class<RepoPlugin> pluginAnnotation)
            throws ClassNotFoundException, UnableLoadPluginException {
        ImmutableMap.Builder<String, RepoExecutor>
                pluginBuilder = ImmutableMap.builder();
        Set<String> repoPluginClasses =
                db.getAnnotationIndex().get(pluginAnnotation.getName());
        log.info("available plugins for {} - {}", pluginAnnotation,
                repoPluginClasses);

        if (repoPluginClasses == null) {
            return pluginBuilder.build();
        }
        for (String cls : repoPluginClasses) {
            Class<? extends P> entity =
                    (Class<? extends P>) cl.loadClass(cls);
            try {
                RepoExecutor instance = (RepoExecutor) entity.newInstance();
                pluginBuilder.put(instance.getName(), instance);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("failed to get new source repo plugin", e);
                throw new UnableLoadPluginException(pluginAnnotation.getName());
            }
        }
        return pluginBuilder.build();
    }

    @Override
    public List<RepoExecutor> getAvailableSourceRepoPlugins() {
        return ImmutableList.copyOf(sourceRepoPluginMap.values());
    }

    @Override
    public Optional<RepoExecutor> getSourceRepoPlugin(String pluginName) {
        return Optional.ofNullable(sourceRepoPluginMap.get(pluginName));
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
