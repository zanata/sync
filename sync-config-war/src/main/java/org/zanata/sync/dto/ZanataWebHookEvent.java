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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

/**
 * payload:
 * <pre>
 *     {
 *        "projectSlug": "gettext-project",
 *        "versionSlug": "master",
 *        "localeId": "zh-CN",
 *        "type": "TranslationChangedEvent"
 *     }
 * </pre>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZanataWebHookEvent {
    private String username;
    private String projectSlug;
    private String versionSlug;
    private String localeId;

    public String getUsername() {
        return username;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public String getVersionSlug() {
        return versionSlug;
    }

    public String getLocaleId() {
        return localeId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("projectSlug", projectSlug)
                .add("version", versionSlug)
                .add("localeId", localeId)
                .toString();
    }
}
