package org.zanata.sync.validation;

import java.util.Map;
import javax.validation.Validator;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.sync.dto.RepoAccountDto;
import org.zanata.sync.system.ResourceProducer;
import com.google.common.collect.Sets;

import static org.assertj.core.data.MapEntry.entry;

public class GenericDTOValidatorTest {

    private GenericDTOValidator validator;
    private Validator validatorImpl = new ResourceProducer().getValidator();

    @BeforeClass
    public static void setEnv() {
        SupportedRepoValidator.supportedRepoTypes = Sets.newHashSet("git");
    }

    @AfterClass
    public static void cleanEnv() {
        SupportedRepoValidator.supportedRepoTypes = null;
    }

    @Before
    public void setUp() {
        validator = new GenericDTOValidator(validatorImpl);
    }

    @Test
    public void canValidateEmptyRepoAccount() {
        Map<String, String> errors = validator.validate(new RepoAccountDto());

        Assertions.assertThat(errors).containsOnly(
                entry("repoType", "may not be null"),
                entry("repoHostname", "may not be null"));
    }

    @Test
    public void canValidateInvalidRepoTypeAndUrl() {
        Map<String, String> errors = validator.validate(
                new RepoAccountDto(null, null, "something", "unknown", null));

        Assertions.assertThat(errors).containsOnly(
                entry("repoType", "unsupported source repository type"),
                entry("repoHostname", "must be a valid URL"));
    }

}
