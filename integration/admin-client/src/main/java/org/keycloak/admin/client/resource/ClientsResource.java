package org.keycloak.admin.client.resource;

import org.keycloak.representations.idm.ClientRepresentation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface ClientsResource {

    @Path("{clientId}")
    public ClientResource get(@PathParam("clientId") String clientId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(ClientRepresentation clientRepresentation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientRepresentation> findAll();



}
