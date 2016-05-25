package org.zanata.sync.common.model;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class UsernamePasswordCredential implements Credentials<String> {

    private final String username;
    private final String apiKey;

    public UsernamePasswordCredential(String username,
            String apiKey) {
        this.username = username;
        this.apiKey = apiKey;
    }

    public String getUsername() {
        return username;
    }

    public String getSecret() {
        return apiKey;
    }
}
