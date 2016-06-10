package org.zanata.sync.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.zanata.sync.controller.SyncWorkForm;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

public interface WorkResource {

    /**
     * @param id - id for work. If empty return list of work
     * @param type - "summary" for {@link org.zanata.sync.model.WorkSummary},
     *             or empty(default) for {@link org.zanata.sync.model.SyncWorkConfig}
     *
     * @return - {@link org.zanata.sync.model.WorkSummary} if type equals 'summary'
     *           {@link org.zanata.sync.model.SyncWorkConfig} if type is empty
     */
    @GET
    public Response
    getWork(@QueryParam(value = "id") @DefaultValue("") String id,
            @QueryParam(value = "type") @DefaultValue("") String type);

    /**
     * Create work
     * @param form - {@link SyncWorkForm}
     *
     * @return - Map<String, String> of fieldKey, and error messages
     */
    @POST
    public Response createWork(SyncWorkForm form);

    /**
     * Update work if {@link SyncWorkForm#id} exists,
     * else trigger {@link #createWork}
     *
     * @param form - {@link SyncWorkForm}
     *
     * @return - Map<String, String> of fieldKey, and error messages
     */
    @PUT
    @Consumes("application/json")
    public Response updateWork(SyncWorkForm form);

    /**
     * Delete work permanently
     *
     * @param id - work id
     *
     * @return - http code
     */
    @DELETE
    @Consumes("application/json")
    public Response deleteWork(
            @QueryParam(value = "id") @DefaultValue("") String id);
}
