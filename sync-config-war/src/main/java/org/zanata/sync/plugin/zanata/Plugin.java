package org.zanata.sync.plugin.zanata;

import java.util.Collections;
import java.util.Map;

import org.zanata.sync.common.annotation.TranslationServerPlugin;
import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.FieldType;
import org.zanata.sync.common.plugin.TranslationServerExecutor;
import org.zanata.sync.common.validator.StringFieldValidator;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@TranslationServerPlugin
public class Plugin extends TranslationServerExecutor {

    private static final String name = "Zanata Server";
    private final String description = Messages.getString("zanata.plugin.description");

    public Plugin(Map<String, String> fields) {
        super(fields);
    }

    public Plugin() {
        super(Collections.emptyMap());
    }

    @Override
    public void initFields() {

        Field usernameField =
                new Field("username",
                        Messages.getString("field.username.label"),
                        "", Messages.getString("field.username.tooltip"),
                        new StringFieldValidator(1, null, true), false, FieldType.TEXT);
        Field apiKeyField =
                new Field("apiKey", Messages.getString("field.secret.label"),
                        "",
                        Messages.getString("field.secret.tooltip"),
                        new StringFieldValidator(1, null, true), false, FieldType.TEXT);

        fields.put(usernameField.getKey(), usernameField);
        fields.put(apiKeyField.getKey(), apiKeyField);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, Field> getFields() {
        return fields;
    }

}
