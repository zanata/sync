package org.zanata.sync.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.FieldType;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.plugin.TranslationServerExecutor;
import org.zanata.sync.i18n.Messages;
import org.zanata.sync.plugin.zanata.Plugin;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.util.CronType;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class HasFormController implements Serializable {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected Map<String, String> errors = new HashMap<>();

    @Getter
    protected RepoExecutor selectedSrcPlugin;

    @Getter
    protected TranslationServerExecutor selectedServerPlugin;

    protected SyncWorkForm form;

    private List<RepoExecutor> repoExecutors;

    private List<Field> syncOptions;

    abstract protected Messages getMessage();

    abstract protected PluginsService getPluginService();

    abstract public String onSubmit() throws IOException;

    abstract public SyncWorkForm getForm();

    @PostConstruct
    public void init() {
        if(!getRepoExecutors().isEmpty()) {
            selectedSrcPlugin = getRepoExecutors().get(0);
        }
        selectedServerPlugin = new Plugin();
    }

    public List<RepoExecutor> getRepoExecutors() {
        if (repoExecutors == null) {
            repoExecutors =
                getPluginService().getAvailableSourceRepoPlugins();
        }
        return repoExecutors;
    }

    public List<Field> getSelectedSrcPluginFields() {
        if(selectedSrcPlugin != null) {
            return new ArrayList<>(selectedSrcPlugin.getFields().values());
        }
        return Collections.emptyList();
    }

    public List<Field> getSelectedServerPluginFields() {
        if(selectedServerPlugin != null) {
            return new ArrayList<>(selectedServerPlugin.getFields().values());
        }
        return Collections.emptyList();
    }

    public List<Field> getSyncOptions() {
        if (syncOptions == null) {
            syncOptions = new ArrayList<>();
            syncOptions.add(new Field(SyncOption.SOURCE.name(),
                    getMessage()
                            .get("jsf.work.syncType.sourceOnly.explanation"),
                    "", "", false, FieldType.TEXT));
            syncOptions
                    .add(new Field(SyncOption.TRANSLATIONS.name(), getMessage()
                            .get("jsf.work.syncType.translationsOnly.explanation"),
                            "", "", false, FieldType.TEXT));
            syncOptions.add(new Field(SyncOption.BOTH.name(), getMessage()
                    .get("jsf.work.syncType.both.explanation"), "", "",
                    false, FieldType.TEXT));
        }
        return syncOptions;
    }

    public boolean hasError(String fieldName) {
        return errors.containsKey(fieldName);
    }

    public String getErrorMessage(String fieldName) {
        return errors.get(fieldName);
    }

    public List<CronType> getSupportedIntervals() {
        return Lists.newArrayList(CronType.values());
    }
}
