package org.zanata.sync.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import com.google.common.collect.Lists;

import static javax.persistence.AccessType.FIELD;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Table(name = "System_Settings_table")
@Access(FIELD)
public class SystemSettings implements Serializable {
    public static final String FIELDS_NEED_ENCRYPTION = "fields.need.encryption";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private int version;

    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setSetting(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
