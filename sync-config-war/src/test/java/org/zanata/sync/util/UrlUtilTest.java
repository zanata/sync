package org.zanata.sync.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.sync.util.UrlUtil.concatUrlPath;

public class UrlUtilTest {
    @Test
    public void canContactPath() {
        assertThat(concatUrlPath("http://localhost/", "/oauth"))
                .isEqualTo("http://localhost/oauth");
        assertThat(concatUrlPath("http://localhost/", "oauth"))
                .isEqualTo("http://localhost/oauth");
        assertThat(concatUrlPath("http://localhost", "oauth"))
                .isEqualTo("http://localhost/oauth");
        assertThat(concatUrlPath("http://localhost", "/oauth"))
                .isEqualTo("http://localhost/oauth");
    }

}
