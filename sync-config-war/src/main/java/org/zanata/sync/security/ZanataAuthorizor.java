package org.zanata.sync.security;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.security.api.authorization.Secures;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ZanataAuthorizor {

    @Secures
    @ZanataAuthorized
    public boolean doSecuredCheck(InvocationContext invocationContext, BeanManager manager, SecurityTokens securityTokens) throws Exception {
        return securityTokens.hasAccess();
    }
}
