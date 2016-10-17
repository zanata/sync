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
package org.zanata.sync.jobs.system;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import javax.mail.Address;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebListener
public class InitListener implements ServletContextListener {
    private static final Logger log =
            LoggerFactory.getLogger(InitListener.class);

    @Inject
    @JAXRSClientConnectionPoolSize
    private int poolSize;

    @Inject
    @HasNativeGit
    private boolean hasNativeGit;

    @Inject
    @RepoCacheDir
    private Path repoCacheRootDir;

    @Inject
    private Address systemNotificationEmail;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // we should check all the system state here to make sure they are all set
        log.info("==== system config ====");
        log.info("==== JAXRS client connection pool size ({}): {}",
                ResourceProducer.JAXRS_CLIENT_CONN_POOL_SIZE, poolSize);
        log.info("==== has native git: {}", hasNativeGit);
        log.info("==== repo cache dir ({}): {}", ResourceProducer.REPO_CACHE_DIR,
                repoCacheRootDir);
        log.info("==== system notification email ({}): {}",
                ResourceProducer.SYSTEM_NOTIFICATION_EMAIL,
                systemNotificationEmail);

        writeOutCustomKeyStore();

        log.info("==== system config ====");
    }

    /**
     * If we have packaged a custom key store, we will write it out. This is due
     * to openshift not supporting replicate file system for scalable app.
     */
    private static void writeOutCustomKeyStore() {
        URL cacerts = Thread.currentThread().getContextClassLoader()
                .getResource("cacerts");
        if (cacerts == null) {
            log.info("==== no packaged custom key store");
            return;
        }

        log.info("==== packaged custom key store (sha1): {}", sha1sum(cacerts));

        try (InputStream cacertsIS = cacerts.openStream()) {
            String trustStorePath =
                    System.getProperty("javax.net.ssl.trustStore");
            if (Strings.isNullOrEmpty(trustStorePath)) {
                log.warn(
                        "[javax.net.ssl.trustStore] is not set but found packaged key store! Ignored.");
                return;
            }
            Files.copy(cacertsIS, Paths.get(trustStorePath),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static String sha1sum(URL fileURL) {
        int bufferSize = 8192;
        try (BufferedInputStream is = new BufferedInputStream(
                fileURL.openStream(), bufferSize)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            int n = 0;
            byte[] buffer = new byte[bufferSize];
            while (n != -1) {
                n = is.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return new HexBinaryAdapter().marshal(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw Throwables.propagate(e);
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
