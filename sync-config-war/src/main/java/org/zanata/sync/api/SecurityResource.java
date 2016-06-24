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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.Payload;
import org.zanata.sync.security.SecurityTokens;
import com.google.common.base.Strings;

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

    @Inject
    private SecurityTokens securityTokens;

    @GET
    @Path("/url")
    @NoSecurityCheck
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
                    .setRedirectURI(appRoot() + "?z=" + zanataUrl)
                    .buildQueryMessage();

            log.debug("redirecting to {}", request.getLocationUri());
            return Response.ok(Payload.ok(request.getLocationUri())).build();

        } catch (OAuthSystemException e) {
            return Response.serverError().entity(Payload.error(e.getMessage())).build();
        }

    }

    @POST
    @Path("/logout")
    public Response logout() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response.ok().build();
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
