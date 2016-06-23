package org.zanata.sync.exception;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;

/**
 * TODO may not need this any more
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ExceptionHandler
public class AccessDeniedExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(AccessDeniedExceptionHandler.class);
    @Inject
    @DeltaSpike
    private HttpServletRequest request;

    @Inject
    @DeltaSpike
    private HttpServletResponse response;

    public void handleException(
            @Handles ExceptionEvent<AccessDeniedException> event) {
        log.warn("unauthorized request: {}?{}" ,request.getRequestURI(), request.getQueryString());
        try {

            response.sendRedirect(request.getContextPath());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        event.handled();
    }
}
