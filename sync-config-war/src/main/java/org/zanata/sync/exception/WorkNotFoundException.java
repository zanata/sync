package org.zanata.sync.exception;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class WorkNotFoundException extends Exception {
    public WorkNotFoundException(String key) {
        super("Work not found:" + key);
    }
}
