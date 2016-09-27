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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.jobs.common.exception.ZanataSyncException;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import org.zanata.sync.jobs.plugin.zanata.util.PushPullOptionsUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

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
    private final Set<String> projectConfigs;

    public ZanataSyncServiceImpl(SyncJobDetail jobDetail) {
        String zanataUrl = jobDetail.getZanataUrl();
        String username = jobDetail.getZanataUsername();
        String apiKey = jobDetail.getZanataSecret();
        SyncOption syncToZanataOption = jobDetail.getSyncToZanataOption();
        String pushToZanataOption =
                syncToZanataOption != null ? syncToZanataOption.getValue() :
                        null;
        projectConfigs = getProjectConfigs(jobDetail.getProjectConfigs());

        String localeId = jobDetail.getLocaleId();
        this.zanataUrl = zanataUrl;
        PullOptionsImpl pullOptions = new PullOptionsImpl();
        PushOptionsImpl pushOptions = new PushOptionsImpl();
        pullOptions.setInteractiveMode(false);
        pushOptions.setInteractiveMode(false);
        pullOptions.setUsername(username);
        pullOptions.setKey(apiKey);
        pushOptions.setUsername(username);
        pushOptions.setKey(apiKey);
        pushOptions.setPushType(pushToZanataOption);
        this.pushOptions = pushOptions;
        this.pullOptions = pullOptions;
//        this.pushOptions.setLogHttp(true);
//        this.pullOptions.setLogHttp(true);
        if (!Strings.isNullOrEmpty(localeId)) {
            pullOptions.setLocales(localeId);
            pushOptions.setLocales(localeId);
        }
    }

    private static Set<String> getProjectConfigs(String projectConfigs) {
        if (Strings.isNullOrEmpty(projectConfigs)) {
            return Collections.emptySet();
        }
        return ImmutableSet
                .copyOf(Splitter.on(",").trimResults().omitEmptyStrings()
                        .split(projectConfigs));
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
        if (projectConfigs.isEmpty()) {
            File projectConfig = findProjectConfigOrThrow(repoBase);
            PushPullOptionsUtil
                    .applyProjectConfig(getPushOptions(), projectConfig);
            checkURL(getPushOptions().getUrl(), zanataUrl);
            pushService.pushToZanata(getPushOptions());
        } else {
            for (String projectConfig : projectConfigs) {
                Path absPath = Paths.get(repoBase.toString(), projectConfig);
                PushPullOptionsUtil.applyProjectConfig(getPushOptions(), absPath.toFile());
                pushService.pushToZanata(getPushOptions());
            }
        }
    }

    private static void checkURL(URL urlInProjectConfig, String urlFromJobDetail) {
        if (urlInProjectConfig != null) {
            // check URL defined in zanata.xml from source repository against the
            // one from API call (which is defines where the zanata account belongs
            // to)
            if (!urlFromJobDetail.equals(urlInProjectConfig.toString())) {
                log.warn("Using account from [{}] but the repo has zanata.xml using [{}]",
                        urlFromJobDetail, urlInProjectConfig);
            }
        }
    }

    // TODO ZNTA-1247 support multiple projects in one repo
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
        if (projectConfigs.isEmpty()) {
            File projectConfig =
                    findProjectConfigOrThrow(repoBase);
            PushPullOptionsUtil
                    .applyProjectConfig(getPullOptions(), projectConfig);
            checkURL(getPullOptions().getUrl(), zanataUrl);
            pullService.pullFromZanata(getPullOptions());
        } else {
            for (String projectConfig : projectConfigs) {
                Path absPath = Paths.get(repoBase.toString(), projectConfig);
                PushPullOptionsUtil.applyProjectConfig(getPullOptions(), absPath.toFile());
                pullService.pullFromZanata(getPullOptions());
            }
        }
    }
}
