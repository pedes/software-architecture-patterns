/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.examples.lra;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.lra.annotation.*;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;

@Path("/inventory")
@ApplicationScoped
public class InventoryResource {


    private ParticipantStatus participantStatus;
    private int inventoryCount = 1; //0 or 1, either have one or don't

    @Path("/reserveInventoryForOrder")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(value = LRA.Type.MANDATORY, end = false)
    public Response reserveInventoryForOrder(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId)  {
        boolean inventoryExists = inventoryCount > 0;
        System.out.println("InventoryResource.placeOrder in LRA due to LRA.Type.MANDATORY " +
                "inventoryExists:" + inventoryExists + "lraID:" + lraId);
        participantStatus = ParticipantStatus.Active;
        if(inventoryExists) return Response.ok()
                .entity("inventorysuccess")
                .build();
        else return  Response.ok()
                .entity("inventoryfailure")
                .build();
    }

    @Path("/cancelOrder")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response cancelOrder(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws NotFoundException {
        participantStatus = ParticipantStatus.Compensating;
        System.out.println("InventoryResource.cancelOrder put inventory back if any lraId:" + lraId);
        participantStatus = ParticipantStatus.Compensated;
        return Response.status(Response.Status.OK).entity(participantStatus.name()).build();
    }

    @PUT
    @Path("/completeOrder")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeOrder(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws NotFoundException {
        participantStatus = ParticipantStatus.Completing;
        System.out.println("InventoryResource.completeOrder prepare item for shipping lraId:" + lraId);
        participantStatus = ParticipantStatus.Completed;
        return Response.status(Response.Status.OK).entity(participantStatus.name()).build();
    }

    @GET
    @Path("/status")
    @Status
    public Response status(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                           @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parent) {
        System.out.println("InventoryResource.status participantStatus:" + participantStatus + " lraId:" + lraId);
        return Response.ok().entity(participantStatus).build();
    }

    @POST
    public Response addInventory() {
        System.out.println("InventoryResource.addInventory");
        inventoryCount = 1;
        return Response.ok()
                .entity("inventoryCount = 1")
                .build();
    }

    @DELETE
    public Response removeInventory() {
        System.out.println("InventoryResource.removeInventory");
        inventoryCount = 0;
        return Response.ok()
                .entity("inventoryCount = 0")
                .build();
    }

}
