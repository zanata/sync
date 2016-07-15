package org.zanata.sync.common.validator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class UrlFieldValidatorTest {
    private UrlFieldValidator urlValidator = new UrlFieldValidator();

    @Test
    public void validUrlTest() {
        String validUrl = "http://zanata.org";
        Assertions.assertThat(urlValidator.validate(validUrl)).isNull();
    }

    @Test
    public void invalidUrlTest() {
        String invalidUrl = "zanata.org";
        Assertions.assertThat(urlValidator.validate(invalidUrl)).isNotNull();
    }

    @Test
    public void localUrlIsValid() {
        String validUrl = "http://localhost:8080/zanata/";
        Assertions.assertThat(urlValidator.validate(validUrl)).isNull();
    }

    @Test
    public void fileUrlIsValid() {
        String fileUrl = "file:///home/work/project.git";
        Assertions.assertThat(urlValidator.validate(fileUrl)).isNull();
    }
}
