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
package org.zanata.sync.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.zanata.sync.dto.RepoSyncGroup;
import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.dto.ZanataSyncGroup;
import org.zanata.sync.service.PluginsService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class SyncWorkFormValidator {
    @Inject
    PluginsService pluginsService;
    private static Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    public Map<String, String> validateForm(SyncWorkForm form) {
        Map<String, String> errors = new HashMap<>();
        validateThenAddErrors(form, errors, Default.class);

        boolean syncToRepoEnabled = form.isSyncToRepoEnabled();
        if (syncToRepoEnabled) {
            validateThenAddErrors(form, errors, RepoSyncGroup.class);
        }

        boolean syncToZanataEnabled = form.isSyncToZanataEnabled();
        if (syncToZanataEnabled) {
            validateThenAddErrors(form, errors, ZanataSyncGroup.class);
        }

        boolean atLeastOneEnabled = syncToRepoEnabled || syncToZanataEnabled;
        if (!atLeastOneEnabled) {
            errors.put("enabledJobs",
                    "At least one type of job should be enabled");
        }

        return errors;
    }

    private static void validateThenAddErrors(SyncWorkForm form,
            Map<String, String> errors, Class<?>... validationGroups) {
        Set<ConstraintViolation<SyncWorkForm>> violations =
                validator.validate(form, validationGroups);
        violations.forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(),
                    violation.getMessage());
        });
    }

}
