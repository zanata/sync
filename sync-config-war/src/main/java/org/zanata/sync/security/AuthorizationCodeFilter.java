package org.zanata.sync.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.dto.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * This filter should only be accessed by Zanata after a successful OAuth authentication
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "authorizationCodeFilter", urlPatterns = "/auth/*")
public class AuthorizationCodeFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(AuthorizationCodeFilter.class);
    @Inject
    private SecurityTokens securityTokens;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        try {
            String zanataUrl = request.getParameter("z");
            OAuthAuthzResponse oAuthResponse = OAuthAuthzResponse
                    .oauthCodeAuthzResponse(httpServletRequest);
            String code = oAuthResponse.getCode();
            // TODO refactor this and make the method accept zanataUrl and make it request scope
            securityTokens.setZanataServerUrl(zanataUrl);
            securityTokens.requestOAuthTokens(code);
            log.debug("authorization code: {}", code);
            log.debug("access token: {}", securityTokens.getAccessToken());
            log.debug("refresh token: {}", securityTokens.getRefreshToken());
            // TODO pahuang just redirect back to home page with user information (maybe we should forward to a page or get URL rewrite)
//            String originalRequest = httpServletRequest.getParameter("origin");
//            httpServletResponse.sendRedirect(originalRequest);

            String accountAsJson = new ObjectMapper().writerFor(Account.class)
                    .writeValueAsString(securityTokens.getAccount());
            request.setAttribute("user", accountAsJson);
            request.setAttribute("zanata", zanataUrl);
//            request.setAttribute("accessToken", "{\"accessToken\": \"" + securityTokens.getAccessToken() +
//                    "\"}");
            chain.doFilter(request, response);
        } catch (OAuthProblemException e) {
            log.warn("=== problem with OAuth", e);

            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.setContentType("text/html");
            PrintWriter writer = httpServletResponse.getWriter();
            writer.print("<h2>error:</h2>");
            writer.println(e.getError() + ":" +  e.getDescription());
            writer.flush();
            writer.close();
        }
    }

    @Override
    public void destroy() {
    }
}
