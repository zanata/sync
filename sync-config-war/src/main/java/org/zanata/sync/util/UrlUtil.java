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

import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class UrlUtil {
    private static final Logger log = LoggerFactory.getLogger(UrlUtil.class);

    /**
     * Takes in two url parts and concatenate them to form a valid url. It
     * handles trailing slash '/' of the first parameter and leading slash '/'
     * of the second parameter.
     *
     * @param first
     *         a url part
     * @param second
     *         another url part
     * @return concatenated url parts without missing '/' or double '/' in
     * between
     */
    public static String concatUrlPath(String first, String second) {
        if (Strings.isNullOrEmpty(first) || Strings.isNullOrEmpty(second)) {
            return first + second;
        }
        StringBuilder sb = new StringBuilder();
        if (first.endsWith("/") && second.startsWith("/")) {
            sb.append(first.substring(0, first.length() - 1))
                    .append(second);
        } else if (!first.endsWith("/") && !second.startsWith("/")) {
            sb.append(first).append("/").append(second);

        } else {
            sb.append(first).append(second);
        }
        return sb.toString();
    }

    /**
     * This will return the Zanata OAuth landing url.
     *
     * @param appRoot
     *         the root url of this app (sync config war)
     * @param zanataServerUrl
     *         zanata server root url
     *
     * @see org.zanata.sync.servlet.AuthorizationCodeFilter
     * @return zanata OAuth page url
     */
    public static String zanataOAuthUrl(String appRoot,
            String zanataServerUrl) {
        String zanataAuthUrl = UrlUtil.concatUrlPath(zanataServerUrl, "oauth");
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(zanataAuthUrl)
                    .setClientId("zanata_sync")
                    .setRedirectURI(concatUrlPath(appRoot, "/app?z=" + zanataServerUrl))
                    .buildQueryMessage();
            log.debug("OAuth will redirect to {}", request.getLocationUri());
            return request.getLocationUri();
        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     *
     * @param request http servlet request
     * @return absolute app root url of the SPA (sync config war)
     */
    public static String appRootAbsoluteUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String scheme = request.getScheme();
        int serverPort = request.getServerPort();
        String serverName = request.getServerName();
        String port = serverPort == 80 ? "" : ":" + serverPort;
        return scheme + "://" + serverName + port + contextPath;
    }
}
