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
package org.zanata.sync.validation;

import java.util.Set;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.zanata.sync.App;
import org.zanata.sync.service.impl.PluginsServiceImpl;
import org.zanata.sync.util.AutoCloseableDependentProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import static org.zanata.sync.util.AutoCloseableDependentProvider.forBean;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SupportedRepoValidator
        implements ConstraintValidator<SupportedRepo, String> {

    @VisibleForTesting
    protected static Set<String> supportedRepoTypes;

    public void initialize(SupportedRepo constraint) {
        if (supportedRepoTypes == null) {
            try (AutoCloseableDependentProvider<PluginsServiceImpl.SupportedRepoTypes> provider =
                    forBean(PluginsServiceImpl.SupportedRepoTypes.class,
                            new AnnotationLiteral<App>() {
                            })) {
                supportedRepoTypes = provider.getBean().getTypes();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        boolean isValid = supportedRepoTypes.contains(value);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "{constraints.unsupported.repo.type}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
