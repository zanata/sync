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
package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.model.Credentials;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.plugin.git.GitPlugin;

/**
 * This service will choose between native git or jgit depending on the
 * environment.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Alternative
@Dependent // note: it has to be dependent scope so that the async JobRunner will use the same object in JobResource
@RepoPlugin(GitPlugin.NAME)
public class EnvAwareGitSyncService implements RepoSyncService {
    private static boolean hasNativeGit = isGitExecutableOnPath();

    private static boolean isGitExecutableOnPath() {
        Pattern pattern = Pattern.compile(Pattern.quote(File.pathSeparator));
        return pattern.splitAsStream(System.getenv("PATH"))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve("git")));
    }

    private RepoSyncService syncService;

    @Inject
    public EnvAwareGitSyncService(GitSyncService jgit, NativeGitSyncService nativeGit) {
//        if (hasNativeGit) {
//            syncService = nativeGit;
//        } else {
            syncService = jgit;
//        }
    }


    @Override
    public void cloneRepo() throws RepoSyncException {
        syncService.cloneRepo();
    }

    @Override
    public void syncTranslationToRepo() throws RepoSyncException {
        syncService.syncTranslationToRepo();
    }

    @Override
    public void setCredentials(Credentials credentials) {
        syncService.setCredentials(credentials);
    }

    @Override
    public void setUrl(String url) {
        syncService.setUrl(url);
    }

    @Override
    public void setBranch(String branch) {
        syncService.setBranch(branch);
    }

    @Override
    public void setWorkingDir(File workingDir) {
        syncService.setWorkingDir(workingDir);
    }
}
