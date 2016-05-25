package org.zanata.sync.service.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.exception.UnableLoadPluginException;
import org.zanata.sync.service.PluginsService;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;


/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class PluginsServiceImpl implements PluginsService {
    private static final Logger log =
            LoggerFactory.getLogger(PluginsServiceImpl.class);
    @Inject @DeltaSpike
    private ServletContext servletContext;

    private static Map<String, Class<? extends RepoExecutor>>
        sourceRepoPluginMap;

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
        } catch (IOException | ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }

    private static <P> Map<String, Class<? extends P>> buildPluginMap(
            AnnotationDB db, ClassLoader cl, Class<? extends Annotation> pluginAnnotation)
            throws ClassNotFoundException {
        ImmutableMap.Builder<String, Class<? extends P>>
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
            pluginBuilder.put(entity.getName(), entity);
        }
        return pluginBuilder.build();
    }

    @Override
    public List<RepoExecutor> getAvailableSourceRepoPlugins() {
        List<RepoExecutor> result = new ArrayList<>();
        for (Class plugin : sourceRepoPluginMap.values()) {
            try {
                RepoExecutor executor =
                    getNewSourceRepoPlugin(plugin.getName(), null);
                result.add(executor);
            } catch (UnableLoadPluginException e) {
                log.warn("Unable to load plugin " + e.getMessage());
            }
        }
        return result;
    }

    @Override
    public RepoExecutor getNewSourceRepoPlugin(String className) {
        for (Class plugin : sourceRepoPluginMap.values()) {
            if (plugin.getName().equals(className)) {
                try {
                    return getNewSourceRepoPlugin(plugin.getName(), null);
                } catch (UnableLoadPluginException e) {
                    log.warn("Unable to load plugin " + e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public RepoExecutor getNewSourceRepoPlugin(String className,
        Map<String, String> fields) throws UnableLoadPluginException {
        Class<? extends RepoExecutor>
            executor = sourceRepoPluginMap.get(className);
        try {
            return executor.getDeclaredConstructor(Map.class)
                .newInstance(fields);
        } catch (Exception e) {
            log.error("failed to get new source repo plugin", e);
            throw new UnableLoadPluginException(className);
        }
    }

}
