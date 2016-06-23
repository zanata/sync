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
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.dto.ZanataAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.util.JSONObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * This filter is responsible for pre-load necessary data for the frontend
 * javascript app.
 * Filter mapping url is defined in web.xml to specify order of execution.
 *
 * see index.jsp
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "frontendDataProviderFilter")
public class FrontendDataProviderFilter implements Filter {

    @Inject
    private PluginsService pluginsService;

    @Inject
    private JSONObjectMapper objectMapper;

    @Inject
    private SecurityTokens securityTokens;

    private String zanataServerUrls;
    private String plugins;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String supportedZanataServer = System.getProperty("zanata.server.urls");
        if (Strings.isNullOrEmpty(supportedZanataServer)) {
            supportedZanataServer =
                    "http://localhost:8080/zanata,http://localhost:8180/zanata";
        }
        List<String> urls = ImmutableList
                .copyOf(Splitter.on(",").omitEmptyStrings().trimResults()
                        .split(supportedZanataServer));
        zanataServerUrls = objectMapper.toJSON(urls);

        pluginsService.init();
        List<RepoExecutor> srcRepoPlugins =
                pluginsService.getAvailableSourceRepoPlugins();
        // TODO maybe use a DTO for json serialization
        List<Map<String, Object>> pluginsList =
                srcRepoPlugins.stream().map(plugin -> {
                    Map<String, Object> pluginMap = Maps.newHashMap();
                    pluginMap.put("name", plugin.getName());
                    pluginMap.put("description", plugin.getDescription());
                    pluginMap.put("fields", plugin.getFields());
                    return pluginMap;
                }).collect(Collectors.toList());
        plugins = objectMapper.toJSON(pluginsList);
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
                    throws IOException, ServletException {
        servletRequest.setAttribute("srcRepoPlugins", plugins);
        servletRequest.setAttribute("zanataServerUrls", zanataServerUrls);

        ZanataAccount account = securityTokens.getAccount();
        String zanataServer = account != null ? account.getZanataServer() : "";
        String accountAsJson = objectMapper.toJSON(account);


        servletRequest.setAttribute("user", accountAsJson);
        servletRequest.setAttribute("zanata", zanataServer);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
