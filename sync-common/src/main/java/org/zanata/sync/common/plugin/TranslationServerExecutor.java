package org.zanata.sync.common.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.zanata.sync.common.model.Field;
import lombok.Getter;

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

}
