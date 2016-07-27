package org.zanata.sync.validation;

import java.util.Map;
import java.util.Optional;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.plugin.git.GitPlugin;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.system.ResourceProducer;
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
    private Validator validatorImpl = new ResourceProducer().getValidator();

    @Before
    public void setUp() {
        validator = new SyncWorkFormValidator(validatorImpl);
    }

    @Test
    public void canValidateCommonFields() {
        SyncWorkForm form =
                new SyncWorkForm("a", null, null, null, null,
                        null, DISABLE_ZANATA_SYNC,
                        DISABLE_REPO_SYNC, null, null, null);
        Map<String, String> errors = validator.validate(form);
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
        Map<String, String> errors = validator.validate(form);

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
        Map<String, String> errors = validator.validate(form);

        assertThat(errors)
                .containsOnlyKeys("srcRepoUrl", "syncToRepoCron",
                        "srcRepoAccountId");
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
