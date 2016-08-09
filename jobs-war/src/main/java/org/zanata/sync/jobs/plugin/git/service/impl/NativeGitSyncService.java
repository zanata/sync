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

import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.model.Credentials;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;

/**
 * This will try to use the native git executable on PATH.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class NativeGitSyncService implements RepoSyncService {

    @Override
    public void cloneRepo() throws RepoSyncException {

        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public void syncTranslationToRepo() throws RepoSyncException {

    }

    @Override
    public void setCredentials(Credentials credentials) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public void setUrl(String url) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public void setBranch(String branch) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }

    @Override
    public void setWorkingDir(File workingDir) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //
    }
}
