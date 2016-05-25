package org.zanata.sync.common.plugin;

import lombok.Getter;
import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.SyncOption;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class TranslationServerExecutor implements Plugin {

    @Getter
    public final Map<String, Field> fields = new LinkedHashMap<String, Field>();

    public TranslationServerExecutor(Map<String, String> fields) {
        initFields();
        if (fields != null) {
            fields.entrySet().stream()
                .filter(entry -> this.fields.containsKey(entry.getKey()))
                .forEach(entry -> {
                    this.fields.get(entry.getKey()).setValue(entry.getValue());
                });
        }
    }
    /**
     * Pull from server into given directory
     *
     * @param dir - directory to pull to
     * @param syncOption - source only, translations only, or both
     */
    public abstract void pullFromServer(File dir, SyncOption syncOption)
        throws Exception;

    /**
     * Push files to server from given directory
     *
     * @param dir - directory to push from
     * @param syncOption - source only, translations only, or both
     */
    public abstract void pushToServer(File dir, SyncOption syncOption)
        throws Exception;
}
