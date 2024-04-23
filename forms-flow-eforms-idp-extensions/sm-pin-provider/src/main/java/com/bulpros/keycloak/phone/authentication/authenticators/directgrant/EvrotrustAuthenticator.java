package com.bulpros.keycloak.phone.authentication.authenticators.directgrant;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.exception.CustomProviderException;
import com.bulpros.keycloak.phone.providers.model.UserCheckModel;
import com.bulpros.keycloak.phone.providers.model.UserExtendedModel;
import com.bulpros.keycloak.phone.providers.representations.MobileLoginRepresentation;
import com.bulpros.keycloak.phone.providers.spi.MobileLoginProvider;
import com.bulpros.keycloak.phone.providers.spi.UserCheckProvider;
import com.google.common.base.Strings;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class EvrotrustAuthenticator extends BaseDirectGrantAuthenticator {

    private static final Logger logger = Logger.getLogger(EvrotrustAuthenticator.class);
    private final KeycloakSession session;
    private final Config.Scope config;

    public EvrotrustAuthenticator(KeycloakSession session, Config.Scope config) {
        this.session = session;
        this.config = config;

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

    private MobileLoginProvider getMobileLoginService() {
        return session.getProvider(MobileLoginProvider.class);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String egn = getEgn(context);

        if (Strings.isNullOrEmpty(egn)) {
            invalidCredentials(context);
            return;
        }
        authToUser(context, egn);
    }

    private void authToUser(AuthenticationFlowContext context, String egn) {
        UserCheckProvider userCheckProvider = context.getSession().getProvider(UserCheckProvider.class);

        UserCheckModel userCheck = new UserCheckModel();
        userCheck.setIdentificationNumber(egn);
        userCheck.setCountry("BG");
        try {
            UserExtendedModel userExtendedModel = userCheckProvider.getEvrotrustUser(userCheck);
            if (Boolean.FALSE.equals(userExtendedModel.getIsIdentified()) || //
                    Boolean.FALSE.equals(userExtendedModel.getIsRegistered()) || //
                    Boolean.FALSE.equals(userExtendedModel.getIsReadyToSign())) { //
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
            invalidCredentials(context, Response.Status.FORBIDDEN, "Invalid user.", "User does not exist!");
            return;
        }

        UserModel user = userModel.get();
        if (!isUserVerified(user)) {
            invalidCredentials(context, Response.Status.FORBIDDEN, "invalid user data", "User data is incorrect!");
            return;
        }
        if (!isUserDataCorrect(user, getPinNumber(context), personIdentifier)) {
            if (!checkIfUserIsAllowedToLogin(context, true)) {
                invalidCredentials(context, Response.Status.FORBIDDEN, "Login count exceeded!",
                        "Login count exceeded!");
            } else {
                invalidCredentials(context, Response.Status.FORBIDDEN, "invalid user data", "User data is incorrect!");
            }
            return;
        }
        if (!checkIfUserIsAllowedToLogin(context, false)) {
            invalidCredentials(context, Response.Status.FORBIDDEN, "Login count exceeded!", "Login count exceeded!");
            return;
        }
        ;
        updateFcm(getFcm(context), user);
        context.setUser(user);
        context.success();
    }

    private boolean checkIfUserIsAllowedToLogin(AuthenticationFlowContext context, boolean falseCredentialDetect) {
        MobileLoginProvider loginProvider = getMobileLoginService();
        int loginExpiresIn = loginProvider.getLoginExpiresIn();
        MobileLoginRepresentation ongoingMobileLogin = loginProvider.ongoingMobileLogin(//
                getEgn(context), //
                getClientId(context), //
                getGrantType(context), //
                getScope(context) //
        );

        if (ongoingMobileLogin != null) {
            Date expiresAt = new Date(ongoingMobileLogin.getCreatedAt().getTime() + loginExpiresIn * 1000L);
            boolean isExpired = Date.from(Instant.now()).after(expiresAt);
            int ongoingSumLoginCount = ongoingMobileLogin.getSumLoginCount();

            if (isExpired) {
                loginProvider.deleteMobileLogin(getEgn(context));
                if (falseCredentialDetect) {
                    MobileLoginRepresentation mobileLoginRepresentation = MobileLoginRepresentation.forMobileLogin(//
                            getEgn(context), //
                            getGrantType(context), //
                            getClientId(context), //
                            getScope(context) //
                    );
                    loginProvider.persistCode(mobileLoginRepresentation);
                }
            } else {
                int maxLoginCount = loginProvider.getMaxLoginCount();
                if (ongoingSumLoginCount < maxLoginCount) {
                    if (falseCredentialDetect) {
                        int newSumLoginCount = ++ongoingSumLoginCount;
                        loginProvider.changeSumLoginCount(//
                                ongoingMobileLogin.getPersonIdentifier(), //
                                ongoingMobileLogin.getClientId(), //
                                ongoingMobileLogin.getGrantType(), //
                                ongoingMobileLogin.getScope(), //
                                newSumLoginCount //
                        );
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            if (falseCredentialDetect) {
                MobileLoginRepresentation mobileLoginRepresentation = MobileLoginRepresentation.forMobileLogin(//
                        getEgn(context), //
                        getGrantType(context), //
                        getClientId(context), //
                        getScope(context) //
                );
                mobileLoginRepresentation.setSumLoginCount(1);
                loginProvider.persistCode(mobileLoginRepresentation);
            }
        }
        return true;
    }

    private static void updateFcm(String fcm, UserModel user) {
        if (!Strings.isNullOrEmpty(fcm)) {
            user.setSingleAttribute(SpiConstants.FCM, fcm);
        }
    }

    private boolean isUserVerified(UserModel user) {
        String verifiedAttribute = getVerifiedAttribute(user);
        return Boolean.TRUE.toString().equalsIgnoreCase(verifiedAttribute);
    }

    private String getVerifiedAttribute(UserModel user) {
        return user.getFirstAttribute(SpiConstants.VERIFIED);
    }

    private boolean isUserDataCorrect(UserModel user, String pin, String personIdentifier) {
        return pin.equals(user.getFirstAttribute(SpiConstants.PIN)) //
                && personIdentifier.equals(user.getFirstAttribute(SpiConstants.PERSON_IDENTIFIER) //
        );
    }
}
