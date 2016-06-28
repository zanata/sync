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
package org.zanata.sync.jobs.ejb;

import java.nio.file.Files;
import java.util.concurrent.Future;
import java.util.function.Function;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response;

import org.zanata.sync.jobs.common.AutoCleanablePath;
import org.zanata.sync.jobs.common.Either;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.exception.ZanataSyncException;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
@Asynchronous
public class JobRunner {

    public Future<Response> syncToZanata(
            Either<RepoSyncService, Response> srcRepoPlugin,
            Either<ZanataSyncService, Response> zanataSyncService,
            String id) {
        try (AutoCleanablePath workingDir = new AutoCleanablePath(
                Files.createTempDirectory(id))) {
            Response result = srcRepoPlugin
                    .map(plugin -> zanataSyncService.map(zanata -> {
                        plugin.setWorkingDir(workingDir.toFile());
                        plugin.cloneRepo();
                        zanata.pushToZanata(workingDir.toPath());
                        return Response.ok().build();
                    }, Function.identity()), Function.identity());
            return new AsyncResult<>(result);
        } catch (Exception e) {
            throw new ZanataSyncException("Fail to sync to Zanata", e);
        }
    }

    public Future<Response> syncToSrcRepo(String id,
            Either<RepoSyncService, Response> srcRepoPlugin,
            Either<ZanataSyncService, Response> zanataSyncService) {

        try (AutoCleanablePath workingDir = new AutoCleanablePath(
                Files.createTempDirectory(id))) {

            Response result = srcRepoPlugin
                    .map(plugin -> zanataSyncService.map(zanata -> {
                        plugin.setWorkingDir(workingDir.toFile());
                        plugin.cloneRepo();
                        zanata.pullFromZanata(workingDir.toPath());
                        plugin.syncTranslationToRepo();
                        return Response.ok().build();
                    }, Function.identity()), Function.identity());
            return new AsyncResult<>(result);
        } catch (Exception e) {
            throw new RepoSyncException("failed to sync to source repo", e);
        }
    }
}