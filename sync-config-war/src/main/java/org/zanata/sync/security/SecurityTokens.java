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
package org.zanata.sync.security;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.ZanataAccount;
import org.zanata.sync.util.ZanataRestClient;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@SessionScoped
public class SecurityTokens implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(SecurityTokens.class);
    @Inject
    private ZanataRestClient zanataRestClient;

    private ZanataAccount account;

    boolean hasAccess() {
        return account != null;
    }

    public void requestOAuthTokens(String zanataUrl, String authorizationCode)
            throws OAuthProblemException {
        if (zanataUrl == null) {
            throw new IllegalStateException("You are not authorized to one Zanata server");
        }
        OAuthClient oAuthClient = null;
        try {
            // TODO pahuang we only need to get access token and refresh token once (then we should persist the refresh token)
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(zanataUrl + "/rest/oauth/token")
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId("zanata_sync")
                    .setClientSecret("we_do_not_have_a_secret")
                    .setRedirectURI("http://www.not.in.use.but.required")
                    .setCode(authorizationCode)
                    .buildBodyMessage();

            //create OAuth client that uses custom http client under the hood
            oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthJSONAccessTokenResponse accessTokenResponse =
                    oAuthClient.accessToken(request, "POST", StatusCodeAwareOAuthJSONAccessTokenResponse.class);

            OAuthToken oAuthToken = accessTokenResponse.getOAuthToken();
            String accessToken = oAuthToken.getAccessToken();
            String refreshToken = oAuthToken.getRefreshToken();

            log.debug("authorization code: {}", authorizationCode);
            log.debug("access token: {}", accessToken);
            log.debug("refresh token: {}", refreshToken);

            // this should change once we have Zanata all converted to use OAuth
            account = zanataRestClient.getAuthorizedAccount(zanataUrl,
                    accessToken);
            log.debug("========= my account: {}", account);
            // for the time being, we only allow Zanata admin to create jobs
            if (!account.getRoles().contains("admin")) {
                throw OAuthProblemException.error("Only Zanata admin can create sync job");
            }

        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        } finally {
            if (oAuthClient != null) {
                oAuthClient.shutdown();
            }
        }
    }

    public ZanataAccount getAccount() {
        return account;
    }

    public static class StatusCodeAwareOAuthJSONAccessTokenResponse extends OAuthJSONAccessTokenResponse {

        @Override
        protected void init(String body, String contentType, int responseCode)
                throws OAuthProblemException {
            if (responseCode < 300) {
                super.init(body, contentType, responseCode);
            } else {
                throw OAuthProblemException.error("invalid status code:" + responseCode);
            }
        }
    }
}
