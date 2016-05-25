package org.zanata.sync.exception;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class UnableLoadPluginException extends Exception {
    public UnableLoadPluginException(String name) {
        super("Unable to load plugin '" + name + "'");
    }
}
