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
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.model.Credentials;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.jobs.system.HasNativeGit;
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
    private static final Logger log =
            LoggerFactory.getLogger(EnvAwareGitSyncService.class);
    private boolean hasNativeGit;
    private GitSyncService jgit;
    private NativeGitSyncService nativeGit;

    @Inject
    public EnvAwareGitSyncService(GitSyncService jgit,
            NativeGitSyncService nativeGit,
            @HasNativeGit boolean hasNativeGit) {
        this.jgit = jgit;
        this.nativeGit = nativeGit;
        this.hasNativeGit = hasNativeGit;
    }


    @Override
    public void cloneRepo() throws RepoSyncException {
        if (hasNativeGit) {
            try {
                nativeGit.cloneRepo();
            } catch (Exception e) {
                log.info("native git clone failed [{}]. Re-try with JGit", e.getMessage());
                log.debug("native git clone failed", e);
                jgit.cloneRepo();
            }
        } else {
            jgit.cloneRepo();
        }
    }

    @Override
    public void syncTranslationToRepo() throws RepoSyncException {
        if (hasNativeGit) {
            try {
                nativeGit.syncTranslationToRepo();
            } catch (Exception e) {
                log.info("native git sync translation failed [{}]. Re-try with JGit", e.getMessage());
                log.debug("native git sync failed", e);
                jgit.syncTranslationToRepo();
            }
        } else {
            jgit.cloneRepo();
        }
    }

    @Override
    public void setCredentials(Credentials credentials) {
        jgit.setCredentials(credentials);
        nativeGit.setCredentials(credentials);
    }

    @Override
    public void setUrl(String url) {
        jgit.setUrl(url);
        nativeGit.setUrl(url);
    }

    @Override
    public void setBranch(String branch) {
        jgit.setBranch(branch);
        nativeGit.setBranch(branch);
    }

    @Override
    public void setWorkingDir(File workingDir) {
        jgit.setWorkingDir(workingDir);
        nativeGit.setWorkingDir(workingDir);
    }

    @Override
    public void setZanataUser(String zanataUrl, String zanataUsername) {
        jgit.setZanataUser(zanataUrl, zanataUsername);
        nativeGit.setZanataUser(zanataUrl, zanataUsername);
    }
}
