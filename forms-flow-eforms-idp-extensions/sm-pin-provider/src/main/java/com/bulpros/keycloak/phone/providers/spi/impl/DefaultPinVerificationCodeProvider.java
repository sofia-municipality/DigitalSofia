package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.authentication.requiredactions.ConfigSmsOtpRequiredAction;
import com.bulpros.keycloak.phone.authentication.requiredactions.UpdatePhoneNumberRequiredAction;
import com.bulpros.keycloak.phone.credential.PhoneOtpCredentialModel;
import com.bulpros.keycloak.phone.credential.PhoneOtpCredentialProvider;
import com.bulpros.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.jpa.TokenCode;
import com.bulpros.keycloak.phone.providers.model.CodeStatusEnum;
import com.bulpros.keycloak.phone.providers.representations.TokenCodeRepresentation;
import com.bulpros.keycloak.phone.providers.spi.PinVerificationCodeProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TemporalType;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DefaultPinVerificationCodeProvider implements PinVerificationCodeProvider {

    private static final Logger logger = Logger.getLogger(DefaultPinVerificationCodeProvider.class);
    private final KeycloakSession session;

    DefaultPinVerificationCodeProvider(KeycloakSession session) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public TokenCodeRepresentation ongoingProcess(String personIdentifier, TokenCodeType tokenCodeType) {

        try {
            TokenCode entity = getEntityManager().createNamedQuery("ongoingProcess", TokenCode.class) //
                    .setParameter("realmId", getRealm().getId()) //
                    .setParameter("personIdentifier", personIdentifier) //
                    .setParameter("now", new Date(), TemporalType.TIMESTAMP) //
                    .setParameter("type", tokenCodeType.name()) //
                    .getSingleResult();
            TokenCodeRepresentation tokenCodeRepresentation = new TokenCodeRepresentation();

            tokenCodeRepresentation.setId(entity.getId());
            tokenCodeRepresentation.setPersonIdentifier(entity.getPersonIdentifier());
            tokenCodeRepresentation.setCode(entity.getCode());
            tokenCodeRepresentation.setType(entity.getType());
            tokenCodeRepresentation.setCreatedAt(entity.getCreatedAt());
            tokenCodeRepresentation.setExpiresAt(entity.getExpiresAt());
            tokenCodeRepresentation.setStatus(CodeStatusEnum.getByWaitingStatusName(entity.getStatus()));

            return tokenCodeRepresentation;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.error("Ongoing Process exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void changeStatusInProcess(String personIdentifier, String code, TokenCodeType tokenCodeType,
            CodeStatusEnum status) {

        getEntityManager().createNamedQuery("changeStatusInProcess") //
                .setParameter("realmId", getRealm().getId()) //
                .setParameter("personIdentifier", personIdentifier) //
                .setParameter("code", code) //
                .setParameter("type", tokenCodeType.name()) //
                .setParameter("status", status.getStatus()) //
                .executeUpdate();
    }

    @Override
    public int deleteUserProcess(String personIdentifier, TokenCodeType tokenCodeType) {

        return getEntityManager().createNamedQuery("deleteUserProcess") //
                .setParameter("realmId", getRealm().getId()) //
                .setParameter("personIdentifier", personIdentifier) //
                .setParameter("type", tokenCodeType.name()) //
                .executeUpdate();
    }

    @Override
    public TokenCodeRepresentation getStatus(String personIdentifier, String code, TokenCodeType tokenCodeType) {
        try {
            TokenCode entity = getEntityManager().createNamedQuery("getStatus", TokenCode.class) //
                    .setParameter("realmId", getRealm().getId()) //
                    .setParameter("personIdentifier", personIdentifier) //
                    .setParameter("code", code) //
                    .setParameter("type", tokenCodeType.name()) //
                    .getSingleResult();
            TokenCodeRepresentation tokenCodeRepresentation = new TokenCodeRepresentation();

            tokenCodeRepresentation.setId(entity.getId());
            tokenCodeRepresentation.setPersonIdentifier(entity.getPersonIdentifier());
            tokenCodeRepresentation.setCode(entity.getCode());
            tokenCodeRepresentation.setType(entity.getType());
            tokenCodeRepresentation.setCreatedAt(entity.getCreatedAt());
            tokenCodeRepresentation.setExpiresAt(entity.getExpiresAt());
            tokenCodeRepresentation.setStatus(CodeStatusEnum.getByWaitingStatusName(entity.getStatus()));

            return tokenCodeRepresentation;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isAbusing(String pin, TokenCodeType tokenCodeType, String sourceAddr, int sourceHourMaximum,
            int targetHourMaximum) {

        Date oneHourAgo = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));

        if (targetHourMaximum > 0) {
            long targetCount = (getEntityManager().createNamedQuery("processesSinceTarget", Long.class)
                    .setParameter("realmId", getRealm().getId()).setParameter("pin", pin)
                    .setParameter("date", oneHourAgo, TemporalType.TIMESTAMP).setParameter("type", tokenCodeType.name())
                    .getSingleResult());
            if (targetCount > targetHourMaximum)
                return true;
        }

        if (sourceHourMaximum > 0) {
            long sourceCount = (getEntityManager().createNamedQuery("processesSinceSource", Long.class)
                    .setParameter("realmId", getRealm().getId()).setParameter("addr", sourceAddr)
                    .setParameter("date", oneHourAgo, TemporalType.TIMESTAMP).setParameter("type", tokenCodeType.name())
                    .getSingleResult());
            if (sourceCount > sourceHourMaximum)
                return true;
        }

        return false;
    }

    @Override
    public void persistCode(TokenCodeRepresentation tokenCode, TokenCodeType tokenCodeType, int tokenExpiresIn) {

        TokenCode entity = new TokenCode();
        Instant now = Instant.now();

        entity.setId(tokenCode.getId());
        entity.setRealmId(getRealm().getId());
        entity.setPersonIdentifier(tokenCode.getPersonIdentifier());
        entity.setCode(tokenCode.getCode());
        entity.setType(tokenCodeType.name());
        entity.setCreatedAt(Date.from(now));
        entity.setExpiresAt(Date.from(now.plusSeconds(tokenExpiresIn)));
        entity.setStatus(tokenCode.getStatus().getStatus());
        if (session.getContext().getConnection() != null) {
            entity.setIp(session.getContext().getConnection().getRemoteAddr());
            entity.setPort(session.getContext().getConnection().getRemotePort());
            entity.setHost(session.getContext().getConnection().getRemoteHost());
        }

        getEntityManager().persist(entity);
    }

    @Override
    public void validateCode(UserModel user, String phoneNumber, String code) {
        validateCode(user, phoneNumber, code, TokenCodeType.VERIFY);
    }

    @Override
    public void validateCode(UserModel user, String pin, String code, TokenCodeType tokenCodeType) {

        logger.info(String.format("valid %s , phone: %s, code: %s", tokenCodeType, pin, code));

        TokenCodeRepresentation tokenCode = ongoingProcess(pin, tokenCodeType);
        if (tokenCode == null)
            throw new BadRequestException(String.format("There is no valid ongoing %s process", tokenCodeType.label));

        if (!tokenCode.getCode().equals(code))
            throw new ForbiddenException("Code does not match with expected value");

        logger.info(String.format("User %s correctly answered the %s code", user.getId(), tokenCodeType.label));

        tokenValidated(user, pin, tokenCode.getId(), TokenCodeType.OTP.equals(tokenCodeType));

        if (TokenCodeType.OTP.equals(tokenCodeType))
            updateUserOTPCredential(user, pin, tokenCode.getCode());
    }

    @Override
    public void tokenValidated(UserModel user, String phoneNumber, String tokenCodeId, boolean isOTP) {

        boolean updateUserPhoneNumber = !isOTP;
        if (isOTP) {
            updateUserPhoneNumber = PhoneOtpCredentialModel.getSmsOtpCredentialData(user)
                    .map(PhoneOtpCredentialModel.SmsOtpCredentialData::getPhoneNumber).map(pn -> pn.equals(phoneNumber))
                    .orElse(false);

        }

        if (updateUserPhoneNumber) {
            if (!Utils.isDuplicatePhoneAllowed(session)) {
                session.users()
                        .searchForUserByUserAttributeStream(session.getContext().getRealm(), "phoneNumber", phoneNumber)
                        .filter(u -> !u.getId().equals(user.getId())).forEach(u -> {
                            logger.info(
                                    String.format("User %s also has phone number %s. Un-verifying.", u.getId(), phoneNumber));
                            u.setSingleAttribute("phoneNumberVerified", "false");

                            u.addRequiredAction(UpdatePhoneNumberRequiredAction.PROVIDER_ID);

                            //remove otp Credentials
                            u.credentialManager().getStoredCredentialsByTypeStream(PhoneOtpCredentialModel.TYPE).filter(c -> {
                                        try {
                                            PhoneOtpCredentialModel.SmsOtpCredentialData credentialData = JsonSerialization.readValue(
                                                    c.getCredentialData(), PhoneOtpCredentialModel.SmsOtpCredentialData.class);
                                            if (Validation.isBlank(credentialData.getPhoneNumber())) {
                                                return true;
                                            }
                                            return credentialData.getPhoneNumber().equals(user.getFirstAttribute("phoneNumber"));
                                        } catch (IOException e) {
                                            logger.warn("Unknown format Otp Credential", e);
                                            return true;
                                        }
                                    }).map(CredentialModel::getId).toList()
                                    .forEach(id -> u.credentialManager().removeStoredCredentialById(id));
                        });
            }
            user.setSingleAttribute("phoneNumberVerified", "true");
            user.setSingleAttribute("phoneNumber", phoneNumber);

            user.removeRequiredAction(UpdatePhoneNumberRequiredAction.PROVIDER_ID);
        }

        validateProcess(tokenCodeId, user);

    }

    @Override
    public void validateProcess(String tokenCodeId, UserModel user) {
        TokenCode entity = getEntityManager().find(TokenCode.class, tokenCodeId);
        getEntityManager().persist(entity);
    }

    private void updateUserOTPCredential(UserModel user, String phoneNumber, String code) {
        user.removeRequiredAction(ConfigSmsOtpRequiredAction.PROVIDER_ID);
        PhoneOtpCredentialProvider ocp = (PhoneOtpCredentialProvider) session.getProvider(CredentialProvider.class,
                PhoneOtpCredentialProviderFactory.PROVIDER_ID);
        if (ocp.isConfiguredFor(getRealm(), user, PhoneOtpCredentialModel.TYPE)) {
            var credentialData = new PhoneOtpCredentialModel.SmsOtpCredentialData(phoneNumber,
                    Utils.getOtpExpires(session));
            PhoneOtpCredentialModel.updateOtpCredential(user, credentialData, code);
        }
    }

    @Override
    public void close() {
    }
}
