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
package org.zanata.sync.util;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.ZanataUserAccount;

import com.google.common.collect.Maps;

/**
 * This rest client knows how to use OAuth to talk to Zanata.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class ZanataRestClient {
    private static final Logger log =
            LoggerFactory.getLogger(ZanataRestClient.class);
    @Inject
    private Client client;

    public ZanataUserAccount getAuthorizedAccount(String zanataUrl,
            String accessToken) {
        Map<String, Object> accessTokenMap = Maps.newHashMap();
        accessTokenMap.put(OAuth.OAUTH_ACCESS_TOKEN, accessToken);

        // we don't use dto from zanata api because
        // it's using an incompatible version of jackson annotation
        return client.target(zanataUrl + "/rest/user/myaccount")
                .request(MediaType.APPLICATION_JSON_TYPE)
//                    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth.HeaderType.AUTHORIZATION,
                        OAuthUtils.encodeAuthorizationBearerHeader(
                                accessTokenMap))
                .get(ZanataUserAccount.class);
    }
}
