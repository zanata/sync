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
package org.zanata.sync.common.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

import static org.zanata.sync.common.util.StringUtil.mask;

/**
 * DTO for everything a sync job requires.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncJobDetail {
    @Size(min = 5, max = 255)
    private String srcRepoUrl;
    private String srcRepoUsername;
    private String srcRepoSecret;
    private String srcRepoBranch;

    @Size(min = 1, max = 255)
    private String srcRepoType;

    private String zanataUrl;

    @Size(min = 3, max = 255)
    private String zanataUsername;
    @NotNull
    private String zanataSecret;
    private SyncOption syncToZanataOption;

    private String localeId;

    private String projectConfigs;

    private String initiatedFromHostURL;

    public String getSrcRepoUrl() {
        return srcRepoUrl;
    }

    public String getSrcRepoUsername() {
        return srcRepoUsername;
    }

    public String getSrcRepoSecret() {
        return srcRepoSecret;
    }

    public String getSrcRepoBranch() {
        return srcRepoBranch;
    }

    public String getSrcRepoType() {
        return srcRepoType;
    }

    public String getZanataUrl() {
        return zanataUrl;
    }

    public String getZanataUsername() {
        return zanataUsername;
    }

    public String getZanataSecret() {
        return zanataSecret;
    }

    public SyncOption getSyncToZanataOption() {
        return syncToZanataOption;
    }

    public String getLocaleId() {
        return localeId;
    }

    public String getProjectConfigs() {
        return projectConfigs;
    }

    public String getInitiatedFromHostURL() {
        return initiatedFromHostURL;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("srcRepoUrl", srcRepoUrl)
                .add("srcRepoUsername", srcRepoUsername)
                .add("srcRepoSecret", mask(srcRepoSecret))
                .add("srcRepoBranch", srcRepoBranch)
                .add("srcRepoType", srcRepoType)
                .add("zanataUrl", zanataUrl)
                .add("zanataUsername", zanataUsername)
                .add("zanataSecret", mask(zanataSecret))
                .add("syncToZanataOption", syncToZanataOption)
                .add("localeId", localeId)
                .add("projectConfigs", projectConfigs)
                .toString();
    }

    public static class Builder {
        private final SyncJobDetail syncJobDetail;

        private Builder(SyncJobDetail syncJobDetail) {
            this.syncJobDetail = syncJobDetail;
        }

        public static Builder builder() {
            return new Builder(new SyncJobDetail());
        }

        public Builder setSrcRepoUrl(String srcRepoUrl) {
            syncJobDetail.srcRepoUrl = srcRepoUrl;
            return this;
        }

        public Builder setSrcRepoUsername(String srcRepoUsername) {
            syncJobDetail.srcRepoUsername = srcRepoUsername;
            return this;
        }

        public Builder setSrcRepoSecret(String srcRepoSecret) {
            syncJobDetail.srcRepoSecret = srcRepoSecret;
            return this;
        }

        public Builder setSrcRepoBranch(String srcRepoBranch) {
            syncJobDetail.srcRepoBranch = srcRepoBranch;
            return this;
        }

        public Builder setSrcRepoType(String srcRepoType) {
            syncJobDetail.srcRepoType = srcRepoType;
            return this;
        }

        public Builder setZanataUrl(String zanataUrl) {
            syncJobDetail.zanataUrl = zanataUrl;
            return this;
        }

        public Builder setZanataUsername(String zanataUsername) {
            syncJobDetail.zanataUsername = zanataUsername;
            return this;
        }

        public Builder setZanataSecret(String zanataSecret) {
            syncJobDetail.zanataSecret = zanataSecret;
            return this;
        }

        public Builder setSyncToZanataOption(
                SyncOption syncToZanataOption) {
            syncJobDetail.syncToZanataOption = syncToZanataOption;
            return this;
        }

        public Builder setLocaleId(String localeId) {
            syncJobDetail.localeId = localeId;
            return this;
        }

        public Builder setProjectConfigs(String projectConfigs) {
            syncJobDetail.projectConfigs = projectConfigs;
            return this;
        }

        public Builder setInitiatedFromHostURL(String url) {
            syncJobDetail.initiatedFromHostURL = url;
            return this;
        }

        public SyncJobDetail build() {
            return syncJobDetail;
        }
    }
}
