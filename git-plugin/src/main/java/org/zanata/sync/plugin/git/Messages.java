package org.zanata.sync.plugin.git;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public final class Messages {
    private static final String BASENAME = "messages";
    private static ResourceBundle resourceBundle =
        ResourceBundle.getBundle(BASENAME);

    private static MessageFormat formatter =
        new MessageFormat("", resourceBundle.getLocale());

    public static String getString(String key, Object... args) {
        if(key == null || key.length() <= 0) {
            return "";
        }
        if(args == null || args.length <= 0) {
            return resourceBundle.getString(key);
        }
        formatter.applyPattern(resourceBundle.getString(key));
        return formatter.format(args);
    }

    public static String getString(Locale locale, String key, Object... args) {
        if(locale != null) {
            resourceBundle = ResourceBundle.getBundle(BASENAME, locale);
            formatter.setLocale(locale);
        }
        return getString(key, args);
    }
}
