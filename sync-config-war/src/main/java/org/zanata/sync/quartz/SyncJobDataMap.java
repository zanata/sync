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
package org.zanata.sync.quartz;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;

/**
 * Helper class to store and retrieve data from job detail data map.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SyncJobDataMap {
    static final String SYNC_WORK_CONFIG_KEY = "value";
    static final String JOB_TYPE_KEY = "jobType";

    private final JobDataMap dataMap;

    private SyncJobDataMap(JobDataMap dataMap) {
        this.dataMap = dataMap;
    }


    public static SyncJobDataMap fromContext(JobExecutionContext context) {
        return new SyncJobDataMap(context.getJobDetail().getJobDataMap());
    }

    public static SyncJobDataMap fromJobDetail(JobDetail jobDetail) {
        return new SyncJobDataMap(jobDetail.getJobDataMap());
    }

    private <T> T get(String key) {
        return (T) dataMap.get(key);
    }

    public SyncWorkConfig getWorkConfig() {
        return get(SYNC_WORK_CONFIG_KEY);
    }

    public JobType getJobType() {
        return get(JOB_TYPE_KEY);
    }

    public SyncJobDataMap storeWorkConfig(SyncWorkConfig workConfig) {
        dataMap.put(SYNC_WORK_CONFIG_KEY, workConfig);
        return this;
    }

    public SyncJobDataMap storeJobType(JobType jobType) {
        dataMap.put(JOB_TYPE_KEY, jobType);
        return this;
    }

}
