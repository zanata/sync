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

import java.util.List;

import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.client.ProjectIterationLocalesClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.sync.plugin.zanata.exception.ZanataSyncException;
import org.zanata.sync.plugin.zanata.service.PushService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PushServiceImpl implements PushService {

    public void pushToZanata(PushOptions pushOptions) {
        LocaleList localesList = getLocalesFromServer(pushOptions);
        pushOptions.setLocaleMapList(localesList);
        PushCommand pushCommand = new PushCommand(pushOptions);
        try {
            pushCommand.run();
        } catch (Exception e) {
            throw new ZanataSyncException("failed pushing to Zanata", e);
        }
    }

    // TODO open up OptionsUtil method to public so that we can reuse it here
    private LocaleList getLocalesFromServer(PushOptions pushOptions) {
        RestClientFactory restClientFactory =
                OptionsUtil.createClientFactoryWithoutVersionCheck(pushOptions);
        ProjectIterationLocalesClient projectLocalesClient = restClientFactory
                .getProjectLocalesClient(pushOptions.getProj(),
                        pushOptions.getProjectVersion());
        List<LocaleDetails> locales = projectLocalesClient.getLocales();
        LocaleList localesList = new LocaleList();
        for (LocaleDetails details : locales) {
            localesList.add(new LocaleMapping(details.getLocaleId().getId(), details.getAlias()));
        }
        return localesList;
    }
}
