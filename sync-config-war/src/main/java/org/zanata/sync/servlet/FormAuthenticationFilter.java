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
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.LocalAccount;
import org.zanata.sync.dto.UserAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.AccountService;
import com.google.common.collect.Sets;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "formAuthenticationFilter")
public class FormAuthenticationFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(FormAuthenticationFilter.class);
    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private AccountService accountService;
    private static String serverURL;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            if (serverURL == null) {
                serverURL = getBaseUrl(req);
            }
            if (!securityTokens.hasAccess() && req.getRemoteUser() != null) {
                UserAccount account = new LocalAccount(req.getRemoteUser(),
                        Sets.newHashSet("syncUser"), true);
                log.info("authenticated using local user: {}", account);
                securityTokens.setAuthenticatedAccount(account);
                accountService.saveAuthenticatedAccount();
            }
        }
        chain.doFilter(request, response);
    }

    private static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
        String contextPath = request.getContextPath();
        return String
                .format("%s://%s%s%s", scheme, serverName, serverPort,
                        contextPath);
    }

    public static Optional<String> serverURLFromRequest() {
        return Optional.ofNullable(serverURL);
    }

    @Override
    public void destroy() {
    }
}
