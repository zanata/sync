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

import java.io.File;
import java.nio.file.Paths;
import java.util.Set;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.zanata.sync.App;
import org.zanata.sync.service.impl.PluginsServiceImpl;
import org.zanata.sync.util.AutoCloseableDependentProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import static org.zanata.sync.util.AutoCloseableDependentProvider.forBean;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CommaSeparatedRelativePathsValidator
        implements ConstraintValidator<CommaSeparatedRelativePaths, String> {

    private static final String SEPARATOR = ",";
    private static final String ZANATA_XML = "zanata.xml";

    public void initialize(CommaSeparatedRelativePaths constraint) {
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Iterable<String> values =
                Splitter.on(SEPARATOR).trimResults().omitEmptyStrings()
                        .split(value);
        for (String v : values) {
            if (v.startsWith("/")) {
                context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                        .addConstraintViolation();
                return false;
            }
            File file = new File(v);
            if (file.isAbsolute() || !file.getName().equals(ZANATA_XML)) {
                context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
