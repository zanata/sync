/*
 * Copyright 2010-2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.sync.i18n;

import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import org.zanata.sync.util.EmptyEnumeration;

/**
 * Utility component to help with programmatic access to the message resource
 * bundle.
 *
 * Unlike the {@link org.jboss.seam.international.Messages} component, this
 * component formats messages using positional arguments like {0} and
 * {1}, not by interpolating EL expressions.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

@Alternative
public class Messages extends AbstractMap<String, String> {

    /**
     * Gets the 'messages' ResourceBundle for the specified locale.
     */
    private static ResourceBundle getResourceBundle(Locale locale) {
        // Generic ResourceBundle without built-in interpolation:
        try {
            return ResourceBundle.getBundle("messages", locale);
        } catch (MissingResourceException e) {
            return new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key) {
                    return key;
                }

                @Override
                public Enumeration<String> getKeys() {
                    return EmptyEnumeration.instance();
                }
            };
        }
    }

    Locale locale;
    // NB: getBundle will load the bundle whenever it is null
    transient ResourceBundle resourceBundle;

    protected Messages() {
    }

    /**
     * Create an instance for the specified locale.
     */
    public Messages(Locale locale) {
        this.locale = locale;
        this.resourceBundle = null;
    }

    private ResourceBundle getBundle() {
        if (resourceBundle == null) {
            resourceBundle = getResourceBundle(locale);
        }
        return resourceBundle;
    }

    // the default toString includes the entire list of properties,
    // which makes a mess of the log file
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Gets a resource string, without any message formatting.  (So an
     * apostrophe just represents an apostrophe (single quote).)
     * @param key ResourceBundle key
     * @return ResourceBundle string
     */
    @Override
    public String get(Object key) {
        if (key instanceof String) {
            String resourceKey = (String) key;
            try {
                return getBundle().getString(resourceKey);
            } catch (MissingResourceException mre) {
                return resourceKey;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets a resource string, and formats it using MessageFormat and the
     * positional parameters.  Due to the use of {@link MessageFormat}
     * any literal apostrophes (single quotes) will need to be doubled,
     * otherwise they will be interpreted as quoting format patterns.
     * @param key ResourceBundle key to look up the format string
     * @param args arguments for interpolation by MessageFormat
     * @return formatted string
     * @see MessageFormat
     */
    public String formatWithAnyArgs(String key, Object... args) {
        String template = get(key);
        return MessageFormat.format(template, args);
    }

    // JSF can't handle varargs, hence the need for these overloaded methods:
    public String format(String key, Object arg1) {
        return formatWithAnyArgs(key, arg1);
    }

    public String format(String key, Object arg1, Object arg2) {
        return formatWithAnyArgs(key, arg1, arg2);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3) {
        return formatWithAnyArgs(key, arg1, arg2, arg3);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
        return formatWithAnyArgs(key, arg1, arg2, arg3, arg4);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return formatWithAnyArgs(key,
                arg1, arg2, arg3, arg4, arg5);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        return formatWithAnyArgs(key,
                arg1, arg2, arg3, arg4, arg5, arg6);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> entrySet =
                new HashSet<Entry<String, String>>();

        for (final String key : getBundle().keySet()) {
            entrySet.add(new Entry<String, String>() {

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public String getValue() {
                    return get(key);
                }

                @Override
                public String setValue(String val) {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return entrySet;
    }

    /**
     * Uses the locale of the current request, if any, otherwise server's
     * default locale. (observes LocaleSelectedEvent)
     */
    @Default
    @RequestScoped
    @Named("msg")
    public static class AutoLocaleMessages extends Messages {
        public AutoLocaleMessages() {
            super(getLocale());
        }

        /**
         * Gets the locale of the current request. NB: May not work during
         * application startup: use @DefaultLocale Messages.
         */
        private static Locale getLocale() {
            return Locale.getDefault();
        }
    }

}
