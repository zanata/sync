package org.zanata.sync.model;

import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.zanata.sync.controller.SyncWorkForm;
import org.zanata.sync.util.CronType;
import org.zanata.sync.util.JSONObjectMapper;
import com.google.common.base.Strings;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class SyncWorkConfigBuilderImpl implements SyncWorkConfigBuilder {
    @Inject
    private JSONObjectMapper objectMapper;

    @Override
    public SyncWorkConfig buildObject(SyncWorkForm syncWorkForm) {
        String zanataUsername =
                syncWorkForm.getTransServerPluginConfig().get("username");
        String srcRepoPluginConfigJson =
                objectMapper.toJSON(syncWorkForm.getSrcRepoPluginConfig());
        String transServerPluginConfigJson =
                objectMapper.toJSON(syncWorkForm.getTransServerPluginConfig());
        CronType syncToZanataCron = syncWorkForm.getSyncToZanataCron();
        CronType syncToRepoCron = syncWorkForm.getSyncToRepoCron();
        return new SyncWorkConfig(syncWorkForm.getId(),
                syncWorkForm.getName(),
                syncWorkForm.getDescription(),
                syncToZanataCron,
                syncToRepoCron,
                syncWorkForm.getSyncOption(),
                syncWorkForm.getSrcRepoPluginName(),
                syncWorkForm.getEncryptionKey(),
                syncWorkForm.isSyncToZanataEnabled(),
                syncWorkForm.isSyncToRepoEnabled(),
                zanataUsername,
                srcRepoPluginConfigJson,
                transServerPluginConfigJson
        );
    }

}
