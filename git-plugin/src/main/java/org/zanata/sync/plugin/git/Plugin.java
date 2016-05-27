package org.zanata.sync.plugin.git;

import java.io.File;
import java.util.Map;

import org.eclipse.jgit.util.StringUtils;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.exception.RepoSyncException;
import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.FieldType;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.common.model.UsernamePasswordCredential;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.validator.UrlValidator;
import org.zanata.sync.plugin.git.service.impl.GitSyncService;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@RepoPlugin
public class Plugin extends RepoExecutor {
    private final String name = "Git plugin";
    private final String description = Messages.getString("plugin.description");
    private final GitSyncService gitSyncService;

    public final static String DEFAULT_BRANCH = "master";

    public Plugin(Map<String, String> fields) {
        super(fields);
        gitSyncService = new GitSyncService(
            new UsernamePasswordCredential(getField("username").getValue(),
                    getField("apiKey").getValue()));
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
                new Field("apiKey", Messages.getString("field.apiKey.label"),
                        "",
                        Messages.getString("field.apiKey.tooltip"),
                        true, FieldType.TEXT);

        fields.put(urlField.getKey(), urlField);
        fields.put(branchField.getKey(), branchField);
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
    public void cloneRepo(File dir) throws RepoSyncException {
        gitSyncService.cloneRepo(getField("url").getValue(),
            getBranch(), dir);
    }

    @Override
    public void pushToRepo(File dir, SyncOption syncOption)
            throws RepoSyncException {
        gitSyncService
                .syncTranslationToRepo(getFields().get("url").getValue(),
                        getBranch(), dir);
    }

    /**
     * Default to {@link Plugin#DEFAULT_BRANCH} branch if it is not specify
     */
    private String getBranch() {
        String branch = getField("branch").getValue();
        if (StringUtils.isEmptyOrNull(branch)) {
            return DEFAULT_BRANCH;
        }
        return branch;
    }
}
