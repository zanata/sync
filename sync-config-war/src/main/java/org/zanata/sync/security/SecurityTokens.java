package org.zanata.sync.security;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.ZanataAccount;
import org.zanata.sync.util.ZanataRestClient;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@SessionScoped
public class SecurityTokens implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(SecurityTokens.class);
    @Inject
    private ZanataRestClient zanataRestClient;

    private String accessToken;
    private String refreshToken;
    private String zanataServerUrl;
    private String zanataUsername;
    private String zanataApiKey;
    private ZanataAccount account;

    boolean hasAccess() {
        return refreshToken != null;
    }

    public OAuthToken requestOAuthTokens(String authorizationCode)
            throws OAuthProblemException {
        if (zanataServerUrl == null) {
            throw new IllegalStateException("You are not authorized to one Zanata server");
        }
        OAuthClient oAuthClient = null;
        try {
            // TODO pahuang we only need to get access token and refresh token once (then we should persist the refresh token)
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(zanataServerUrl + "/rest/oauth/token")
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId("zanata_sync")
                    .setClientSecret("we_do_not_have_a_secret")
                    .setRedirectURI("http://www.not.in.use.but.required")
                    .setCode(authorizationCode)
                    .buildBodyMessage();

            //create OAuth client that uses custom http client under the hood
            oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthJSONAccessTokenResponse accessTokenResponse =
                    oAuthClient.accessToken(request, "POST", StatusCodeAwareOAuthJSONAccessTokenResponse.class);

            OAuthToken oAuthToken = accessTokenResponse.getOAuthToken();
            accessToken = oAuthToken.getAccessToken();
            refreshToken = oAuthToken.getRefreshToken();

            // this should change once we have Zanata all converted to use OAuth
            account = zanataRestClient.getAuthorizedAccount();
            log.debug("========= my account: {}", account);
            // for the time being, we only allow Zanata admin to create jobs
            if (!account.getRoles().contains("admin")) {
                throw OAuthProblemException.error("Only Zanata admin can create sync job");
            }

            zanataUsername = account.getUsername();
            zanataApiKey = account.getApiKey();

            return oAuthToken;
        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        } finally {
            if (oAuthClient != null) {
                oAuthClient.shutdown();
            }
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getZanataServerUrl() {
        return zanataServerUrl;
    }

    public void setZanataServerUrl(String zanataServerUrl) {
        this.zanataServerUrl = zanataServerUrl;
    }

    public String getZanataUsername() {
        return zanataUsername;
    }

    public String getZanataApiKey() {
        return zanataApiKey;
    }

    public ZanataAccount getAccount() {
        return account;
    }

    public static class StatusCodeAwareOAuthJSONAccessTokenResponse extends OAuthJSONAccessTokenResponse {

        @Override
        protected void init(String body, String contentType, int responseCode)
                throws OAuthProblemException {
            if (responseCode < 300) {
                super.init(body, contentType, responseCode);
            } else {
                throw OAuthProblemException.error("invalid status code:" + responseCode);
            }
        }
    }
}
