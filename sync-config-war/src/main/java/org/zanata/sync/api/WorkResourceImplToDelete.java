package org.zanata.sync.api;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;

import org.zanata.sync.controller.SyncWorkForm;

/**
 * TODO delete
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class WorkResourceImplToDelete implements WorkResource {
    @Override
    public Response getWork(@DefaultValue("") String id,
            @DefaultValue("") String type) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return null;
    }

    @Override
    public Response createWork(SyncWorkForm form) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return null;
    }

    @Override
    public Response updateWork(SyncWorkForm form) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return null;
    }

    @Override
    public Response deleteWork(@DefaultValue("") String id) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return null;
    }
}
