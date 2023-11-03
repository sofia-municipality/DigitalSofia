package com.bulpros.keycloak.phone.providers.rest;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.model.PersonIdentifierResponse;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class PersonIdentifierResource {

    private final KeycloakSession session;

    public PersonIdentifierResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @NoCache
    @Path("user")
    @Produces(APPLICATION_JSON)
    public Response checkIfUserExistsByPersonIdentifier(@QueryParam("personIdentifier") String personIdentifier) {

        Optional<UserModel> users = Utils.findUserByPersonIdentifier(//
                session, //
                session.getContext().getRealm(), //
                String.format(SpiConstants.PNOBG_PREFIX + personIdentifier //
                ));
        if (users.isEmpty()) {
            PersonIdentifierResponse response = new PersonIdentifierResponse(false);
            return Response.ok(response, APPLICATION_JSON).build();
        } else {
            PersonIdentifierResponse response = new PersonIdentifierResponse(true);
            return Response.ok(response, APPLICATION_JSON).build();
        }
    }
}
