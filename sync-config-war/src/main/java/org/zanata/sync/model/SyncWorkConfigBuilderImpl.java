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
        String syncToZanataCron = syncWorkForm.getSyncToZanataCron().getExpression();
        String syncToRepoCron = syncWorkForm.getSyncToRepoCron().getExpression();
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

    // TODO to be deleted
    public SyncWorkForm buildForm(SyncWorkConfig syncWorkConfig) {

        SyncWorkForm form = new SyncWorkForm();
        form.setId(syncWorkConfig.getId());
        form.setName(syncWorkConfig.getName());
        form.setDescription(syncWorkConfig.getDescription());
        form.setEncryptionKey(syncWorkConfig.getEncryptionKey());
        form.setSrcRepoPluginName(syncWorkConfig.getSrcRepoPluginName());

        form.setSrcRepoPluginConfig(syncWorkConfig.getSrcRepoPluginConfig());
        form.setTransServerPluginConfig(
            syncWorkConfig.getTransServerPluginConfig());

        if (!Strings.isNullOrEmpty(syncWorkConfig.getSyncToZanataCron())) {
            form.setSyncOption(syncWorkConfig.getSyncToZanataOption());
            form.setSyncToZanataCron(
                    CronType.getTypeFromExpression(
                            syncWorkConfig.getSyncToZanataCron()));
        }

        if (!Strings.isNullOrEmpty(syncWorkConfig.getSyncToRepoCron())) {
            form.setSyncToRepoCron(CronType.getTypeFromExpression(
                    syncWorkConfig.getSyncToRepoCron()));
        }

        form.setSyncToRepoEnabled(syncWorkConfig.isSyncToRepoEnabled());
        form.setSyncToZanataEnabled(syncWorkConfig.isSyncToServerEnabled());
        return form;
    }
}
