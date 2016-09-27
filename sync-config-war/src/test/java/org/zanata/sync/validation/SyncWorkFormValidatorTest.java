package org.zanata.sync.validation;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.system.ResourceProducer;
import org.zanata.sync.util.CronType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class SyncWorkFormValidatorTest {

    private static final boolean DISABLE_ZANATA_SYNC = false;
    private static final boolean DISABLE_REPO_SYNC = false;
    private static final boolean ENABLE_ZANATA_SYNC = true;
    private static final boolean ENABLE_REPO_SYNC = true;
    private SyncWorkFormValidator validator;

    @Before
    public void setUp() {
        validator = new SyncWorkFormValidator(
                new ResourceProducer().getValidator());
    }

    @Test
    public void canValidateCommonFields() {
        SyncWorkForm form =
                new SyncWorkForm("a", null, null, null, null,
                        null, DISABLE_ZANATA_SYNC,
                        DISABLE_REPO_SYNC, null, null, null, null);
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
                        DISABLE_REPO_SYNC, null, null, 1L, null);
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
                        ENABLE_REPO_SYNC, null, null, null, null);
        Map<String, String> errors = validator.validate(form);

        assertThat(errors)
                .containsOnlyKeys("srcRepoUrl", "syncToRepoCron",
                        "srcRepoAccountId");
    }

    @Test
    public void canValidateProjectPaths() {
        SyncWorkForm form1 =
                new SyncWorkForm("a name", null, CronType.MANUAL,
                        SyncOption.SOURCE, CronType.MANUAL,
                        null, ENABLE_ZANATA_SYNC,
                        ENABLE_REPO_SYNC, "http://github.com/a", null, 1L,
                        "/a/b/zanata.xml");
        assertThat(validator.validate(form1)).containsOnly(
                entry("projectConfigs", "only accepts relative paths separated by comma"));

        SyncWorkForm form2 =
                new SyncWorkForm("a name", null, CronType.MANUAL,
                        SyncOption.SOURCE, CronType.MANUAL,
                        null, ENABLE_ZANATA_SYNC,
                        ENABLE_REPO_SYNC, "http://github.com/a", null, 1L,
                        "a/b/not_zanata.xml");
        assertThat(validator.validate(form2)).containsOnly(
                entry("projectConfigs", "only accepts relative paths separated by comma"));
    }

}
