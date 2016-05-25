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
import javax.validation.ValidatorFactory;

import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.plugin.Plugin;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.plugin.TranslationServerExecutor;
import org.zanata.sync.common.plugin.Validator;
import org.zanata.sync.controller.SyncWorkForm;
import org.zanata.sync.service.PluginsService;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class SyncWorkFormValidator {
    @Inject
    PluginsService pluginsService;

    public Map<String, String> validateJobForm(SyncWorkForm form) {

        // validate the input
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Set<ConstraintViolation<SyncWorkForm>> violations = factory.getValidator()
                .validate(form);

        Map<String, String> errors = new HashMap<>();
        violations.stream().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        errors.putAll(validateRepoFields(form.getSrcRepoPluginConfig(),
                form.getSrcRepoPluginName()));
        errors.putAll(validateTransFields(form.getTransServerPluginConfig()
        ));

        return errors;
    }

    private Map<String, String> validateRepoFields(
            Map<String, String> config, String executorClass) {
        RepoExecutor executor = pluginsService.getNewSourceRepoPlugin(
                executorClass);
        if(executor == null) {
            return new HashMap<>();
        }
        return validateFields(config, executor, SyncWorkForm.repoSettingsPrefix);
    }

    private Map<String, String> validateTransFields(
            Map<String, String> config) {
        TranslationServerExecutor executor = new org.zanata.sync.plugin.zanata.Plugin(null);
        if(executor == null) {
            return new HashMap<>();
        }
        return validateFields(config, executor, SyncWorkForm.transSettingsPrefix);
    }

    private Map<String, String> validateFields(Map<String, String> config,
            Plugin executor, String prefix) {
        Map<String, String> errors = Maps.newHashMap();

        for (Map.Entry<String, Field> fieldEntry : executor.getFields()
                .entrySet()) {
            String key = fieldEntry.getKey();

            Field field = fieldEntry.getValue();
            String configValue = config.get(key);
            if (field.getValidator() != null) {
                Validator validator = field.getValidator();
                String message = validator.validate(configValue);
                if (!Strings.isNullOrEmpty(message)) {
                    errors.put(prefix + key, message);
                }
            }
        }

        return errors;
    }
}
