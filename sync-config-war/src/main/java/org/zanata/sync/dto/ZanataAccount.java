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
package org.zanata.sync.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

/**
 * Light weight DTO representing a Zanata Account
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZanataAccount {
    private String zanataServer;
    private String username;
    private String apiKey;
    private Set<String> roles;
    private String email;
    private String name;
    private boolean enabled;

    public String getUsername() {
        return username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getZanataServer() {
        return zanataServer;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("zanataServer", zanataServer)
                .add("username", username)
                .add("apiKey", apiKey)
                .add("roles", roles)
                .add("email", email)
                .add("name", name)
                .add("enabled", enabled)
                .toString();
    }

    public void setZanataServer(String zanataServer) {
        this.zanataServer = zanataServer;
    }
}
