package org.zanata.sync.jobs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Cookie;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

/**
 * Provides mocking for zanata server interaction
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockZanataServer {
    private static final Logger log =
            LoggerFactory.getLogger(MockZanataServer.class);
    private final MockServerClient mockServerClient;

    public MockZanataServer(MockServerClient mockServerClient) {
        this.mockServerClient = mockServerClient;
    }

    // rest/projects/p/test-project/iterations/i/new/locales
    private void projectLocales() {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/projects/p/test-project/iterations/i/master/locales"),
                exactly(1)
        )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type",
                                                "application/json")
                                )
                                .withBody(
                                        "[{\"localeId\":\"zh\",\"displayName\":\"Chinese\"}]")
                );
    }

    private void versionCheck() {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/version"),
                exactly(1)
        )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type",
                                                "application/vnd.zanata.version+xml")
                                )
                                .withBody(readFile("zanata-output/version.xml"))
                );
    }

    // rest/projects/p/test-project/iterations/i/new/r
    private void getDocumentNames() {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/projects/p/test-project/iterations/i/master/r"),
                exactly(1)
        )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type",
                                                "application/xml")
                                )
                                .withBody(readFile("zanata-output/documentNames.xml"))
                );
    }

    private void pullTranslation() {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/projects/p/test-project/iterations/i/master/r/messages/translations/zh")
                        .withQueryStringParameters(
                                new Parameter("ext", "comment"),
                                new Parameter("skeletons", "false")
                        ),
                exactly(1)
        )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/xml")
                                )
                                .withBody(readFile("zanata-output/pullTranslations.xml"))
                );
    }

    void setUpPullExpectation() {
        projectLocales();
        versionCheck();
        getDocumentNames();
        pullTranslation();
    }

    void template() {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/version")
                        .withQueryStringParameters(
                                new Parameter("returnUrl", "/account")
                        )
                        .withCookies(
                                new Cookie("sessionId",
                                        "2By8LOhBmaW5nZXJwcmludCIlMDAzMW")
                        )
                        .withBody(exact("{username: 'foo', password: 'bar'}")),
                exactly(1)
        )
                .respond(
                        response()
                                .withStatusCode(401)
                                .withHeaders(
                                        new Header("Content-Type",
                                                "application/json; charset=utf-8"),
                                        new Header("Cache-Control",
                                                "public, max-age=86400")
                                )
                                .withBody(
                                        "{ message: 'incorrect username and password combination' }")
                );
    }

    static String readFile(String file) {
        URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(file);
        if (resource == null) {
            log.error("can not find {}", file);
            throw new IllegalStateException("can not find " + file);
        }
        try {
            List<String> lines =
                    Files.readAllLines(Paths.get(resource.getPath()));
            return Joiner.on("\n").skipNulls().join(lines);
        } catch (IOException e) {
            throw new RuntimeException("failed reading file:" + file);
        }
    }
}
