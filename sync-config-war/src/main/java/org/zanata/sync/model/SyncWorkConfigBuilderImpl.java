package org.zanata.sync.model;

import javax.enterprise.context.RequestScoped;

import org.zanata.sync.controller.SyncWorkForm;
import org.zanata.sync.util.CronType;
import com.google.common.base.Strings;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class SyncWorkConfigBuilderImpl implements SyncWorkConfigBuilder {

    @Override
    public SyncWorkConfig buildObject(SyncWorkForm syncWorkForm) {
        return new SyncWorkConfig(syncWorkForm.getId(),
            syncWorkForm.getName(),
            syncWorkForm.getDescription(),
                syncWorkForm.getSyncToServerCron().getExpression(),
                syncWorkForm.getSyncToRepoCron().getExpression(),
                syncWorkForm.getSyncToServerOption(),
                syncWorkForm.getSrcRepoPluginConfig(),
            syncWorkForm.getSrcRepoPluginName(),
            syncWorkForm.getTransServerPluginConfig(),
            syncWorkForm.getEncryptionKey(),
            syncWorkForm.isSyncToServerEnabled(),
            syncWorkForm.isSyncToRepoEnabled());
    }

    @Override
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
            form.setSyncToServerOption(syncWorkConfig.getSyncToZanataOption());
            form.setSyncToServerCron(
                    CronType.getTypeFromExpression(
                            syncWorkConfig.getSyncToZanataCron()));
        }

        if (!Strings.isNullOrEmpty(syncWorkConfig.getSyncToRepoCron())) {
            form.setSyncToRepoCron(CronType.getTypeFromExpression(
                    syncWorkConfig.getSyncToRepoCron()));
        }

        form.setSyncToRepoEnabled(syncWorkConfig.isSyncToRepoEnabled());
        form.setSyncToServerEnabled(syncWorkConfig.isSyncToServerEnabled());
        return form;
    }
}
