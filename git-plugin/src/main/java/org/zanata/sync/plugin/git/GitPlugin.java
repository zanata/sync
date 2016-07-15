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
package org.zanata.sync.plugin.git;

import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.model.Field;
import org.zanata.sync.common.model.FieldType;
import org.zanata.sync.common.plugin.RepoExecutor;
import org.zanata.sync.common.validator.UrlFieldValidator;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@RepoPlugin
public class GitPlugin extends RepoExecutor {
    private static final String NAME = "git";
    private static final String DESCRIPTION = Messages.getString("plugin.description");

    @Override
    public void initFields() {
        Field urlField = new Field("url", Messages.getString("field.url.label"),
                "https://github.com/zanata/zanata-server.git", null,
                new UrlFieldValidator(), false, FieldType.TEXT);
        Field branchField =
                new Field("branch", Messages.getString("field.branch.label"),
                        "master", Messages.getString("field.branch.tooltip"),
                        false, FieldType.TEXT);
        Field usernameField =
                new Field("username",
                        Messages.getString("field.username.label"),
                        "", Messages.getString("field.username.tooltip"),
                        false, FieldType.TEXT);
        Field apiKeyField =
                new Field("secret", Messages.getString("field.secret.label"),
                        "",
                        Messages.getString("field.secret.tooltip"),
                        true, FieldType.TEXT);

        fields.put(urlField.getKey(), urlField);
        fields.put(branchField.getKey(), branchField);
        fields.put(usernameField.getKey(), usernameField);
        fields.put(apiKeyField.getKey(), apiKeyField);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

}
