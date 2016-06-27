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
package org.zanata.sync.jobs.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public enum JobDetailEntry {
    // entries for syncing to source repo
    srcRepoUrl, srcRepoUsername, srcRepoSecret, srcRepoBranch, srcRepoType,
    syncToZanataOption,
    // entries for syncing to Zanata
    zanataUrl, zanataUsername, zanataSecret;

    private static List<String> validKeys =
            ImmutableList.copyOf(values()).stream().map(Enum::name).collect(
                    Collectors.toList());

    /**
     * @param jobDetail
     * @return any keys that is not part of this enum
     */
    public static List<String> unknownEntries(Map<String, String> jobDetail) {
        List<String> result = jobDetail.keySet().stream()
                .filter(key -> !validKeys.contains(key)).collect(
                        Collectors.toList());
        return ImmutableList.copyOf(result);
    }


    public String extract(Map<String, String> jobDetail) {
        return jobDetail.get(this.name());
    }
}
