package org.zanata.sync.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@NoArgsConstructor
public class SyncWorkConfig extends PersistModel {

    @Setter
    private Long id;
    private String name;
    private String description;

    private JobConfig syncToServerConfig;
    private JobConfig syncToRepoConfig;

    private Map<String, String> srcRepoPluginConfig =
            new HashMap<>();
    private Map<String, String> transServerPluginConfig =
            new HashMap<>();

    private String srcRepoPluginName;

    private String encryptionKey;

    private boolean syncToServerEnabled = true;

    private boolean syncToRepoEnabled = true;

    @Setter(AccessLevel.PROTECTED)
    private Date createdDate;

    public SyncWorkConfig(Long id, String name, String description,
            JobConfig syncToServerConfig, JobConfig syncToRepoConfig,
            Map<String, String> srcRepoPluginConfig, String srcRepoPluginName,
            Map<String, String> transServerPluginConfig,
            String encryptionKey,
            boolean syncToServerEnabled, boolean syncToRepoEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncToServerConfig = syncToServerConfig;
        this.syncToRepoConfig = syncToRepoConfig;
        this.srcRepoPluginConfig = srcRepoPluginConfig;
        this.srcRepoPluginName = srcRepoPluginName;
        this.transServerPluginConfig = transServerPluginConfig;
        this.encryptionKey = encryptionKey;
        this.syncToServerEnabled = syncToServerEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncWorkConfig that = (SyncWorkConfig) o;
        return equalsExceptCreatedDate(that) &&
                Objects.equals(createdDate, that.createdDate);
    }

    public boolean equalsExceptCreatedDate(SyncWorkConfig that) {
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(syncToServerConfig, that.syncToServerConfig) &&
                Objects.equals(syncToRepoConfig, that.syncToRepoConfig) &&
                Objects.equals(srcRepoPluginConfig, that.srcRepoPluginConfig) &&
                Objects.equals(transServerPluginConfig,
                        that.transServerPluginConfig) &&
                Objects.equals(srcRepoPluginName, that.srcRepoPluginName) &&
                Objects.equals(syncToServerEnabled, that.syncToServerEnabled) &&
                Objects.equals(syncToRepoEnabled, that.syncToRepoEnabled);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, name, description, syncToServerConfig,
                syncToRepoConfig, srcRepoPluginConfig,
                transServerPluginConfig, srcRepoPluginName,
                createdDate, syncToServerEnabled,
                syncToRepoEnabled);
    }

    public void enableJob(JobType jobType, boolean enable) {
        if(jobType.equals(JobType.REPO_SYNC)) {
            syncToRepoEnabled = enable;
        } else if(jobType.equals(JobType.SERVER_SYNC)) {
            syncToServerEnabled = enable;
        }
    }
}
