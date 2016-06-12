package org.zanata.sync.plugin.git;

import java.util.Collections;
import java.util.Map;

import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.FieldType;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.validator.UrlValidator;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@RepoPlugin
public class Plugin extends RepoExecutor {
    private static final String NAME = "git";
    private static final String DESCRIPTION = Messages.getString("plugin.description");

    public Plugin(Map<String, String> fields) {
        super(fields);
    }

    @Override
    public void initFields() {
        Field urlField = new Field("url", Messages.getString("field.url.label"),
                "https://github.com/zanata/zanata-server.git", null,
                new UrlValidator(), false, FieldType.TEXT);
        Field branchField =
                new Field("branch", Messages.getString("field.branch.label"),
                        "master", Messages.getString("field.branch.tooltip"), false, FieldType.TEXT);
        Field usernameField =
                new Field("username",
                        Messages.getString("field.username.label"),
                        "", Messages.getString("field.username.tooltip"),
                        false, FieldType.TEXT);
        Field apiKeyField =
                new Field("secret", Messages.getString("field.secret.label"),
                        "",
                        Messages.getString("field.secret.tooltip"),
                        true, FieldType.TEXT);

        fields.put(urlField.getKey(), urlField);
        fields.put(branchField.getKey(), branchField);
        fields.put(usernameField.getKey(), usernameField);
        fields.put(apiKeyField.getKey(), apiKeyField);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

}
