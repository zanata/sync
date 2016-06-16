package org.zanata.sync.api;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/job")
@Produces("application/json")
public interface JobResource {

    /**
     * Get job status
     *
     * @param id - work identifier
     * @param type - {@link JobType}
     *
     * @return - {@link org.zanata.sync.model.JobStatus}
     */
    @Path("/status")
    @GET
    public Response getJobStatus(
            @QueryParam(value = "id") @DefaultValue("") String id,
            @QueryParam(value = "type") @DefaultValue("")
                    JobType type);

    /**
     * Cancel job if it is running
     *
     * @param id - work identifier
     * @param type - {@link JobType}
     *
     * @return - http code
     */
    @Path("/cancel")
    @POST
    public Response cancelRunningJob(
            @QueryParam(value = "id") @DefaultValue("") String id,
            @QueryParam(value = "type") @DefaultValue("") JobType type);

    /**
     * trigger job
     *
     * @param id - work identifier
     * @param type - {@link JobType}
     *
     * @return - http code
     */
    @Path("/start")
    @POST
    public Response triggerJob(
            @QueryParam(value = "id") @DefaultValue("") String id,
            @QueryParam(value = "type") @DefaultValue("") JobType type);

    /**
     * Get list of job with matching filter.
     *
     * @param id - work identifier, empty for all job
     * @param type - required if id is present. {@link JobType}
     * @param status - {@link JobStatusType},  empty for all status
     *
     * @return - List of {@link org.zanata.sync.model.JobSummary}
     *  or List of 1 if id and type is present.
     *
     */
    @GET
    Response getJob(
            @QueryParam(value = "id") @DefaultValue("") String id,
            @QueryParam(value = "type") @DefaultValue("") JobType type,
            @QueryParam(value = "status") @DefaultValue("")
                    JobStatusType status);
}
