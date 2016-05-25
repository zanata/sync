package org.zanata.sync.common.validator;

import org.zanata.sync.common.Messages;
import org.zanata.sync.common.plugin.Validator;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class EmailValidator implements Validator {
    private final org.apache.commons.validator.routines.EmailValidator
        emailValidator =
        org.apache.commons.validator.routines.EmailValidator.getInstance();

    @Override
    public String validate(String value) {
        if(emailValidator.isValid(value)) {
            return null;
        }
        return Messages.getString("validation.email.invalid", value);
    }
}
