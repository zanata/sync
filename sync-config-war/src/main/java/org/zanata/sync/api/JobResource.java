/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.api;

import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.JobRunStatus;
import org.zanata.sync.dto.Payload;
import org.zanata.sync.dto.RunningJobKey;
import org.zanata.sync.events.JobProgressEvent;
import org.zanata.sync.events.JobRunCompletedEvent;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.service.JobStatusService;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.service.WorkService;
import com.google.common.base.Strings;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/job")
@Produces("application/json")
public class JobResource {
    private static final Logger log =
            LoggerFactory.getLogger(JobResource.class);

    @Inject
    private SchedulerService schedulerService;

    @Inject
    private WorkService workService;

    @Inject
    private JobStatusService jobStatusService;

    @Inject
    private Event<JobRunCompletedEvent> jobRunCompletedEvent;

    @Inject
    private Event<JobProgressEvent> jobProgressEvent;

    /**
     * Right now this is used as heart beat checker.
     * @return 200 ok
     */
    @HEAD
    @NoSecurityCheck
    public Response head() {
        return Response.ok().build();
    }

    /**
     * Get job status. Used by polling when websockets is not supported.
     *
     * @param id - work identifier
     * @param type - {@link JobType}
     *
     * @return - {@link org.zanata.sync.model.JobStatus}
     */
    @Path("/last/status")
    @GET
    public Response getJobStatus(@QueryParam("id") Long id,
        @QueryParam("type") JobType type) {
        if (id == null || type == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            SyncWorkConfig config = workService.getById(id);
            JobStatus jobStatus = jobStatusService
                    .getLatestJobStatus(config, type);
            return Response.ok(JobRunStatus.fromEntity(jobStatus, id, type))
                    .build();
        } catch (WorkNotFoundException e) {
            log.warn("get job status not found", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Accepts callback from jobs-war app.
     *
     * @param jobFiringId
     *         the job firing id
     * @param status
     *         status
     * @return 200 ok
     */
    @Path("/status")
    @PUT
    // FIXME we may need a different kind of security interceptor here to allow jobs to post back status
    @NoSecurityCheck
    public Response changeJobStatus(@QueryParam("id") String jobFiringId,
            @QueryParam("status") JobStatusType status) {
        if (Strings.isNullOrEmpty(jobFiringId) || status == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<JobStatus> jobStatusOpt =
                jobStatusService.getJobStatusByFiringId(jobFiringId);
        if (!jobStatusOpt.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        JobStatus jobStatus = jobStatusOpt.get();
        SyncWorkConfig workConfig = jobStatus.getWorkConfig();
        JobType jobType = jobStatus.getJobType();
        if (status.isFinished()) {
            jobRunCompletedEvent
                    .fire(JobRunCompletedEvent.finished(jobFiringId, workConfig
                            .getId(), jobType, status));
        } else {
            jobProgressEvent.fire(
                    JobProgressEvent.running(jobFiringId, workConfig.getId()));
        }
        return Response.ok().build();
    }

    /**
     * TODO may not support this
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
        @QueryParam(value = "type") @DefaultValue("") JobType type) {
        try {
            if (Strings.isNullOrEmpty(id)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            schedulerService.cancelRunningJob(new Long(id), type);
            return Response.ok().build();
        } catch (SchedulerException e) {
            log.error("cancel error", e);
            return Response.serverError().build();
        } catch (JobNotFoundException e) {
            log.warn("cancel job not found", e);
            return Response.status(
                    Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Trigger job from the UI.
     *
     * @param id - work identifier
     * @param type - {@link JobType}
     *
     * @return - http code
     */
    @Path("/start")
    @POST
    public Response triggerJob(@QueryParam("id") Long id,
            @QueryParam("type") JobType type) {
        try {
            if (id == null || type == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        Payload.error("missing id and/or type")).build();
            }
            schedulerService.triggerJob(id, type);
            return Response.ok(new RunningJobKey(id, type)).build();
        } catch (SchedulerException e) {
            log.error("trigger job error", e);
            return Response.serverError().build();
        } catch (JobNotFoundException e) {
            log.warn("job not found", e);
            return Response.status(
                    Response.Status.NOT_FOUND).build();
        }
    }

}
