package org.zanata.sync.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.ZanataAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.util.JSONObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

/**
 * This filter should only be accessed by Zanata after a successful OAuth authentication
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "authorizationCodeFilter", urlPatterns = "/*")
public class AuthorizationCodeFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(AuthorizationCodeFilter.class);
    @Inject
    private SecurityTokens securityTokens;

    @Inject
    private JSONObjectMapper objectMapper;

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
            String authCode = request.getParameter(OAuth.OAUTH_CODE);
            if (Strings.isNullOrEmpty(zanataUrl) || Strings.isNullOrEmpty(authCode)) {
                chain.doFilter(request, response);
                return;
            }
            OAuthAuthzResponse oAuthResponse = OAuthAuthzResponse
                    .oauthCodeAuthzResponse(httpServletRequest);
            String code = oAuthResponse.getCode();
            // TODO refactor this and make the method accept zanataUrl and make it request scope
            securityTokens.requestOAuthTokens(zanataUrl, code);
            // TODO just hitting home page with ugly parameters. get URL rewrite and make the url look nicer
//            String originalRequest = httpServletRequest.getParameter("origin");
//            httpServletResponse.sendRedirect(originalRequest);

            String accountAsJson = objectMapper.toJSON(securityTokens.getAccount());
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
