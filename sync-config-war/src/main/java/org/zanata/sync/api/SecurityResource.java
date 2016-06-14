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
package org.zanata.sync.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.security.AuthorizationServlet;
import org.zanata.sync.security.SecurityTokens;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/oauth")
@Produces(MediaType.APPLICATION_JSON)
public class SecurityResource {
    private static final Logger log =
            LoggerFactory.getLogger(SecurityResource.class);
    @Inject
    @DeltaSpike
    private HttpServletRequest request;

//    @Inject
//    @DeltaSpike
//    private HttpServletResponse response;
//
    @Inject
    private SecurityTokens securityTokens;

    private List<String> productionServerUrls = ImmutableList.<String>builder()
            .add("http://localhost:8180/zanata")
            .add("http://localhost:8080/zanata")
            .add("https://translate.zanata.org")
            .add("https://translate.jboss.org")
            .add("https://fedora.zanata.org")
            .build();

    @GET
    public Response getAvailableZanataServers() {
        return Response.ok(productionServerUrls).build();
    }

    @GET
    @Path("/url")
    public Response getZanataAuthUrl(@QueryParam("z") String zanataUrl) {
        if (Strings.isNullOrEmpty(zanataUrl)) {
            String errorMessage =
                    "You must select one production server";

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Payload.error(errorMessage)).build();
        }
        String zanataAuthUrl = generateOAuthURL(zanataUrl);

        try {
            // we prepend /auth/ to redirect url so that it can hit the web filter
            // see AuthorizationCodeFilter
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(zanataAuthUrl)
                    .setClientId("zanata_sync")
                    .setRedirectURI(appRoot() + AuthorizationServlet.SERVLET_URL + "?z=" + zanataUrl)
                    .buildQueryMessage();

            log.info("=========== redirecting to {}", request.getLocationUri());
//            context.getExternalContext().redirect(request.getLocationUri());
//            context.responseComplete();
            return Response.ok(Payload.ok(request.getLocationUri())).build();

        } catch (OAuthSystemException e) {
            return Response.serverError().entity(Payload.error(e.getMessage())).build();
        }

    }

    // TODO may not need this method
    @POST
    public Response getOAuthTokens(@QueryParam("z") String zanataUrl, @QueryParam("code") String authorizationCode) {
        if (Strings.isNullOrEmpty(zanataUrl)) {
            String errorMessage =
                    "You must select one production server";

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Payload.error(errorMessage)).build();
        }
        securityTokens.setZanataServerUrl(zanataUrl);

        try {
            securityTokens.requestOAuthTokens(authorizationCode);
            log.debug("authorization code: {}", authorizationCode);
            log.debug("access token: {}", securityTokens.getAccessToken());
            log.debug("refresh token: {}", securityTokens.getRefreshToken());
            // cheap DTO
            Map<String, String> data = Maps.newHashMap();
            data.put("accessToken", securityTokens.getAccessToken());
            data.put("refreshToken", securityTokens.getRefreshToken());
            return Response.ok(data).build();
        } catch (OAuthProblemException e) {
            return Response.serverError().entity(Payload.error(e.getMessage())).build();
        }
    }

    private String generateOAuthURL(String zanataUrl) {
        String authorizeUri = "oauth/";
        if (zanataUrl.endsWith("/")) {
            return zanataUrl + authorizeUri;
        } else {
            return zanataUrl + "/" + authorizeUri;
        }
    }

    private String appRoot() {
        String contextPath = request.getContextPath();
        String scheme = request.getScheme();
        int serverPort = request.getServerPort();
        String serverName = request.getServerName();
        String port = serverPort == 80 ? "" : ":" + serverPort;
        return scheme + "://" + serverName + port + contextPath;
    }

}
