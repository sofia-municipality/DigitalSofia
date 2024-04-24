package com.bulpros.keycloak.phone.providers.rest;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.model.PersonIdentifierResponse;
import com.google.common.base.Strings;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.Optional;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

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
        PersonIdentifierResponse response = new PersonIdentifierResponse();

        if (users.isEmpty()) {
            response.setUserExist(false);
            response.setHasPin(false);
            response.setHasContactInfo(false);
            response.setVerified(false);
        } else {
            response.setUserExist(true);
            response.setHasPin(users.get().getFirstAttribute(SpiConstants.PIN) != null);
            boolean contactInfo = Strings.isNullOrEmpty(users.get().getEmail()) || Strings.isNullOrEmpty(
                    users.get().getFirstAttribute(SpiConstants.PHONE_NUMBER)) ? false : true;
            response.setHasContactInfo(contactInfo);
            response.setVerified(Boolean.TRUE.toString().equals(users.get().getFirstAttribute(SpiConstants.VERIFIED)));
        }
        return Response.ok(response, APPLICATION_JSON).build();
    }

}
