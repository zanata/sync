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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public enum CronType {
    MANUAL("Only run manually by user", ""),
    WEBHOOK("Only triggered by web hook event", ""),
    ONE_HOUR("Hourly", "0 0 0/1 * * ?"),
    TWO_HOUR("Every two hours", "0 0 0/2 * * ?"),
    SIX_HOUR("Every six hours (6am, 12am, 6pm, 12pm)", "0 0 0/6 * * ?"),
    TWELVE_HOUR("Every twelve hours (12:00am/pm)", "0 0 0,12 * * ?"),
    ONE_DAY("Every day (12:00am)", "0 0 0 * * ?");

    private final String display;
    private final String expression;

    CronType(String display, String expression) {
        this.display = display;
        this.expression = expression;
    }

    public String getDisplay() {
        return display;
    }

    public String getExpression() {
        return expression;
    }

    public static CronType getTypeFromExpression(String expression) {
        for (CronType cronType : values()) {
            if (cronType.getExpression().equals(expression)) {
                return cronType;
            }
        }
        throw new IllegalArgumentException(expression);
    }

    public static CronType getTypeFromDisplay(String display) {
        for (CronType cronType : values()) {
            if (cronType.getDisplay().equals(display)) {
                return cronType;
            }
        }
        throw new IllegalArgumentException(display);
    }

    public static Map<String, CronType> toMapWithDisplayAsKey() {
        ImmutableMap.Builder<String, CronType> builder = ImmutableMap.builder();
        for (CronType cronType : values()) {
            builder.put(cronType.getDisplay(), cronType);
        }
        return builder.build();
    }
}
