package org.zanata.sync.jobs;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.api.JobDetailEntry;
import org.zanata.sync.jobs.plugin.git.service.impl.RemoteGitRepoRule;
import com.google.common.collect.Maps;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SyncToRepoITCase {
    private static final Logger log =
            LoggerFactory.getLogger(SyncToRepoITCase.class);
    private final String zanataUsername = "pahuang";
    @Rule
    public RemoteGitRepoRule remoteGitRepoRule = new RemoteGitRepoRule();

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.MINUTES);

    // this will be set by the MockServerRule
    private MockServerClient mockServerClient;
    private String zanataUrl;
    private MockZanataServer mockZanataServer;

    @Before
    public void setUp() {
        zanataUrl = "http://localhost:" + mockServerRule.getPort();
        String content = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
                .append("<config xmlns=\"http://zanata.org/namespace/config/\">\n")
                .append("    <url>").append(zanataUrl).append("</url>\n")
                .append("    <project>test-project</project>\n")
                .append("    <project-version>master</project-version>\n")
                .append("    <project-type>properties</project-type>\n")
                .append("</config>").toString();
        remoteGitRepoRule.addFile("zanata.xml", content);
        remoteGitRepoRule.commitFiles("added zanata.xml");
        mockZanataServer = new MockZanataServer(mockServerClient);
    }

    @After
    public void cleanUp() {
        if (mockServerClient.isRunning()) {
            log.info("------- stopping mock server -------");
            mockServerClient.stop(true);
        }
    }


    @Test
    public void canSyncToRepo() {
        mockZanataServer.setUpPullExpectation();
        Client client = ClientBuilder.newClient();

        Map<String, String> payload = createPayload();
        Response response =
                client.target("http://localhost:8280/jobs/api/job/2repo/start/id1")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.json(payload));

        assertThat(response.getStatus())
                .isEqualTo(Response.Status.CREATED.getStatusCode());

        log.debug("====remote: {}", remoteGitRepoRule.getRemoteUrl());
        Awaitility.await()
                .pollInterval(1, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.MINUTES).until(() -> {
            List<String> commitMessages =
                    remoteGitRepoRule.getCommitMessages("master");
            log.debug(".... checking result ....{}", commitMessages);
            assertThat(commitMessages).isNotEmpty();
            assertThat(commitMessages.get(0))
                    .startsWith(
                            "Zanata Sync job triggered by " + zanataUsername);
        });
    }



    /**
     * <li>srcRepoUrl</li>
     *      <li>srcRepoUsername</li>
     *      <li>srcRepoSecret</li>
     *      <li>srcRepoBranch</li>
     *      <li>syncToZanataOption=source|trans|both</li>
     *      <li>srcRepoType=git|anything else we may support in the future</li>
     *      <li>zanataUrl</li>
     *      <li>zanataUsername</li>
     *      <li>zanataSecret</li>
     * @return
     */
    private Map<String, String> createPayload() {
        Map<String, String> map = Maps.newHashMap();
        map.put(JobDetailEntry.srcRepoUrl.name(), remoteGitRepoRule.getRemoteUrl());
        map.put(JobDetailEntry.srcRepoType.name(), "git");
        map.put(JobDetailEntry.zanataUrl.name(), zanataUrl);
        map.put(JobDetailEntry.zanataUsername.name(), zanataUsername);
        map.put(JobDetailEntry.zanataSecret.name(), "secret");
        return map;
    }
}
