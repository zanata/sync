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
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.zanata.sync.security.SecurityTokens;
import com.google.common.collect.Sets;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ServerEndpoint("/websocket/jobStatus")
public class JobStatusWebSocketsEndpoint {
//    @Inject
//    private SecurityTokens securityTokens;

    @OnMessage
    public String sayHello(String name) {
        System.out.println("Say hello to '" + name + "'");
        return ("Hello" + name);
    }

    @OnOpen
    public void onOpen(Session session) {
//        if (!securityTokens.hasAccess()) {
//            throw new AccessDeniedException(
//                    Sets.newHashSet(() -> "You need to sign in first"));
//        }
        System.out.println("WebSocket opened: " + session.getId());
        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
        asyncRemote.sendText("hello from the server");
    }

    @OnClose
    public void onClose(CloseReason reason) {
        System.out.println("Closing a WebSocket due to "
                + reason.getReasonPhrase());
    }
}
