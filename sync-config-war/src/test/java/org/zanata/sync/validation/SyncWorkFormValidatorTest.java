package org.zanata.sync.validation;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.plugin.git.GitPlugin;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.util.CronType;
import com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.when;

public class SyncWorkFormValidatorTest {

    private static final boolean DISABLE_ZANATA_SYNC = false;
    private static final boolean DISABLE_REPO_SYNC = false;
    private static final boolean ENABLE_ZANATA_SYNC = true;
    private static final boolean ENABLE_REPO_SYNC = true;
    private SyncWorkFormValidator validator;
    @Mock
    private PluginsService pluginService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new SyncWorkFormValidator();
        validator.pluginsService = pluginService;
        when(pluginService.getSourceRepoPlugin("git"))
                .thenReturn(Optional.of(new GitPlugin()));
        when(pluginService.getSourceRepoPlugin("unknown"))
                .thenReturn(Optional.empty());
        SupportedRepoValidator.supportedRepoTypes = Sets.newHashSet("git");
    }

    @Test
    public void canValidateCommonFields() {
        SyncWorkForm form =
                new SyncWorkForm("a", null, null, null, null,
                        null, DISABLE_ZANATA_SYNC,
                        DISABLE_REPO_SYNC, null, null, null);
        Map<String, String> errors = validator.validateForm(form);
        assertThat(errors).containsOnly(
                entry("name", "size must be between 5 and 100"),
                entry("enabledJobs",
                        "At least one type of job should be enabled"),
                entry("srcRepoUrl", "may not be empty"),
                entry("srcRepoAccountId", "may not be null"));
    }

    @Test
    public void canValidateZantaSyncFields() {
        SyncWorkForm form =
                new SyncWorkForm("abcde", null, null, null, null,
                        null, ENABLE_ZANATA_SYNC,
                        DISABLE_REPO_SYNC, null, null, 1L);
        Map<String, String> errors = validator.validateForm(form);

        assertThat(errors)
                .containsOnlyKeys("syncToZanataCron",
                        "syncOption", "srcRepoUrl");

    }

    @Test
    public void canValidateRepoSyncFields() {
        SyncWorkForm form =
                new SyncWorkForm("abcde", null, null, null, null,
                        null, DISABLE_ZANATA_SYNC,
                        ENABLE_REPO_SYNC, null, null, null);
        Map<String, String> errors = validator.validateForm(form);

        assertThat(errors)
                .containsOnlyKeys("srcRepoUrl", "syncToRepoCron", "srcRepoAccountId");
    }

    /*@Test
    public void canValidateUnknownSourceRepoPluginName() {
        SyncWorkForm form =
                new SyncWorkForm("abcde", null, null, SyncOption.SOURCE,
                        CronType.MANUAL, null,
                        DISABLE_ZANATA_SYNC, ENABLE_REPO_SYNC,
                        null, null, repoAccId);
        Map<String, String> errors = validator.validateForm(form);

        assertThat(errors)
                .containsOnly(
                        entry("srcRepoPluginName",
                                "unsupported source repository type"),
                        entry("srcRepoUrl", "may not be empty"));
    }*/
}
