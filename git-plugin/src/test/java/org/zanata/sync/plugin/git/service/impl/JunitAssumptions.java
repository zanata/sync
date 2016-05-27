package org.zanata.sync.plugin.git.service.impl;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public final class JunitAssumptions {
    public static String assumeGitPasswordExists() {
        String password = System.getProperty("github.password");
        Assume.assumeThat(
                "github password is provided as system property: github.password",
                password,
                CoreMatchers.notNullValue());
        return password;
    }

    public static String assumeGitUsernameExists() {
        String username = System.getProperty("github.user");
        Assume.assumeThat(
                "github username is provided as system property: github.user",
                username,
                CoreMatchers.notNullValue());
        return username;
    }

    static String assumeZanataUrlExists() {
        String zanataUrl = System.getProperty("zanata.url");
        Assume.assumeThat(
                "zanata test server is running and url is given as system property: zanata.url",
                zanataUrl,
                CoreMatchers.notNullValue());
        return zanataUrl;
    }

    static String assumeGithubRepoUrlExists() {
        String githubUrl = System.getProperty("github.url");
        Assume.assumeThat(
                "github repo url is given as system property: github.url",
                githubUrl,
                CoreMatchers.notNullValue());
        return githubUrl;
    }
}
