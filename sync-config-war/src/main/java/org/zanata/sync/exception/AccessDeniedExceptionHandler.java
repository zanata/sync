package org.zanata.sync.exception;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ExceptionHandler
public class AccessDeniedExceptionHandler {
    @Inject
    @DeltaSpike
    private HttpServletRequest request;

    public void handleException(
            @Handles ExceptionEvent<AccessDeniedException> event) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            context.getExternalContext().redirect(request.getContextPath() + "/sign_in.jsf?original=" + request.getRequestURI());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        context.responseComplete();
        event.handled();
    }
}
