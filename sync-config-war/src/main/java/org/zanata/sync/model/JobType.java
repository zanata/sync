package org.zanata.sync.model;

import java.io.File;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public enum JobType {
    SERVER_SYNC,
    REPO_SYNC;

    public JobKey toJobKey(Long workId) {
        return new JobKey(this.name(), workId.toString());
    }

    public TriggerKey toTriggerKey(Long workId) {
        return new TriggerKey(this.name(), workId.toString());
    }

    public File baseWorkDir(File base) {
        return new File(base, this.name().toLowerCase());
    }
}
