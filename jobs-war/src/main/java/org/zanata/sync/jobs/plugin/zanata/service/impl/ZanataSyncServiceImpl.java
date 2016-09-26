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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullOptions;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.jobs.common.exception.ZanataSyncException;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import org.zanata.sync.jobs.plugin.zanata.util.PushPullOptionsUtil;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataSyncServiceImpl implements ZanataSyncService {
    private static final Logger log =
            LoggerFactory.getLogger(ZanataSyncServiceImpl.class);

    private final PullOptions pullOptions;
    private final PushOptions pushOptions;

    private final PushServiceImpl pushService = new PushServiceImpl();
    private final PullServiceImpl pullService = new PullServiceImpl();
    private final String zanataUrl;

    public ZanataSyncServiceImpl(String zanataUrl, String username,
            String apiKey, String syncToZanataOption, String localeId) {
        this.zanataUrl = zanataUrl;
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
        if (!Strings.isNullOrEmpty(localeId)) {
            pullOptions.setLocales(localeId);
            pushOptions.setLocales(localeId);
        }
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
        checkURL(getPushOptions());
        pushService.pushToZanata(getPushOptions());
    }

    private void checkURL(PushPullOptions options) {
        if (options.getUrl() != null) {
            String urlInProjectConfig = options.getUrl().toString();
            // check URL defined in zanata.xml from source repository against the
            // one from API call (which is defines where the zanata account belongs
            // to)
            if (!urlInProjectConfig.equals(zanataUrl)) {
                log.warn("Using account from [{}] but the repo has zanata.xml using [{}]",
                        zanataUrl, urlInProjectConfig);
            }
        }
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
        checkURL(getPullOptions());
        pullService.pullFromZanata(getPullOptions());
    }
}
