package org.zanata.sync.model;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Setter
@NoArgsConstructor
public class SystemSettings implements Serializable {
    @NotEmpty
    private String dataPath;

    @NotNull
    private List<String> fieldsNeedEncryption = Lists.newArrayList("apiKey");

    public void updateSettings(List<String> fieldsNeedEncryption) {
        this.fieldsNeedEncryption = fieldsNeedEncryption;
    }
}
