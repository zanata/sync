package org.zanata.sync.common.validator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class EmailFieldValidatorTest {

    private EmailFieldValidator emailValidator = new EmailFieldValidator();

    @Test
    public void validEmailTest() {
        String validEmail = "test@domain.com";
        String message = emailValidator.validate(validEmail);
        Assertions.assertThat(message).isNull();
    }

    @Test
    public void invalidEmailTest() {
        String invalidEmail = "testdomain.com";
        String message = emailValidator.validate(invalidEmail);
        Assertions.assertThat(message).isNotNull();
    }
}
