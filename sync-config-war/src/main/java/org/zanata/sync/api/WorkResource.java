package org.zanata.sync.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.dto.WorkDetail;
import org.zanata.sync.dto.ZanataAccount;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.model.SyncWorkConfigBuilder;
import org.zanata.sync.dto.WorkSummary;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.service.WorkService;
import org.zanata.sync.validation.SyncWorkFormValidator;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/work")
@Produces("application/json")
@Consumes("application/json")
public class WorkResource {
    private static final Logger log =
            LoggerFactory.getLogger(WorkResource.class);

    @Inject
    private SchedulerService schedulerServiceImpl;

    @Inject
    private WorkService workServiceImpl;

    @Inject
    private SyncWorkFormValidator formValidator;

    @Inject
    private SyncWorkConfigBuilder syncWorkConfigBuilder;

    @Inject
    private PluginsService pluginsService;

    @Inject
    private SecurityTokens securityTokens;

    @GET
    @Path("/{id}")
    public Response
        getWork(@PathParam(value = "id") Long id) {

        try {
            // TODO doing two queries. optimize
            SyncWorkConfig workConfig = schedulerServiceImpl.getWorkById(id);
            List<JobStatus> allJobStatus =
                    schedulerServiceImpl.getAllJobStatus(id);

            return Response.ok(WorkDetail.fromEntity(workConfig, allJobStatus))
                    .build();
        } catch (WorkNotFoundException e) {
            log.error("fail getting job " + id, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/mine")
    public Response getMyWorks() {
        String username = securityTokens.getAccount().getUsername();
        List<WorkSummary> workSummaries = schedulerServiceImpl.getWorkFor(username);
        return Response.ok(workSummaries).build();
    }

    @POST
    public Response createWork(SyncWorkForm form) {
        Map<String, String> errors = formValidator.validateJobForm(form);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).build();
        }
        // TODO pahuang this is a quick hack to make it work with old code
        ZanataAccount zanataAccount = securityTokens.getAccount();
        form.getTransServerPluginConfig().put("username",
                zanataAccount.getUsername());
        form.getTransServerPluginConfig()
                .put("secret", zanataAccount.getApiKey());
        SyncWorkConfig syncWorkConfig = syncWorkConfigBuilder.buildObject(form);
        // TODO pahuang here we should persist the refresh token
        try {
            workServiceImpl.updateOrPersist(syncWorkConfig);
            schedulerServiceImpl.scheduleWork(syncWorkConfig);
        } catch (SchedulerException e) {
            log.error("Error trying to schedule job", e);
            errors.put("error", e.getMessage());
            return Response.serverError().entity(errors).build();
        }
        return Response.created(URI.create("/work/" + syncWorkConfig.getId()))
                .build();
    }

    @PUT
    public Response updateWork(SyncWorkForm form) {
        if(form.getId() == null) {
            return createWork(form);
        }
        Map<String, String> errors = formValidator.validateJobForm(form);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).build();
        }
        SyncWorkConfig syncWorkConfig = syncWorkConfigBuilder.buildObject(form);

        try {
            workServiceImpl.updateOrPersist(syncWorkConfig);
            schedulerServiceImpl.rescheduleWork(syncWorkConfig);
        } catch (SchedulerException e) {
            log.error("Error rescheduling work", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errors).build();
        }
        // TODO create URI
        return Response.created(URI.create("")).entity(errors).build();
    }

    @DELETE
    public Response deleteWork(String id) {
        try {
            workServiceImpl.deleteWork(new Long(id));
        } catch (WorkNotFoundException e) {
            log.error("No work found", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    private Response getAllWork(String type) {
        try {
            if(!type.equals("summary")) {
                return Response.ok(schedulerServiceImpl.getAllWork()).build();
            } else {
                return Response.ok(schedulerServiceImpl.getAllWorkSummary()).build();
            }
        } catch (SchedulerException e) {
            log.error("fail getting all jobs", e);
            return Response.serverError().build();
        }
    }
}
