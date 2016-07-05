package org.zanata.sync.service;

import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.SyncWorkConfig;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface WorkService {
    void deleteWork(Long id);

    void updateOrPersist(SyncWorkConfig syncWorkConfig);
}
