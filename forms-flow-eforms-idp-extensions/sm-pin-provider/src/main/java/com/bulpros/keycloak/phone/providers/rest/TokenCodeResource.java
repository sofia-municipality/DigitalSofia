package com.bulpros.keycloak.phone.providers.rest;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.model.CheckForGeneratedCodeResponse;
import com.bulpros.keycloak.phone.providers.model.CodeStatusEnum;
import com.bulpros.keycloak.phone.providers.model.ConfirmStatusResponse;
import com.bulpros.keycloak.phone.providers.model.GetStatusModel;
import com.bulpros.keycloak.phone.providers.model.SendTokenResponse;
import com.bulpros.keycloak.phone.providers.model.UpdateCodeStatusResponse;
import com.bulpros.keycloak.phone.providers.model.UpdateStatusModel;
import com.bulpros.keycloak.phone.providers.spi.PinProvider;
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
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.validation.Validation;

import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class TokenCodeResource {

    protected final KeycloakSession session;
    protected final TokenCodeType tokenCodeType;
    private final AuthenticationManager.AuthResult auth;

    TokenCodeResource(KeycloakSession session, TokenCodeType tokenCodeType) {
        this.session = session;
        this.tokenCodeType = tokenCodeType;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    @GET
    @NoCache
    @Path("generate-code")
    @Produces(APPLICATION_JSON)
    public Response generateTokenCode(@NotBlank @QueryParam("personIdentifier") String personIdentifier) {

        if (Validation.isBlank(personIdentifier))
            throw new BadRequestException("Must provide a personIdentifier!");

        Optional<UserModel> users = Utils.findUserByPersonIdentifier(//
                session, //
                session.getContext().getRealm(), //
                String.format(SpiConstants.PNOBG_PREFIX + personIdentifier //
                ) //
        );

        if (users.isEmpty() || Objects.isNull(users.get().getFirstAttribute(SpiConstants.VERIFIED)) || users.get()
                .getFirstAttribute(SpiConstants.VERIFIED).isEmpty() || !TokenCodeType.AUTH.equals(tokenCodeType)) {
            throw new ForbiddenException("User not found!");
        }

        String fcm = users.get().getFirstAttribute(SpiConstants.FCM);

        if (Objects.isNull(fcm) || fcm.isEmpty()) {
            throw new ForbiddenException("User nas no fcm!");
        }

        SendTokenResponse tokenResponse = session.getProvider(PinProvider.class).sendTokenCode(//
                SpiConstants.PNOBG_PREFIX + personIdentifier, //
                fcm, //
                session.getContext().getConnection().getRemoteAddr(), //
                tokenCodeType //
        );

        return Response.ok(tokenResponse, APPLICATION_JSON_TYPE).build();
    }

    @POST
    @NoCache
    @Path("update-code-status")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response updateCodeStatus(UpdateStatusModel updateStatus) {

        if (auth == null)
            throw new NotAuthorizedException("Bearer");
        if (Validation.isBlank(updateStatus.getCode()))
            throw new BadRequestException("Must provide a code!");
        if (Validation.isBlank(updateStatus.getStatus()))
            throw new BadRequestException("Must provide a status!");

        String personIdentifier = auth.getUser().getFirstAttribute(SpiConstants.PERSON_IDENTIFIER);
        CodeStatusEnum status = CodeStatusEnum.getByWaitingStatusName(updateStatus.getStatus());

        UpdateCodeStatusResponse response = session.getProvider(PinProvider.class).updateCodeStatus( //
                personIdentifier, //
                updateStatus.getCode(), //
                status, //
                tokenCodeType //
        );

        return Response.ok(response, APPLICATION_JSON).build();
    }

    @GET
    @NoCache
    @Path("code-status")
    @Produces(APPLICATION_JSON)
    public Response checkForGeneratedCode() {

        if (auth == null)
            throw new NotAuthorizedException("Bearer");

        try {
            CheckForGeneratedCodeResponse response = session.getProvider(PinProvider.class).checkForGeneratedCode(//
                    auth.getUser().getFirstAttribute(SpiConstants.PERSON_IDENTIFIER), //
                    tokenCodeType //
            );
            return Response.ok(response, APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @NoCache
    @Path("check-confirm-status")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response checkConfirmStatus(GetStatusModel statusModel) {

        try {
            ConfirmStatusResponse response = session.getProvider(PinProvider.class).checkConfirmStatus(//
                    statusModel.getPersonIdentifier(), //
                    statusModel.getCode(), //
                    tokenCodeType //
            );
            return Response.ok(response, APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
