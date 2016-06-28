package org.zanata.sync.common.validator;

import org.zanata.sync.common.Messages;
import org.zanata.sync.common.plugin.Validator;

import static org.apache.commons.validator.routines.UrlValidator.ALLOW_ALL_SCHEMES;
import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class UrlValidator implements Validator {

    private final org.apache.commons.validator.routines.UrlValidator
        urlValidator = new
        org.apache.commons.validator.routines.UrlValidator(
            ALLOW_LOCAL_URLS + ALLOW_ALL_SCHEMES);

    @Override
    public String validate(String value) {
        if (value == null || value.length() <= 0) {
            return Messages.getString("validation.string.notEmpty");
        }
        if (urlValidator.isValid(value)) {
            return null;
        }
        return Messages.getString("validation.url.invalid", value);
    }
}
