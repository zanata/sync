package org.zanata.sync.exception;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class JobNotFoundException extends Exception {
    public JobNotFoundException(String key) {
        super("Job not found:" + key);
    }
}
