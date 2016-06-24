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
package org.zanata.sync.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.zanata.sync.App;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class JSONObjectMapper {

    private final ObjectMapper objectMapper;

    @Inject
    public JSONObjectMapper(@App ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromJSON(Class<? super T> type, String jsonString) {
        try {
            return objectMapper.readerFor(type).readValue(jsonString);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Marshall an object into JSON string. If the object is null, return empty
     * string.
     *
     * @param value
     *         the object
     * @return the JSON string
     */
    public String toJSON(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        try {
            return objectMapper.writer().writeValueAsString(value);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
