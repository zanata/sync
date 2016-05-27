package org.zanata.sync.controller;

import java.io.IOException;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.security.SecurityTokens;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Named("zanataSignIn")
public class ZanataSignIn {
    private static final Logger log =
            LoggerFactory.getLogger(ZanataSignIn.class);
    @Inject
    @DeltaSpike
    private HttpServletRequest request;

    @Getter
    @Setter
    private String url;

    private String errorMessage;

    @Setter
    @Getter
    private String originalRequest;

    @Inject
    private SecurityTokens securityTokens;

    @Getter
    private List<String> productionServerUrls = ImmutableList.<String>builder()
            .add("http://localhost:8180/zanata")
            .add("http://localhost:8080/zanata")
            .add("https://translate.zanata.org")
            .add("https://translate.jboss.org")
            .add("https://fedora.zanata.org")
            .build();

    @Setter
    @Getter
    private String selectedUrl;

    public boolean hasError() {
        return errorMessage != null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void signIn() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String zanataAuthUrl = generateOAuthURL();

            if (hasError()) {
                return;
            }

            securityTokens.setZanataServerUrl(selectedUrl);

            // we prepend /auth/ to redirect url so that it can hit the web filter
            // see AuthorizationCodeFilter
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(zanataAuthUrl)
                    .setClientId("zanata_sync")
                    .setRedirectURI(appRoot() + "/auth/?origin=" + Strings.nullToEmpty(originalRequest))
                    .buildQueryMessage();

            log.info("=========== redirecting to {}", request.getLocationUri());
            context.getExternalContext().redirect(request.getLocationUri());
            context.responseComplete();

        } catch (IOException | OAuthSystemException e) {
            errorMessage = e.getMessage();
        }
    }

    private String generateOAuthURL() {
        String zanataUrl = Strings.isNullOrEmpty(selectedUrl) ? url : selectedUrl;
        if (Strings.isNullOrEmpty(zanataUrl)) {
            errorMessage = "You must select one production server or enter your own Zanata URL";
            return null;
        }
        String authorizeUri = "authorize/";
        if (zanataUrl.endsWith("/")) {
            return zanataUrl + authorizeUri;
        } else {
            return zanataUrl + "/" + authorizeUri;
        }
    }

    private String appRoot() {
        String contextPath = request.getContextPath();
        String scheme = request.getScheme();
        int serverPort = request.getServerPort();
        String serverName = request.getServerName();
        String port = serverPort == 80 ? "" : ":" + serverPort;
        return scheme + "://" + serverName + port + contextPath;
    }

}
