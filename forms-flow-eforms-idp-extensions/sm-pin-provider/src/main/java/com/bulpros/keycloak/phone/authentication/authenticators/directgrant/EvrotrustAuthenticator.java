package com.bulpros.keycloak.phone.authentication.authenticators.directgrant;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.exception.CustomProviderException;
import com.bulpros.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import com.bulpros.keycloak.phone.providers.model.UserCheckModel;
import com.bulpros.keycloak.phone.providers.model.UserExtendedModel;
import com.bulpros.keycloak.phone.providers.spi.UserCheckProvider;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;

public class EvrotrustAuthenticator extends BaseDirectGrantAuthenticator {

    private static final Logger logger = Logger.getLogger(EvrotrustAuthenticator.class);

    public EvrotrustAuthenticator(KeycloakSession session) {
        if (session.getContext().getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String phoneNumber = getUserPhoneNumber(context);
        String userEmail = getEmail(context);
        String userFcm = getFcm(context);
        String pin = getPinNumber(context);
        String egn = getEgn(context);

        if (Objects.isNull(pin) || Objects.isNull(egn)) {
            invalidCredentials(context);
            return;
        }
        authToUser(context, userEmail, pin, egn, phoneNumber, userFcm);
    }

    private String getUserPhoneNumber(AuthenticationFlowContext context) {
        String phoneNumber;
        try {
            phoneNumber = getPhoneNumber(context);
            if (Objects.isNull(phoneNumber))
                return null;
            Utils.canonicalizePhoneNumber(context.getSession(), phoneNumber);
        } catch (PhoneNumberInvalidException e) {
            throw new BadRequestException("Phone number is invalid!");
        }
        return phoneNumber;
    }

    private void authToUser(AuthenticationFlowContext context, String email, String pin, String egn, String phoneNumber,
            String fcm) {
        UserCheckProvider userCheckProvider = context.getSession().getProvider(UserCheckProvider.class);

        UserCheckModel userCheck = new UserCheckModel();
        userCheck.setIdentificationNumber(egn);
        userCheck.setCountry("BG");
        UserExtendedModel evrotrustUser;
        try {
            evrotrustUser = userCheckProvider.getEvrotrustUser(userCheck);
            if (!evrotrustUser.getIsRegistered()) {
                invalidCredentials(context, Response.Status.FORBIDDEN, "invalid evrotrust user",
                        "User does not have active evrotrust profile!");
                return;
            }
        } catch (CustomProviderException e) {
            logger.error("Evrotrust service is unavailable or doesn't response");
            invalidCredentials(context, Response.Status.FORBIDDEN, "evrotrust service error", e.getMessage());
            return;
        }
        String personIdentifier = SpiConstants.PNOBG_PREFIX + egn;

        Optional<UserModel> userModel = Utils.findUserByPersonIdentifier(context.getSession(), context.getRealm(),
                personIdentifier);
        if (userModel.isEmpty()) {
            createNewUser(context, email, pin, phoneNumber, fcm, personIdentifier);
            return;
        }
        UserModel user = userModel.get();
        if (Objects.isNull(getVerifiedAttribute(user))) {
            updateUserData(context, email, pin, phoneNumber, fcm, user);
            return;
        }

        if (isUserVerified(user)) {
            if (!evrotrustUser.getIsIdentified()) {
                user.setSingleAttribute(SpiConstants.VERIFIED, Boolean.FALSE.toString());
            }
        }

        if (!isUserDataCorrect(user, pin, personIdentifier)) {
            invalidCredentials(context, Response.Status.FORBIDDEN, "invalid user data", "User data is incorrect!");
            return;
        }
        context.setUser(user);
        context.success();
    }

    private static void updateUserData(AuthenticationFlowContext context, String email, String pin, String phoneNumber,
            String fcm, UserModel user) {
        user.setSingleAttribute(SpiConstants.VERIFIED, Boolean.FALSE.toString());
        user.setSingleAttribute(SpiConstants.PIN, pin);
        user.setSingleAttribute(SpiConstants.PHONE_NUMBER, phoneNumber);
        user.setSingleAttribute(SpiConstants.FCM, fcm);
        user.setSingleAttribute(SpiConstants.EVROTRUST_EMAIL, email);
        context.setUser(user);
        context.success();
    }

    private boolean isUserVerified(UserModel user) {
        String verifiedAttribute = getVerifiedAttribute(user);
        return Boolean.TRUE.toString().equalsIgnoreCase(verifiedAttribute);
    }

    private String getVerifiedAttribute(UserModel user) {
        return user.getFirstAttribute(SpiConstants.VERIFIED);
    }

    private boolean isUserDataCorrect(UserModel user, String pin, String personIdentifier) {
        return pin.equals(user.getFirstAttribute(SpiConstants.PIN)) && personIdentifier.equals(
                user.getFirstAttribute(SpiConstants.PERSON_IDENTIFIER));
    }

    private void createNewUser(AuthenticationFlowContext context, String email, String pin, String phoneNumber,
            String fcm, String personIdentifier) {

        if (Objects.isNull(email) || Objects.isNull(phoneNumber)) {
            invalidCredentials(context, Response.Status.FORBIDDEN, "invalid user data",
                    "User data is incorrect or missing!");
            return;
        }

        UserModel newUser = context.getSession().users().addUser(context.getRealm(), email);
        newUser.setEnabled(true);
        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, email);

        newUser.setUsername(personIdentifier);
        newUser.setSingleAttribute(SpiConstants.VERIFIED, Boolean.FALSE.toString());
        newUser.setSingleAttribute(SpiConstants.PIN, pin);
        newUser.setSingleAttribute(SpiConstants.PHONE_NUMBER, phoneNumber);
        newUser.setSingleAttribute(SpiConstants.PERSON_IDENTIFIER, personIdentifier);
        newUser.setSingleAttribute(SpiConstants.FCM, fcm);
        newUser.setSingleAttribute(SpiConstants.EVROTRUST_EMAIL, email);
        context.setUser(newUser);
        context.success();
    }

}
