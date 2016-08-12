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
package org.zanata.sync.jobs.plugin.zanata.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.sync.jobs.common.exception.ZanataSyncException;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import org.zanata.sync.jobs.plugin.zanata.util.PushPullOptionsUtil;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataSyncServiceImpl implements ZanataSyncService {

    private final PullOptions pullOptions;
    private final PushOptions pushOptions;

    private final PushServiceImpl pushService = new PushServiceImpl();
    private final PullServiceImpl pullService = new PullServiceImpl();

    public ZanataSyncServiceImpl(String username,
            String apiKey, String syncToZanataOption) {
        PullOptionsImpl pullOptions = new PullOptionsImpl();
        PushOptionsImpl pushOptions = new PushOptionsImpl();
        pullOptions.setInteractiveMode(false);
        pushOptions.setInteractiveMode(false);
        pullOptions.setUsername(username);
        pullOptions.setKey(apiKey);
        pushOptions.setUsername(username);
        pushOptions.setKey(apiKey);
        pushOptions.setPushType(syncToZanataOption);
        this.pushOptions = pushOptions;
        this.pullOptions = pullOptions;
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
        File projectConfig = findProjectConfigOrThrow(repoBase);
        PushPullOptionsUtil
                .applyProjectConfig(getPushOptions(), projectConfig);
        pushService.pushToZanata(getPushOptions());
    }

    private File findProjectConfigOrThrow(Path repoBase) {
        Optional<File> projectConfig =
                PushPullOptionsUtil.findProjectConfig(repoBase.toFile());

        if (!projectConfig.isPresent()) {
            throw new ZanataSyncException(
                    "can not find project config (zanata.xml) in the repo");
        }
        return projectConfig.get();
    }

    @Override
    public void pullFromZanata(Path repoBase) throws ZanataSyncException {
        File projectConfig =
                findProjectConfigOrThrow(repoBase);
        PushPullOptionsUtil
                .applyProjectConfig(getPullOptions(), projectConfig);
        pullService.pullFromZanata(getPullOptions());
    }
}
