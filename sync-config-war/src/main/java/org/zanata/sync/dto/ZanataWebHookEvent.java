package org.zanata.sync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * {"username":"admin","project":"test-repo","version":"master","docId":"book","locale":"ja","wordDeltasByState":{"New":-4,"Translated":4},"type":"DocumentStatsEvent"}
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZanataWebHookEvent {
    private String username;
    private String project;
    private String version;
    private String locale;

    public String getUsername() {
        return username;
    }

    public String getProject() {
        return project;
    }

    public String getVersion() {
        return version;
    }

    public String getLocale() {
        return locale;
    }
}
