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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dto.Payload;
import org.zanata.sync.dto.SyncWorkForm;
import org.zanata.sync.dto.WorkDetail;
import org.zanata.sync.dto.ZanataWebHookEvent;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.exception.WorkNotFoundException;
import org.zanata.sync.jobs.RemoteJobExecutor;
import org.zanata.sync.model.JobStatus;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.RepoAccount;
import org.zanata.sync.model.SyncWorkConfig;
import org.zanata.sync.model.SyncWorkConfigBuilder;
import org.zanata.sync.dto.WorkSummary;
import org.zanata.sync.model.ZanataAccount;
import org.zanata.sync.security.SecurityTokens;
import org.zanata.sync.service.AccountService;
import org.zanata.sync.service.JobStatusService;
import org.zanata.sync.service.PluginsService;
import org.zanata.sync.service.SchedulerService;
import org.zanata.sync.service.WorkService;
import org.zanata.sync.util.HmacUtil;
import org.zanata.sync.util.JSONObjectMapper;
import org.zanata.sync.validation.SyncWorkFormValidator;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

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
    private SchedulerService schedulerService;

    @Inject
    private WorkService workService;

    @Inject
    private JobStatusService jobStatusService;

    @Inject
    private AccountService accountService;

    @Inject
    private SyncWorkFormValidator formValidator;

    @Inject
    private SyncWorkConfigBuilder syncWorkConfigBuilder;

    @Inject
    private PluginsService pluginsService;

    @Inject
    private SecurityTokens securityTokens;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpHeaders httpHeaders;

    @Inject
    private JSONObjectMapper objectMapper;

    @Inject
    private RemoteJobExecutor remoteJobExecutor;


    /**
     * Use this to check whether frontend data is still valid against the
     * server session (e.g. zanata user held by frontend js is the one stored
     * in server session).
     * @return 200 ok
     *         401 Unauthorized if server logged in session has timed out or gone.
     */
    @HEAD
    public Response head() {
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    public Response
        getWork(@PathParam(value = "id") Long id) {

        try {
            // TODO doing two queries. optimize
            SyncWorkConfig workConfig = workService.getById(id);
            List<JobStatus> allJobStatus =
                    jobStatusService.getAllJobStatus(workConfig);

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
        List<WorkSummary> workSummaries = workService.getWorkForCurrentUser();
        return Response.ok(workSummaries).build();
    }

    @POST
    public Response createWork(SyncWorkForm form) {
        Map<String, String> errors = formValidator.validate(form);
        if (!errors.isEmpty()) {
            return Response.status(BAD_REQUEST).entity(errors).build();
        }
        ZanataAccount zanataAccount =
                accountService.getZanataAccountForCurrentUser();
        Optional<RepoAccount> repoAccount = zanataAccount.getRepoAccounts().stream()
                .filter(acc -> acc.getId()
                        .equals(form.getSrcRepoAccountId())).findAny();

        if (!repoAccount.isPresent()) {
            return Response.status(BAD_REQUEST).entity(Payload
                    .error("Source Repo ID not found: " +
                            form.getSrcRepoAccountId())).build();
        }

        SyncWorkConfig syncWorkConfig =
                syncWorkConfigBuilder.buildObject(form, zanataAccount, repoAccount.get());
        // TODO pahuang here we should persist the refresh token
        try {
            workService.updateOrPersist(syncWorkConfig);
            schedulerService.scheduleWork(syncWorkConfig);
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
        Map<String, String> errors = formValidator.validate(form);
        if (!errors.isEmpty()) {
            return Response.status(BAD_REQUEST).entity(errors).build();
        }
        /* TODO not supported yet
        ZanataAccount zanataAccount = getZanataAccount(form);
        SyncWorkConfig syncWorkConfig = syncWorkConfigBuilder.buildObject(form,
                zanataAccount);

        try {
            workService.updateOrPersist(syncWorkConfig);
            schedulerService.rescheduleWork(syncWorkConfig);
        } catch (SchedulerException e) {
            log.error("Error rescheduling work", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errors).build();
        }*/
        // TODO create URI
        return Response.created(URI.create("")).entity(errors).build();
    }

    @Path("/{id}/translation/changed")
    @POST
    @NoSecurityCheck
    // TODO ZNTA-1290 we may need to whitelist zanata server urls that can trigger this endpoint
    public Response zanataWebHook(@PathParam("id") Long id, String payload) {
        log.debug("webhook event received from {}", request.getRemoteAddr());
        Optional<SyncWorkConfig> configOpt = workService.load(id);
        if (!configOpt.isPresent()) {
            log.warn("can not find config for id {}", id);
            return Response.status(NOT_FOUND).build();
        }

        SyncWorkConfig config = configOpt.get();
        // TODO ZNTA-1290 store webhook secret in the config and get it back here

        List<String> headerHashes =
                httpHeaders.getRequestHeader("X-Zanata-Webhook");
        if (!headerHashes.isEmpty()) {
            String headerHash = headerHashes.get(0);
            String expectedHash = HmacUtil.signWebHookHeader(payload, "secret",
                    request.getRequestURL().toString());

            if (!headerHash.equals(expectedHash)) {
                log.warn("webhook hash does not match content");
                return Response.status(BAD_REQUEST).build();
            }
        }
        log.debug("webhook payload: {}", payload);

        // TODO ZNTA-1290 get language from the payload and only trigger job for that language
        // payload: {"username":"admin","project":"test-repo","version":"master","docId":"book","locale":"ja","wordDeltasByState":{"New":-4,"Translated":4},"type":"DocumentStatsEvent"}
        ZanataWebHookEvent webHookEvent =
                objectMapper.fromJSON(ZanataWebHookEvent.class, payload);

        // bypass schedulerService since it's not triggered by schedule but webhook
        remoteJobExecutor.executeJob(UUID.randomUUID().toString(), config, JobType.REPO_SYNC);

        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteWork(@PathParam("id") Long id) {
        log.info("========== about to delete {}", id);
        workService.deleteWork(id);
        return Response.status(Response.Status.OK).build();
    }

}
