package org.zanata.sync.common.model;

import lombok.Getter;
import lombok.Setter;
import org.zanata.sync.common.plugin.Validator;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
public class Field implements Serializable {
    private String key;
    private String label;

    @Setter
    private String value;

    private FieldType type;

    private String placeholder;
    private String tooltip;
    @JsonIgnore
    private Validator validator;
    private boolean isMasked;

    public Field() {
    }

    public Field(String key, String label, String placeholder, String tooltip,
        boolean isMasked, FieldType type) {
        this(key, label, placeholder, tooltip, null, isMasked, type);
    }

    public Field(String key, String label, String placeholder, String tooltip,
        Validator validator, boolean isMasked, FieldType type) {
        this.key = key;
        this.label = label;
        this.placeholder = placeholder;
        this.tooltip = tooltip;
        this.validator = validator;
        this.isMasked = isMasked;
        this.type = type;
    }
}
