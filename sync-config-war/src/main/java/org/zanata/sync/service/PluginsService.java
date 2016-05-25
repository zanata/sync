package org.zanata.sync.service;

import java.util.List;
import java.util.Map;

import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.exception.UnableLoadPluginException;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface PluginsService {

    List<RepoExecutor> getAvailableSourceRepoPlugins();

    RepoExecutor getNewSourceRepoPlugin(String className);

    RepoExecutor getNewSourceRepoPlugin(String className,
            Map<String, String> fields) throws UnableLoadPluginException;

    void init();
}
