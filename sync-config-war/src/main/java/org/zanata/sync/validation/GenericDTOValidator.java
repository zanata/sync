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

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.zanata.sync.App;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class GenericDTOValidator implements BeanValidator<Object> {
    private Validator validator;

    @Inject
    public GenericDTOValidator(@App Validator validator) {
        this.validator = validator;
    }

    @SuppressWarnings("unused")
    public GenericDTOValidator() {
    }

    @Override
    public Map<String, String> validate(Object bean) {
        Map<String, String> errors = new HashMap<>();
        validateThenAddErrors(bean, errors, Default.class);
        return errors;
    }

    @Override
    public Validator getValidator() {
        return validator;
    }
}
