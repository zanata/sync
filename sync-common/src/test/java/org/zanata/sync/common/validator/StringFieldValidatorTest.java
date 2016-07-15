package org.zanata.sync.common.validator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class StringFieldValidatorTest {
    private StringFieldValidator stringValidator;

    @Test
    public void test1() {
        int minLength = 0;
        int maxLength = 5;
        boolean notEmpty = true;
        stringValidator = new StringFieldValidator(minLength, maxLength, notEmpty);

        String value1 = "abc";
        Assertions.assertThat(stringValidator.validate(value1)).isNull();

        String value2 = "abc123";
        Assertions.assertThat(stringValidator.validate(value2)).isNotNull();

        String value3 = "";
        Assertions.assertThat(stringValidator.validate(value3)).isNotNull();
    }

    @Test
    public void test2() {
        int minLength = 2;
        int maxLength = 5;
        boolean notEmpty = true;
        stringValidator = new StringFieldValidator(minLength, maxLength, notEmpty);

        String value1 = "a";
        Assertions.assertThat(stringValidator.validate(value1)).isNotNull();

        String value2 = "ab";
        Assertions.assertThat(stringValidator.validate(value2)).isNull();

        String value3 = "abc123";
        Assertions.assertThat(stringValidator.validate(value3)).isNotNull();

        String value4 = "";
        Assertions.assertThat(stringValidator.validate(value4)).isNotNull();
    }

    @Test
    public void test3() {
        boolean notEmpty = true;
        stringValidator = new StringFieldValidator(null, null, notEmpty);

        String value1 = "a";
        Assertions.assertThat(stringValidator.validate(value1)).isNull();

        String value2 = null;
        Assertions.assertThat(stringValidator.validate(value2)).isNotNull();

        String value3 = "";
        Assertions.assertThat(stringValidator.validate(value3)).isNotNull();
    }
}
