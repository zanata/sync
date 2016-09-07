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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
import org.zanata.sync.EncryptionKey;
import org.zanata.sync.events.ResourceReadyEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ResourceProducer {
    private static final Logger log =
            LoggerFactory.getLogger(ResourceProducer.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Event<ResourceReadyEvent> resourceReadyEvent;
    private byte[] encryptionKey;

    public void onStartUp(
            @Observes @Initialized ServletContext servletContext) {
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
                .configure(
                        JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                        true)
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

    @Produces
    @EncryptionKey
    protected byte[] encryptionKey() {
        if (encryptionKey == null) {
            // defined in standalone.xml
            String secretStore = System.getProperty("secretStore");
            Preconditions.checkState(!Strings.isNullOrEmpty(secretStore),
                    "secretStore must be given as system property");
            Path secretStorePath = Paths.get(secretStore);
            Preconditions.checkState(secretStorePath.isAbsolute(),
                    "%s is not an absolute path", secretStorePath);
            try {
                List<String> lines =
                        Files.readLines(secretStorePath.toFile(), Charsets.UTF_8);
                // for now we only store the master encryption key in it
                Preconditions.checkState(lines.size() == 1,
                        "secretStore should only contain one line");

                this.encryptionKey = lines.get(0).getBytes(Charsets.UTF_8);
                Preconditions.checkState(encryptionKey.length <= 16,
                        "Java only allows 128 bit (16 bytes) for encryption by default. The encryption key is too long");
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
        return encryptionKey;
    }
}
