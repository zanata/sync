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

import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * So that we can annotation specific methods or classes to bypass security check.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Provider
public class BypassSecurityBinder implements DynamicFeature {
    @Inject
    private RestSecurityInterceptor securityInterceptor;

    @Override
    public void configure(ResourceInfo resourceInfo,
            FeatureContext featureContext) {
        Class<?> clazz = resourceInfo.getResourceClass();
        Method method = resourceInfo.getResourceMethod();
        if (!method.isAnnotationPresent(NoSecurityCheck.class)
                && !clazz.isAnnotationPresent(NoSecurityCheck.class)) {
            featureContext.register(securityInterceptor);
        }
    }
}
