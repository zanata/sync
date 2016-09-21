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
package org.zanata.sync.jobs.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.common.model.SyncOption;
import org.zanata.sync.jobs.common.Either;
import org.zanata.sync.jobs.common.model.ErrorMessage;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;
import org.zanata.sync.jobs.ejb.JobRunner;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.jobs.plugin.zanata.ZanataSyncService;
import org.zanata.sync.jobs.plugin.zanata.service.impl.ZanataSyncServiceImpl;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/job")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobResource {
    private static final Logger log =
            LoggerFactory.getLogger(JobResource.class);
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Inject
    private JobRunner jobRunner;

    @Inject
    @RepoPlugin
    private Map<String, RepoSyncService> repoTypeToServiceMap;

    // TODO until we make trigger job an aync task, we won't be able to get status or cancel running job (To make it an async task, we will need database backend to store running job)

    /**
     * Get job status
     *
     * @param id
     *         - job identifier
     */
    @Path("status/{id}")
    @GET
    public Response getJobStatus(@PathParam(value = "id") String id) {

        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    /**
     * Cancel job if it is running
     *
     * @param id
     *         - job identifier
     * @return - http code
     */
    @Path("cancel/{id}")
    @POST
    public Response cancelRunningJob(@PathParam("id") String id) {
//        try {
//            if (Strings.isNullOrEmpty(id)) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }
//            schedulerServiceImpl.cancelRunningJob(new Long(id), type);
//            return Response.ok().build();
//        } catch (SchedulerException e) {
//            log.error("cancel error", e);
//            return Response.serverError().build();
//        } catch (JobNotFoundException e) {
//            log.warn("cancel job not found", e);
        return Response.status(
                Response.Status.SERVICE_UNAVAILABLE).build();
//        }
    }

    @OPTIONS
    public Response options() {
        // TODO return supported method and their accepted body (e.g. jobDetail map)
        return Response.ok().build();
    }

    /**
     * trigger sync to zanata job (push files from source repo to zanata).
     *
     * @param id
     *         - work identifier
     * @param jobDetail
     *         detail about a job.
     * @return - http code
     */
    @Path("/2zanata/start/{id}")
    @POST
    public Response triggerJobToSyncToZanata(@PathParam(value = "id") String id,
            SyncJobDetail jobDetail) {
        Set<ConstraintViolation<SyncJobDetail>> violations =
                validator.validate(jobDetail);
        if (!violations.isEmpty()) {
            List<ErrorMessage> errors = violations.stream()
                    .map(violation -> new ErrorMessage(
                            violation.getPropertyPath().toString(),
                            violation.getMessage())).collect(
                            Collectors.toList());
            return Response.status(Response.Status.BAD_REQUEST).entity(errors)
                    .build();
        }

        Either<RepoSyncService, Response> srcRepoPlugin =
                createRepoSyncService(jobDetail);
        Either<ZanataSyncService, Response> zanataSyncService =
                createZanataSyncService(jobDetail);

        log.info(">>>>>> about to run 2zanata job for {}", id);
        jobRunner.syncToZanata(srcRepoPlugin, zanataSyncService, id);
        // TODO return a URI to get access to the async job
        return Response.created(URI.create(id)).build();
    }

    private Either<RepoSyncService, Response> createRepoSyncService(
            SyncJobDetail jobDetail) {
        String repoUrl = jobDetail.getSrcRepoUrl();
        String repoUsername = jobDetail.getSrcRepoUsername();
        String repoSecret = Strings.nullToEmpty(jobDetail.getSrcRepoSecret());
        String repoBranch = jobDetail.getSrcRepoBranch();
        String repoType = jobDetail.getSrcRepoType();

        RepoSyncService service = repoTypeToServiceMap.get(repoType);
        service.setCredentials(
                new UsernamePasswordCredential(repoUsername, repoSecret));
        service.setUrl(repoUrl);
        service.setBranch(repoBranch);

        String zanataUsername = jobDetail.getZanataUsername();

        service.setZanataUser(zanataUsername);
        return Either.fromLeft(service, Response.class);
    }

    private static Either<ZanataSyncService, Response> createZanataSyncService(
            SyncJobDetail jobDetail) {
        // TODO at the moment we assumes zanata.xml is in the repo so this is not needed
        String zanataUrl = jobDetail.getZanataUrl();
        String zanataUsername = jobDetail.getZanataUsername();
        String zanataSecret = jobDetail.getZanataSecret();
        SyncOption syncToZanataOption = jobDetail.getSyncToZanataOption();
        String pushToZanataOption =
                syncToZanataOption != null ? syncToZanataOption.getValue() :
                        null;

        return Either.fromLeft(
                new ZanataSyncServiceImpl(zanataUrl, zanataUsername,
                        zanataSecret,
                        pushToZanataOption), Response.class);
    }

    /**
     * trigger job to sync to source repo (pull translation from zanata then
     * commit and push to source repo).
     *
     * @param id
     *         - work identifier
     * @param jobDetail
     *         detail about a job.
     * @return - http code
     */
    @Path("2repo/start/{id}")
    @POST
    public Response triggerJobToSyncToSourceRepo(
            @PathParam(value = "id") String id,
            SyncJobDetail jobDetail) {
        Set<ConstraintViolation<SyncJobDetail>> violations =
                validator.validate(jobDetail);
        if (!violations.isEmpty()) {
            List<ErrorMessage> errors = violations.stream()
                    .map(violation -> new ErrorMessage(
                            violation.getPropertyPath().toString(),
                            violation.getMessage())).collect(
                            Collectors.toList());
            return Response.status(Response.Status.BAD_REQUEST).entity(errors)
                    .build();
        }

        Either<RepoSyncService, Response> srcRepoPlugin =
                createRepoSyncService(jobDetail);
        Either<ZanataSyncService, Response> zanataSyncService =
                createZanataSyncService(jobDetail);

        log.info(">>>>>> about to run 2repo job for {}", id);
        jobRunner.syncToSrcRepo(id, srcRepoPlugin, zanataSyncService);

        // TODO create URI to access running async job
        return Response.created(URI.create(id)).build();
    }


}
