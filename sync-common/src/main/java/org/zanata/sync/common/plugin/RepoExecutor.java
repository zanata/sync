package org.zanata.sync.common.plugin;

import lombok.Getter;
import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.common.exception.RepoSyncException;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class RepoExecutor implements Plugin {

    @Getter
    public final HashMap<String, Field> fields =
        new LinkedHashMap<String, Field>();

    public RepoExecutor(Map<String, String> fields) {
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
     * Clone from source repository
     *
     * @param dir - directory to clone to
     */
    public abstract void cloneRepo(File dir) throws RepoSyncException;

    /**
     * Push changes to source repository
     *
     * @param dir - directory to push from
     * @param syncOption - source only, translations only, or both
     * @return push successful
     */
    public abstract void pushToRepo(File dir, SyncOption syncOption) throws
        RepoSyncException;
}
