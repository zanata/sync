package org.zanata.sync.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.util.CronType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@NoArgsConstructor
public class SyncWorkForm implements Serializable {
    public final static String repoSettingsPrefix = "sourceRepoSettings.";

    @Setter
    private Long id;

    @Size(min = 5, max = 100)
    @NotEmpty
    @Setter
    private String name;

    @Size(max = 255)
    @Setter
    private String description;

    @Setter
    private CronType syncToZanataCron;

    @Setter
    private SyncOption syncOption = SyncOption.SOURCE;

    @Setter
    private CronType syncToRepoCron;

    @NotEmpty
    @Size(max = 255)
    @Setter
    private String srcRepoPluginName;

    /**
     * If specified, it will encrypt field listed in config properties whose
     * value matches key in {@link SyncWorkForm#srcRepoPluginConfig} and {@link
     * SyncWorkForm#transServerPluginConfig}
     */
    @Size(max = 16)
    @Setter
    private String encryptionKey;

    @Setter
    private Map<String, String> srcRepoPluginConfig = new HashMap<>();


    @Setter
    private Map<String, String> transServerPluginConfig = new HashMap<>();

    @Setter
    private boolean syncToZanataEnabled = true;

    @Setter
    private boolean syncToRepoEnabled = true;

}
