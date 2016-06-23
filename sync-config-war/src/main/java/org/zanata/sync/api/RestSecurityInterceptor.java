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

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.zanata.sync.dto.Payload;
import org.zanata.sync.security.SecurityTokens;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Provider
@PreMatching
@SecurityPrecedence
public class RestSecurityInterceptor implements ContainerRequestFilter {
    private static final String AUTH_ERROR_MSG =
            "Authorization check failed. You need to sign in from a Zanata server";
    @Inject
    private SecurityTokens securityTokens;

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {
        if (!securityTokens.hasAccess()) {
            context.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .header("Content-Type", MediaType.APPLICATION_JSON)
                            .entity(Payload.error(AUTH_ERROR_MSG)).build());
        }
    }
}
