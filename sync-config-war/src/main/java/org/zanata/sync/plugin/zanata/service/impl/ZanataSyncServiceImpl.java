/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.sync.plugin.zanata.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.sync.plugin.zanata.exception.ZanataSyncException;
import org.zanata.sync.plugin.zanata.service.ZanataSyncService;
import org.zanata.sync.plugin.zanata.util.PushPullOptionsUtil;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataSyncServiceImpl implements ZanataSyncService {

    private final PullOptions pullOptions;
    private final PushOptions pushOptions;

    private final PushServiceImpl pushService = new PushServiceImpl();
    private final PullServiceImpl pullService = new PullServiceImpl();

    public ZanataSyncServiceImpl(PullOptions pullOptions,
            PushOptions pushOptions, String username, String apiKey) {
        this.pullOptions = pullOptions;
        this.pushOptions = pushOptions;
        this.pullOptions.setInteractiveMode(false);
        this.pushOptions.setInteractiveMode(false);
        this.pullOptions.setUsername(username);
        this.pullOptions.setKey(apiKey);
        this.pushOptions.setUsername(username);
        this.pushOptions.setKey(apiKey);
//        this.pushOptions.setLogHttp(true);
//        this.pullOptions.setLogHttp(true);
    }

    @Override
    public PullOptions getPullOptions() {
        return pullOptions;
    }

    @Override
    public PushOptions getPushOptions() {
        return pushOptions;
    }

    @Override
    public void pushToZanata(Path repoBase) throws ZanataSyncException {
        Optional<File> projectConfig =
                PushPullOptionsUtil.findProjectConfig(repoBase.toFile());

        projectConfig.ifPresent((file) -> {
            PushPullOptionsUtil.applyProjectConfig(getPushOptions(), projectConfig.get());
            pushService.pushToZanata(getPushOptions());
        });
        // TODO handle where project config can not be found in repo
    }

    @Override
    public void pullFromZanata(Path repoBase) throws ZanataSyncException {
        Optional<File> projectConfig =
                PushPullOptionsUtil.findProjectConfig(repoBase.toFile());

        projectConfig.ifPresent((file) -> {
            PushPullOptionsUtil.applyProjectConfig(getPullOptions(), projectConfig.get());
            pullService.pullFromZanata(getPullOptions());
        });
        // TODO handle where project config can not be found in repo
    }
}
