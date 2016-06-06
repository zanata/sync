package org.zanata.sync.common.plugin;

import lombok.Getter;
import org.zanata.sync.common.model.Field;

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

}
