package org.zanata.sync.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class PersistModel implements Serializable {

    protected abstract void setCreatedDate(Date date);

    public void onPersist() {
        setCreatedDate(new Date());
    }
}
