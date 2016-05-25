package org.zanata.sync.plugin.zanata.exception;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataSyncException extends RuntimeException {

    public ZanataSyncException() {
        super("failed to sync to Zanata server");
    }

    public ZanataSyncException(Exception e) {
        super("failed to sync to Zanata server", e);
    }

    public ZanataSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
