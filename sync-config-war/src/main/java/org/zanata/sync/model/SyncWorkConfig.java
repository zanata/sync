package org.zanata.sync.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.ws.rs.core.GenericType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.util.AutoCloseableDependentProvider;
import org.zanata.sync.util.JSONObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.zanata.sync.util.AutoCloseableDependentProvider.*;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "Sync_Work_Config_table")
@Access(AccessType.FIELD)
public class SyncWorkConfig {
    private static final Logger log =
            LoggerFactory.getLogger(SyncWorkConfig.class);

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
    private Map<String, String> srcRepoPluginConfig;
    @Transient
    private Map<String, String> transServerPluginConfig;

    private String srcRepoPluginConfigJson;
    private String transServerConfigJson;

    private String srcRepoPluginName;

    private String encryptionKey;

    private boolean syncToServerEnabled = true;

    private boolean syncToRepoEnabled = true;

    private String zanataUsername;

    @Setter(AccessLevel.PROTECTED)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToMany(mappedBy = "workConfig")
    private List<JobStatus> jobStatusHistory = Collections.emptyList();


    // TODO may not need the id parameter
    public SyncWorkConfig(Long id, String name, String description,
            String syncToZanataCron, String syncToRepoCron,
            SyncOption syncToZanataOption,
            String srcRepoPluginName,
            String encryptionKey,
            boolean syncToServerEnabled, boolean syncToRepoEnabled,
            String username, String srcRepoPluginConfigJson,
            String transServerPluginConfigJson) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.syncToZanataOption = syncToZanataOption;
        this.srcRepoPluginName = srcRepoPluginName;
        this.encryptionKey = encryptionKey;
        this.syncToServerEnabled = syncToServerEnabled;
        this.syncToRepoEnabled = syncToRepoEnabled;
        this.syncToZanataCron = syncToZanataCron;
        this.syncToRepoCron = syncToRepoCron;
        this.zanataUsername = username;

        this.srcRepoPluginConfigJson = srcRepoPluginConfigJson;
        this.transServerConfigJson = transServerPluginConfigJson;
    }

    public Map<String, String> getSrcRepoPluginConfig() {
        if (srcRepoPluginConfig == null) {
            srcRepoPluginConfig = fromJson(srcRepoPluginConfigJson);
        }
        return srcRepoPluginConfig;
    }

    public Map<String, String> getTransServerPluginConfig() {
        if (transServerPluginConfig == null) {
            transServerPluginConfig = fromJson(transServerConfigJson);
        }
        return transServerPluginConfig;
    }

    @PostLoad
    protected void postLoad() {
        getSrcRepoPluginConfig();
        getTransServerPluginConfig();
    }

    private static <T> T fromJson(String jsonString) {
        try (AutoCloseableDependentProvider<JSONObjectMapper> provider =
                forBean(JSONObjectMapper.class)) {
            JSONObjectMapper objectMapper = provider.getBean();
            return objectMapper.fromJSON(Map.class, jsonString);
        } catch (Exception e) {
            log.error("exception unmarshalling json: {}", jsonString);
            return null;
        }
    }

}
