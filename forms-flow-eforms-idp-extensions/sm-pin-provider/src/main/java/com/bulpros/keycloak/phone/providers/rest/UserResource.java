package com.bulpros.keycloak.phone.providers.rest;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.model.GetUserLogLevelResponse;
import com.bulpros.keycloak.phone.providers.model.RegisterUserModel;
import com.bulpros.keycloak.phone.providers.model.UpdateFCMModel;
import com.bulpros.keycloak.phone.providers.model.UpdatePinModel;
import com.google.common.base.Strings;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.validation.Validation;

import java.util.Optional;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class UserResource {

    private final AuthenticationManager.AuthResult auth;

    private final KeycloakSession session;

    public UserResource(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    @POST
    @NoCache
    @Path("update-pin")
    @Consumes(APPLICATION_JSON)
    public Response updatePin(UpdatePinModel updatePinModel) {

        if (auth == null)
            throw new NotAuthorizedException("Bearer");

        if (Strings.isNullOrEmpty(updatePinModel.getPin()))
            throw new BadRequestException("There is no new pin provided.");

        String isVerified = auth.getUser().getFirstAttribute(SpiConstants.VERIFIED);

        if (Strings.isNullOrEmpty(isVerified) || !Boolean.parseBoolean(isVerified.trim()))
            throw new ForbiddenException("User is not verified.");

        auth.getUser().setSingleAttribute(SpiConstants.PIN, updatePinModel.getPin());
        return Response.noContent().build();
    }

    @POST
    @NoCache
    @Path("update-fcm")
    @Consumes(APPLICATION_JSON)
    public Response updateFCM(UpdateFCMModel updateFCMModel) {

        if (auth == null)
            throw new NotAuthorizedException("Bearer");

        if (Strings.isNullOrEmpty(updateFCMModel.getFcm()))
            throw new BadRequestException("There is no new fcm provided.");

        String isVerified = auth.getUser().getFirstAttribute(SpiConstants.VERIFIED);

        if (Strings.isNullOrEmpty(isVerified) || !Boolean.parseBoolean(isVerified.trim()))
            throw new ForbiddenException("User is not verified.");

        auth.getUser().setSingleAttribute(SpiConstants.FCM, updateFCMModel.getFcm());
        return Response.noContent().build();
    }

    @POST
    @NoCache
    @Path("delete")
    public Response deleteUser() {

        if (auth == null)
            throw new NotAuthorizedException("Bearer");

        String isVerified = auth.getUser().getFirstAttribute(SpiConstants.VERIFIED);

        if (Strings.isNullOrEmpty(isVerified) || !Boolean.parseBoolean(isVerified.trim()))
            throw new ForbiddenException("User is not verified.");

        RealmModel realm = auth.getSession().getRealm();
        UserModel user = auth.getUser();

        if (Utils.deleteUser(session, realm, user)) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @NoCache
    @Path("register")
    @Consumes(APPLICATION_JSON)
    public Response registerUser(RegisterUserModel registerUserModel) {

        if (Utils.isPersonIdentifierNotValid(registerUserModel.getPersonIdentifier())) {
            throw new BadRequestException("Not valid person identifier!");
        }

        RealmModel realm = session.getContext().getRealm();

        Optional<UserModel> existingUser = Utils.findUserByPersonIdentifier(//
                session, //
                realm, //
                SpiConstants.PNOBG_PREFIX + registerUserModel.getPersonIdentifier() //
        );

        if (existingUser.isPresent()) {
            return Response.noContent().build();
        }

        Utils.createNewUser(//
                session, //
                realm, //
                SpiConstants.PNOBG_PREFIX + registerUserModel.getPersonIdentifier() //
        );
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @NoCache
    @Path("log")
    @Produces(APPLICATION_JSON)
    public Response getUserLogLevel(@NotBlank @QueryParam("personIdentifier") String personIdentifier) {

        if (Validation.isBlank(personIdentifier))
            throw new BadRequestException("Must provide a personIdentifier!");

        RealmModel realm = session.getContext().getRealm();
        Optional<UserModel> existingUser = Utils.findUserByPersonIdentifier(//
                session, //
                realm, //
                SpiConstants.PNOBG_PREFIX + personIdentifier //
        );

        GetUserLogLevelResponse userLogLevelResponse = new GetUserLogLevelResponse(0);

        if (existingUser.isPresent() && ("1").equals(existingUser.get().getFirstAttribute(SpiConstants.LOG_LEVEL))) {
            userLogLevelResponse.setLogLevel(1);
        }

        return Response.ok(userLogLevelResponse, APPLICATION_JSON_TYPE).build();
    }
}
