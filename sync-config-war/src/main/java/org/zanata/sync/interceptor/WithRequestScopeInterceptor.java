package org.zanata.sync.interceptor;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.util.ContextUtils;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Interceptor
@WithRequestScope
public class WithRequestScopeInterceptor {
    @Inject
    private ContextControl contextControl;

    @AroundInvoke
    public Object manageRequestScope(InvocationContext ctx) throws Exception {
        boolean started = false;
        try {
             if (!ContextUtils.isContextActive(RequestScoped.class)) {
                 started = true;
                 contextControl.startContext(RequestScoped.class);
             }
             return ctx.proceed();
         } finally {
             if (started) {
                 contextControl.stopContext(RequestScoped.class);
             }
         }
    }
}
