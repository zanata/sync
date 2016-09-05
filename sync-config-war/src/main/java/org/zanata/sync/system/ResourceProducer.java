/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.sync.system;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.client.Client;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.App;
import org.zanata.sync.events.ResourceReadyEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ResourceProducer {
    private static final Logger log = LoggerFactory.getLogger(ResourceProducer.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Event<ResourceReadyEvent> resourceReadyEvent;

    public void onStartUp(@Observes @Initialized ServletContext servletContext) {
        resourceReadyEvent.fire(new ResourceReadyEvent());

    }

//    @Produces
//    @RequestScoped
//    @Default
//    protected EntityManager entityManager() {
//        return entityManager;
//    }
//
//    protected void onDispose(@Disposes EntityManager entityManager) {
//        if (entityManager.isOpen()) {
//            Session session = entityManager.unwrap(Session.class);
//            // sometimes EntityManager.isOpen() returns true when the Session
//            // is actually closed, so we ask the Session too
//            if (session.isOpen()) {
//                log.debug("___________ closing EntityManager: {}", entityManager);
//                entityManager.close();
//            } else {
//                log.debug("Session is not open");
//            }
//        } else {
//            log.debug("EntityManager is not open");
//        }
//    }

    @Produces
    @App
    protected ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper
                .configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        return objectMapper;
    }

    @Produces
    protected Client getRestClient() {
        return ResteasyClientBuilder.newBuilder().build();
    }

    @Produces
    @App
    @ApplicationScoped
    public Validator getValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
