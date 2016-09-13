package org.zanata.sync.jobs;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Cookie;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;

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
                                .withBody(
                                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:versionInfo xmlns:ns2=\"http://zanata.org/namespace/api/\"><versionNo>4.0.0-SNAPSHOT</versionNo><buildTimeStamp>unknown</buildTimeStamp><ns2:scmDescribe>unknown</ns2:scmDescribe></ns2:versionInfo>")
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
                                                "application/json")
                                )
                                .withBody(
                                        "[{\"name\":\"messages\",\"contentType\":\"text/plain\",\"lang\":\"en-US\",\"type\":\"FILE\",\"revision\":1}]")
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
                                        new Header("Content-Type", "application/json")
                                )
                                .withBody("{\"extensions\":[],\"textFlowTargets\":[{\"resId\":\"greeting\",\"state\":\"Approved\",\"translator\":{\"email\":\"user@example.com\",\"name\":\"User\"},\"content\":\"hello, world\",\"extensions\":[],\"revision\":1,\"textFlowRevision\":1}]}")
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
}
