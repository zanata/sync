/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.api;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.JobRunStatus;
import org.zanata.sync.dto.RunningJobKey;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.util.JSONObjectMapper;

/**
 * WebSocket endpoint doesn't seem to support RequestScoped and SessionScoped
 * injection.
 * <p>
 * Similar to JAX-RS endpoints, websocket endpoint is one instance per
 * connection by default. It's lifecycle differ from the one in CDI.
 * <p>
 * See below for reference:
 * <pre>
 * <ul>
 *     <li> see https://issues.jboss.org/browse/CDI-370</li>
 *     <li> see https://abhirockzz.wordpress.com/2015/02/10/integrating-cdi-and-websockets/</li>
 *     <li> see https://netbeans.org/kb/docs/javaee/maven-websocketapi.html</li>
 * </ul>
 * </pre>
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ServerEndpoint("/websocket/jobStatus")
public class JobStatusWebSocketsEndpoint {
    private static final Logger log =
            LoggerFactory.getLogger(JobStatusWebSocketsEndpoint.class);
    @Inject
    private SchedulerService schedulerService;

    @Inject
    private JSONObjectMapper objectMapper;

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket opened: {}", session.getId());
        schedulerService.addWebSocketSession(session);
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        log.info("Closing a WebSocket {} due to ", session.getId(),
                reason.getReasonPhrase());
        schedulerService.removeWebSocketSession(session);
    }
}
