package org.zanata.sync.common.validator;

import lombok.AllArgsConstructor;
import org.zanata.sync.common.Messages;
import org.zanata.sync.common.plugin.FieldValidator;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
public class StringFieldValidator implements FieldValidator {
    private Integer minLength;
    private Integer maxLength;
    //Allow empty string
    private boolean notEmpty;

    @Override
    public String validate(String value) {
        if (notEmpty) {
            if (value == null || value.length() <= 0) {
                return Messages.getString("validation.string.notEmpty");
            }
        }
        if (minLength != null) {
            if (value == null || value.length() < minLength) {
                return Messages.getString("validation.string.minlength", minLength);
            }
        }
        if (maxLength != null) {
            if (value == null || value.length() > maxLength) {
                return Messages.getString("validation.string.maxlength", maxLength);
            }
        }
        return null;
    }
}
