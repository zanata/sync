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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.sync.dto.RepoAccountDto;
import org.zanata.sync.dto.ZanataUserAccount;
import org.zanata.sync.model.RepoAccount;
import org.zanata.sync.model.ZanataAccount;
import org.zanata.sync.service.AccountService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {
    @Inject
    private AccountService accountService;

    @GET
    @Path("/zanata")
    public Response getZanataAccount() {
        ZanataAccount accountForCurrentUser =
                accountService.getZanataAccountForCurrentUser();
        ZanataUserAccount dto =
                ZanataUserAccount.fromEntity(accountForCurrentUser);
        return Response.ok(dto).build();
    }

    // TODO pahuang run bean validation for the two dto
    @PUT
    @Path("/zanata")
    public Response saveZanataAccount(ZanataUserAccount zanataUserAccount) {
        ZanataAccount entity =
                accountService.updateZanataAccount(zanataUserAccount);
        return Response.ok(ZanataUserAccount.fromEntity(entity)).build();
    }

    @POST
    @Path("/repo")
    public Response saveRepoAccount(RepoAccountDto repoAccount) {
        RepoAccount entity =
                accountService.saveRepoAccountForCurrentUser(repoAccount);
        return Response
                .ok(ZanataUserAccount.fromEntity(entity.getZanataAccount()))
                .build();
    }
}
