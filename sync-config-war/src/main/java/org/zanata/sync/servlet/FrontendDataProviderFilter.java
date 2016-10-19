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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.plugin.Plugin;
import org.zanata.sync.dto.UserAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.util.CronType;
import org.zanata.sync.util.JSONObjectMapper;
import org.zanata.sync.util.UrlUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This filter is responsible for pre-load necessary data for the frontend
 * javascript app.
 * Filter mapping url is defined in web.xml to specify order of execution.
 * <p>
 * see index.jsp
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "frontendDataProviderFilter")
public class FrontendDataProviderFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(FrontendDataProviderFilter.class);

    private static String serverURL;

    @Inject
    private PluginsService pluginsService;

    @Inject
    private JSONObjectMapper objectMapper;

    @Inject
    private SecurityTokens securityTokens;

    private String repoTypes;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        List<Plugin> srcRepoPlugins =
                pluginsService.getAvailableSourceRepoPlugins();
        List<String> repoTypes =
                srcRepoPlugins.stream().map(
                        repoExecutor -> repoExecutor.getClass()
                                .getAnnotation(RepoPlugin.class).value())
                        .collect(Collectors.toList());
        this.repoTypes = objectMapper.toJSON(repoTypes);
    }

    private List<String> getZanataUrls() {
        String supportedZanataServer = System.getProperty("zanata.server.urls");
        if (Strings.isNullOrEmpty(supportedZanataServer)) {
            supportedZanataServer =
                    "http://localhost:8080,http://localhost:8180";
        }
        return ImmutableList
                .copyOf(Splitter.on(",").omitEmptyStrings().trimResults()
                        .split(supportedZanataServer));
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (serverURL == null) {
            serverURL = getBaseUrl((HttpServletRequest) servletRequest);
        }

        List<String> zanataUrls = getZanataUrls();
        String appRoot =
                UrlUtil.appRootAbsoluteUrl((HttpServletRequest) servletRequest);

        servletRequest.setAttribute("srcRepoPlugins", repoTypes);
        servletRequest.setAttribute("zanataOAuthUrls",
                objectMapper.toJSON(getZanataOAuthUrls(appRoot, zanataUrls)));
        servletRequest.setAttribute("cronOptions",
                objectMapper
                        .toJSON(CronType.toMapWithDisplayAsKey()));

        UserAccount account = securityTokens.getAccount();
        String accountAsJson = objectMapper.toJSON(account);
        servletRequest.setAttribute("user", accountAsJson);

        servletRequest
                .setAttribute("websocketPort", System.getenv("websocket_port"));

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private Map<String, String> getZanataOAuthUrls(String appRoot,
            List<String> zanataUrls) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (String zanataUrl : zanataUrls) {
            builder.put(zanataUrl, UrlUtil.zanataOAuthUrl(appRoot, zanataUrl));
        }

        return builder.build();
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
