package org.zanata.sync.model;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.zanata.sync.common.model.SyncOption;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "Sync_Work_Config_table")
@Access(AccessType.FIELD)
public class SyncWorkConfig extends PersistModel {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    private String syncToZanataCron;
    private String syncToRepoCron;

    @Enumerated(EnumType.STRING)
    private SyncOption syncToZanataOption;

    @Transient
    private Map<String, String> srcRepoPluginConfig =
            new HashMap<>();
    @Transient
    // TODO pahuang rename
    private Map<String, String> transServerPluginConfig =
            new HashMap<>();

    private String srcRepoPluginConfigJson;
    private String transServerConfigJson;

    private String srcRepoPluginName;

    private String encryptionKey;

    private boolean syncToServerEnabled = true;

    private boolean syncToRepoEnabled = true;

    @Setter(AccessLevel.PROTECTED)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public SyncWorkConfig(Long id, String name, String description,
            String syncToZanataCron, String syncToRepoCron,
            SyncOption syncToZanataOption,
            Map<String, String> srcRepoPluginConfig, String srcRepoPluginName,
            Map<String, String> transServerPluginConfig,
            String encryptionKey,
            boolean syncToServerEnabled, boolean syncToRepoEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncToZanataOption = syncToZanataOption;
        this.srcRepoPluginConfig = srcRepoPluginConfig;
        this.srcRepoPluginName = srcRepoPluginName;
        this.transServerPluginConfig = transServerPluginConfig;
        this.encryptionKey = encryptionKey;
        this.syncToServerEnabled = syncToServerEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
        this.syncToZanataCron = syncToZanataCron;
        this.syncToRepoCron = syncToRepoCron;

        transServerConfigJson = marshallToJson(transServerPluginConfig);
        srcRepoPluginConfigJson = marshallToJson(srcRepoPluginConfig);
    }

    private static String marshallToJson(Map<String, String> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
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
                Objects.equals(syncToRepoCron, that.syncToRepoCron) &&
                Objects.equals(syncToZanataCron, that.syncToZanataCron) &&
                Objects.equals(syncToZanataOption, that.syncToZanataOption) &&
                Objects.equals(srcRepoPluginConfig, that.srcRepoPluginConfig) &&
                Objects.equals(transServerPluginConfig,
                        that.transServerPluginConfig) &&
                Objects.equals(srcRepoPluginName, that.srcRepoPluginName) &&
                Objects.equals(syncToServerEnabled, that.syncToServerEnabled) &&
                Objects.equals(syncToRepoEnabled, that.syncToRepoEnabled);
    }

    @PostLoad
    protected void postLoad() {
        srcRepoPluginConfig = readJsonValue(srcRepoPluginConfigJson);
        transServerPluginConfig = readJsonValue(transServerConfigJson);
    }

    private static Map<String, String> readJsonValue(
            String srcRepoPluginConfigJson) {
        try {
            return new ObjectMapper().readerFor(Map.class).readValue(
                    srcRepoPluginConfigJson);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, name, description, syncToRepoCron,
                syncToZanataCron, syncToZanataOption, srcRepoPluginConfig,
                transServerPluginConfig, srcRepoPluginName,
                createdDate, syncToServerEnabled,
                syncToRepoEnabled);
    }
}
