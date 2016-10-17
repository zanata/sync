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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.client.Client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

/**
 * Single place to produce anything system wide. Althouth it's
 * applicationScoped, it's okay to have multiple copies in different JVM. In
 * order words, it's okay to scale this app as it won't share or store any state
 * for the app. Making it applicationScoped is just so we can save a tiny bit of
 * memory to only have one instance per app.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ResourceProducer {
    private static final Logger log =
            LoggerFactory.getLogger(ResourceProducer.class);

    public static final String JAXRS_CLIENT_CONN_POOL_SIZE =
            "jaxrs.connection.pool.size";
    public static final String SYSTEM_NOTIFICATION_EMAIL =
            "system.notification.email";
    public static final String REPO_CACHE_DIR = "repo.cache.dir";
    public static final String TRY_NATIVE_GIT ="try_native_git";

    private boolean hasNativeGit = isGitExecutableOnPath();

    private static boolean isGitExecutableOnPath() {
        String tryNativeGit = System.getenv(TRY_NATIVE_GIT);
        if (Boolean.parseBoolean(tryNativeGit)) {
            Pattern pattern = Pattern.compile(Pattern.quote(File.pathSeparator));
            return pattern.splitAsStream(System.getenv("PATH"))
                    .map(Paths::get)
                    .anyMatch(path -> Files.exists(path.resolve("git")));
        }
        return false;
    }

    @Produces
    @JAXRSClientConnectionPoolSize
    protected int jaxrsClientConnectionPoolSize() {
        return Integer
                .valueOf(System.getProperty(JAXRS_CLIENT_CONN_POOL_SIZE, "20"));
    }

    @Produces
    @RestClient
    protected Client client(@JAXRSClientConnectionPoolSize int poolSize) {
        // This will create a threadsafe JAX-RS client using pooled connections.
        // Per default this implementation will create no more than than 2
        // concurrent connections per given route and no more 20 connections in
        // total. (see javadoc of PoolingHttpClientConnectionManager)
        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager();

        CloseableHttpClient closeableHttpClient =
                HttpClientBuilder.create().setConnectionManager(cm).build();
        ApacheHttpClient4Engine engine =
                new ApacheHttpClient4Engine(closeableHttpClient);
        return new ResteasyClientBuilder().httpEngine(engine).build();
    }

    @Produces
    @HasNativeGit
    protected boolean hasNativeGit() {
        return hasNativeGit;
    }

    @Produces
    @RepoCacheDir
    protected Path cacheDir() {
        Path defaultPath =
                Paths.get(System.getProperty("java.io.tmpdir"), "repo-cache");
        String cacheDir = System.getProperty(REPO_CACHE_DIR);
        if (Strings.isNullOrEmpty(cacheDir)) {
            return defaultPath;
        }
        return Paths.get(cacheDir);
    }

    @Produces
    protected Address systemNotificationEmailAddress() {
        String email = System.getProperty(SYSTEM_NOTIFICATION_EMAIL);
        if (!Strings.isNullOrEmpty(email)) {
            try {
                return new InternetAddress(email, "Zanata Sync System");
            } catch (UnsupportedEncodingException e) {
                throw Throwables.propagate(e);
            }
        }
        throw new IllegalStateException(
                SYSTEM_NOTIFICATION_EMAIL + " system property is not set");
    }
}
