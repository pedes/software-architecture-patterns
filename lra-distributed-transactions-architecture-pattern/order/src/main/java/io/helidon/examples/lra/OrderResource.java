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

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import io.narayana.lra.filter.ClientLRARequestFilter;
import io.narayana.lra.filter.ClientLRAResponseFilter;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_PARENT_CONTEXT_HEADER;

@Path("/order")
@ApplicationScoped
public class OrderResource {

    private Client client;
    private ParticipantStatus participantStatus;
    private String orderStatus = "none";

    public OrderResource() {
        client = ClientBuilder.newBuilder()
                .register(ClientLRARequestFilter.class)
                .register(ClientLRAResponseFilter.class)
                .build();
    }

    @Path("/placeOrder")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(value = LRA.Type.REQUIRES_NEW)
    public Response placeOrder(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId)  {
        System.out.println("OrderResource.placeOrder in LRA due to LRA.Type.REQUIRES_NEW lraId:" + lraId);
        participantStatus = ParticipantStatus.Active;
        orderStatus = "pending";
        Response response = client.target("http://localhost:8091/inventory/reserveInventoryForOrder")
                .request().get();
        String entity = response.readEntity(String.class);
        System.out.println("OrderResource.placeOrder response from inventory:" + entity);
        switch (entity) {
            case "inventorysuccess":
                orderStatus = "completed";
                return Response.ok()
                        .entity("orderStatus:" + orderStatus)
                        .build();
            case "inventoryfailure":
                orderStatus = "failed";
                return Response.serverError()
                        .entity("orderStatus:" + orderStatus)
                        .build();
            default:
                orderStatus = "unknown";
                return Response.serverError()
                        .entity("orderStatus:" + orderStatus)
                        .build();
        }
    }

    @Path("/cancelOrder")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Compensate
    public Response cancelOrder(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws NotFoundException {
        participantStatus = ParticipantStatus.Compensating;
        System.out.println("OrderResource.cancelOrder lraId:" + lraId);
        orderStatus = "cancelled";
        participantStatus = ParticipantStatus.Compensated;
        return Response.ok().entity(participantStatus.name()).build();
    }

    @PUT
    @Path("/completeOrder")
    @Produces(MediaType.APPLICATION_JSON)
    @Complete
    public Response completeOrder(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws NotFoundException {
        participantStatus = ParticipantStatus.Completing;
        System.out.println("OrderResource.completeOrder lraId:" + lraId);
        participantStatus = ParticipantStatus.Completed;
        return Response.ok().entity(participantStatus.name()).build();
    }

    @GET
    @Path("/status")
    @Status
    public Response status(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId,
                           @HeaderParam(LRA_HTTP_PARENT_CONTEXT_HEADER) URI parentlraId) {
        System.out.println("OrderResource.status participantStatus:" + participantStatus +
                " lraId:" + lraId + " parentlraId:" + parentlraId);
        return Response.ok().entity(participantStatus).build();
    }

}
