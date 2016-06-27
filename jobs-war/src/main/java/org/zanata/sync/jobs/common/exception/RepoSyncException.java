package org.zanata.sync.jobs.common.exception;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RepoSyncException extends RuntimeException {
    public RepoSyncException() {
        super("failed to clone source repository");
    }

    public RepoSyncException(Exception e) {
        super("failed to clone source repository", e);
    }

    public RepoSyncException(String message, Throwable e) {
        super(message, e);
    }

    public RepoSyncException(String message) {
        super(message);
    }
}
