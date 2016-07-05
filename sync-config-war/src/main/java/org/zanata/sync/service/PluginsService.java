package org.zanata.sync.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.exception.UnableLoadPluginException;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface PluginsService {

    List<RepoExecutor> getAvailableSourceRepoPlugins();

    Optional<RepoExecutor> getSourceRepoPlugin(String className);

    void init();
}
