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
package org.zanata.sync.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.ZanataAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.util.ZanataRestClient;
import com.google.common.base.Throwables;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This filter should only be accessed by Zanata after a successful OAuth authentication.
 * Filter mapping url is defined in web.xml to specify order of execution.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "authorizationCodeFilter")
public class AuthorizationCodeFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(AuthorizationCodeFilter.class);
    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private ZanataRestClient zanataRestClient;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        try {
            String zanataUrl = request.getParameter("z");
            String authCode = request.getParameter(OAuth.OAUTH_CODE);
            if (!isNullOrEmpty(zanataUrl) && !isNullOrEmpty(authCode)) {
                OAuthAuthzResponse oAuthResponse = OAuthAuthzResponse
                        .oauthCodeAuthzResponse(httpServletRequest);
                String code = oAuthResponse.getCode();
                ZanataAccount account = requestOAuthTokens(zanataUrl, code);
                account.setZanataServer(zanataUrl);
                securityTokens.setAuthenticatedAccount(account);
            }
            // TODO just hitting home page with ugly parameters. get URL rewrite and make the url look nicer
            chain.doFilter(request, response);
        } catch (OAuthProblemException e) {
            log.warn("=== problem with OAuth", e);

            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.setContentType("text/html");
            PrintWriter writer = httpServletResponse.getWriter();
            writer.print("<h2>error:</h2>");
            writer.println(e.getError() + ":" +  e.getDescription());
            writer.flush();
            writer.close();
        }
    }

    private ZanataAccount requestOAuthTokens(String zanataUrl,
            String authorizationCode)
            throws OAuthProblemException {
        if (zanataUrl == null) {
            throw new IllegalStateException(
                    "You are not authorized to one Zanata server");
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
                    oAuthClient.accessToken(request, "POST",
                            StatusCodeAwareOAuthJSONAccessTokenResponse.class);

            OAuthToken oAuthToken = accessTokenResponse.getOAuthToken();
            String accessToken = oAuthToken.getAccessToken();
            String refreshToken = oAuthToken.getRefreshToken();

            log.debug("authorization code: {}", authorizationCode);
            log.debug("access token: {}", accessToken);
            log.debug("refresh token: {}", refreshToken);

            // this should change once we have Zanata all converted to use OAuth
            ZanataAccount account = zanataRestClient
                    .getAuthorizedAccount(zanataUrl, accessToken);
            log.debug("========= my account: {}", account);
            // for the time being, we only allow Zanata admin to create jobs
            if (!account.getRoles().contains("admin")) {
                throw OAuthProblemException
                        .error("Only Zanata admin can create sync job");
            }
            return account;

        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        } finally {
            if (oAuthClient != null) {
                oAuthClient.shutdown();
            }
        }
    }

    @Override
    public void destroy() {
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
