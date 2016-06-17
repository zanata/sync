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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.Payload;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.model.JobProgress;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobSummary;
import org.zanata.sync.model.JobType;
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
    private SchedulerService schedulerServiceImpl;

    @Inject
    private WorkService workServiceImpl;

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
        JobType type) {
        try {
            if (Strings.isNullOrEmpty(id) || type == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(schedulerServiceImpl
                .getLatestJobStatus(new Long(id), type)).build();
        } catch (SchedulerException e) {
            log.error("get job status error", e);
            return Response.serverError().build();
        } catch (JobNotFoundException e) {
            log.warn("get job status not found", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

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
        @QueryParam(value = "type") @DefaultValue("") JobType type) {
        try {
            if (Strings.isNullOrEmpty(id)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            schedulerServiceImpl.cancelRunningJob(new Long(id), type);
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
     * trigger job
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
            schedulerServiceImpl.triggerJob(id, type);
            return Response.ok().build();
        } catch (SchedulerException e) {
            log.error("trigger job error", e);
            return Response.serverError().build();
        } catch (JobNotFoundException e) {
            log.warn("job not found", e);
            return Response.status(
                    Response.Status.NOT_FOUND).build();
        }
    }

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
    public Response getJob(
        @QueryParam(value = "id") @DefaultValue("") String id,
        @QueryParam(value = "type") @DefaultValue("") JobType type,
        @QueryParam(value = "status") @DefaultValue("") JobStatusType status) {

        boolean filterByKey = !Strings.isNullOrEmpty(id) && type != null;

        if ((!Strings.isNullOrEmpty(id) || type != null) && !filterByKey) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // TODO refactor and comment on on this block
        try {
            List<JobSummary> jobs = schedulerServiceImpl.getJobs();
            if(status == null && Strings.isNullOrEmpty(id) && type == null) {
                return Response.ok(jobs).build();
            } else {
                List<JobSummary> filteredList = new ArrayList<>();
                boolean filterByStatus = status != null;

                for (JobSummary summary : jobs) {
                    if (filterByKey && filterByStatus) {
                        JobKey key = type.toJobKey(new Long(id));
                        if (summary.getJobKey().equals(key.toString())
                                && isMatchStatus(summary.getLastJobStatus(),
                                        status)) {
                            filteredList.add(summary);
                            continue;
                        }
                    } else if (filterByKey) {
                        JobKey key = type.toJobKey(new Long(id));
                        if (summary.getJobKey().equals(key.toString())) {
                            filteredList.add(summary);
                            continue;
                        }
                    } else if (filterByStatus && isMatchStatus(
                            summary.getLastJobStatus(), status)) {
                        filteredList.add(summary);
                        continue;
                    }
                }
                return Response.ok(filteredList).build();
            }
        } catch (SchedulerException e) {
            log.error("fail getting running jobs", e);
            return Response.serverError().build();
        }
    }

    private boolean isMatchStatus(JobStatus jobStatus, JobStatusType status) {
        if(status.equals(JobStatusType.RUNNING)) {
            JobProgress currentProgress = jobStatus.getCurrentProgress();
            if(currentProgress != null && currentProgress.getStatus().equals(status)) {
                return true;
            }
        }
        return jobStatus.getStatus().equals(status);
    }
}
