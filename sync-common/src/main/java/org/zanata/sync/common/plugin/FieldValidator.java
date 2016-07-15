package org.zanata.sync.common.plugin;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface FieldValidator extends Serializable {
    String validate(String value);
}
