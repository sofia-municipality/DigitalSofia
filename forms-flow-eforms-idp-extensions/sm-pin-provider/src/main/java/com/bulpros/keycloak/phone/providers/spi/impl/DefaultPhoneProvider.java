package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.common.OptionalUtils;
import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.model.CheckForGeneratedCodeResponse;
import com.bulpros.keycloak.phone.providers.model.CodeStatusEnum;
import com.bulpros.keycloak.phone.providers.model.ConfirmStatusResponse;
import com.bulpros.keycloak.phone.providers.model.SendTokenResponse;
import com.bulpros.keycloak.phone.providers.model.UpdateCodeStatusResponse;
import com.bulpros.keycloak.phone.providers.representations.TokenCodeRepresentation;
import com.bulpros.keycloak.phone.providers.spi.MessageSenderService;
import com.bulpros.keycloak.phone.providers.spi.PinProvider;
import com.bulpros.keycloak.phone.providers.spi.PinVerificationCodeProvider;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.ServiceUnavailableException;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.validation.Validation;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class DefaultPhoneProvider implements PinProvider {

    private static final Logger logger = Logger.getLogger(DefaultPhoneProvider.class);
    private final KeycloakSession session;
    private final String service;
    private final int tokenExpiresIn;
    private final int targetHourMaximum;
    private final int sourceHourMaximum;

    private final Scope config;

    DefaultPhoneProvider(KeycloakSession session, Scope config) {
        this.session = session;
        this.config = config;

        this.service = session.listProviderIds(MessageSenderService.class).stream()
                .filter(s -> s.equals(config.get("service"))).findFirst()
                .orElse(session.listProviderIds(MessageSenderService.class).stream().findFirst().orElse(null));

        if (Validation.isBlank(this.service)) {
            logger.error("Message sender service provider not found!");
        }

        if (Validation.isBlank(config.get("service")))
            logger.warn(
                    "No message sender service provider specified! Default provider'" + this.service + "' will be used. You can use keycloak start param '--spi-phone-default-service' to specify a different one. ");

        this.tokenExpiresIn = config.getInt("tokenExpiresIn", 7200);
        this.targetHourMaximum = config.getInt("targetHourMaximum", 3);
        this.sourceHourMaximum = config.getInt("sourceHourMaximum", 10);
    }

    @Override
    public void close() {
    }

    private PinVerificationCodeProvider getTokenCodeService() {
        return session.getProvider(PinVerificationCodeProvider.class);
    }

    private String getRealmName() {
        return session.getContext().getRealm().getName();
    }

    private Optional<String> getStringConfigValue(String configName) {
        return OptionalUtils.ofBlank(
                OptionalUtils.ofBlank(config.get(getRealmName() + "-" + configName)).orElse(config.get(configName)));
    }

    private boolean getBooleanConfigValue(String configName, boolean defaultValue) {
        Boolean result = config.getBoolean(getRealmName() + "-" + configName, null);
        if (result == null) {
            result = config.getBoolean(configName, defaultValue);
        }
        return result;
    }

    @Override
    public boolean isDuplicatePhoneAllowed() {
        return getBooleanConfigValue("duplicate-phone", false);
    }

    @Override
    public boolean validPhoneNumber() {
        return getBooleanConfigValue("valid-phone", true);
    }

    @Override
    public boolean compatibleMode() {
        return getBooleanConfigValue("compatible", false);
    }

    @Override
    public int otpExpires() {
        return getStringConfigValue("otp-expires").map(Integer::valueOf).orElse(60 * 60);
    }

    @Override
    public Optional<String> canonicalizePhoneNumber() {
        return getStringConfigValue("canonicalize-phone-numbers");
    }

    @Override
    public Optional<String> defaultPhoneRegion() {
        return getStringConfigValue("phone-default-region");
    }

    @Override
    public Optional<String> phoneNumberRegex() {
        return getStringConfigValue("number-regex");
    }

    @Override
    public int sendTokenCode(String phoneNumber, String sourceAddr, TokenCodeType type, String kind) {
        return 0;
    }

    @Override
    public SendTokenResponse sendTokenCode(String personIdentifier, String fcm, String sourceAddr, TokenCodeType type) {

        //        TODO: Fix isAbusing logic
        //        if (getTokenCodeService().isAbusing(pin, type, sourceAddr, sourceHourMaximum, targetHourMaximum)) {
        //            throw new ForbiddenException("You requested the maximum number of messages the last hour");
        //        }

        TokenCodeRepresentation ongoing = getTokenCodeService().ongoingProcess(personIdentifier, type);
        SendTokenResponse sendTokenResponse = new SendTokenResponse();

        if (ongoing != null) {
            boolean isExpired = ongoing.getExpiresAt().before(new Date());

            if (!isExpired && ongoing.getStatus().equals(CodeStatusEnum.WAITING)) {
                sendTokenResponse.setExpiresIn((ongoing.getExpiresAt().getTime() - new Date().getTime()) / 1000);
                sendTokenResponse.setCodeAlreadySent(true);
                return sendTokenResponse;
            } else {
                getTokenCodeService().deleteUserProcess(personIdentifier, type);
            }
        }

        TokenCodeRepresentation tokenCode;
        try {
            tokenCode = TokenCodeRepresentation.forPersonIdentifier(personIdentifier);
        } catch (NoSuchAlgorithmException e) {
            throw new ForbiddenException("Could not create a code for the user!");
        }

        try {
            try {
                HashMap<String, String> message = new HashMap<>();
                message.put("code", tokenCode.getCode());
                message.put("expiresAt", String.valueOf(tokenExpiresIn));
                session.getProvider(MessageSenderService.class, service).sendCompleteMessage(fcm, message);
            } catch (Exception e) {
                logger.error("Could not send message!");
            }

            getTokenCodeService().persistCode(tokenCode, type, tokenExpiresIn);

            TokenCodeRepresentation process = getTokenCodeService().ongoingProcess(personIdentifier, type);

            sendTokenResponse.setExpiresIn(
                    (process.getExpiresAt().getTime() - process.getCreatedAt().getTime()) / 1000);
            sendTokenResponse.setCodeAlreadySent(false);
            return sendTokenResponse;
        } catch (Exception e) {
            throw new ServiceUnavailableException("Internal server error");
        }
    }

    @Override
    public CheckForGeneratedCodeResponse checkForGeneratedCode(String personIdentifier, TokenCodeType type) {
        // TODO: Add isAbusing logic
        TokenCodeRepresentation ongoing = getTokenCodeService().ongoingProcess(personIdentifier, type);
        CheckForGeneratedCodeResponse response = new CheckForGeneratedCodeResponse(false, null, 0);
        if (ongoing == null || !CodeStatusEnum.WAITING.equals(ongoing.getStatus())) {
            return response;
        }

        long expiresIn = (ongoing.getExpiresAt().getTime() - (Instant.now().toEpochMilli())) / 1000;

        if (expiresIn > 0) {
            response.setCodeExists(true);
            response.setCode(ongoing.getCode());
            response.setExpiresIn(expiresIn);
        }
        return response;
    }

    @Override
    public UpdateCodeStatusResponse updateCodeStatus(String personIdentifier, String code, CodeStatusEnum status,
            TokenCodeType type) {
        TokenCodeRepresentation ongoing = getTokenCodeService().ongoingProcess(personIdentifier, type);

        if (ongoing == null || ongoing.getStatus().equals(CodeStatusEnum.CONFIRMED) || ongoing.getStatus()
                .equals(CodeStatusEnum.CANCELLED) || !ongoing.getCode().equals(code)) {
            return new UpdateCodeStatusResponse(false);
        }

        long expiresIn = (ongoing.getExpiresAt().getTime() - (Instant.now().toEpochMilli())) / 1000;
        boolean expireAtIfExpired = expiresIn > tokenExpiresIn;

        if (expireAtIfExpired) {
            return new UpdateCodeStatusResponse(false);
        } else {
            try {
                getTokenCodeService().changeStatusInProcess(personIdentifier, code, type, status);
                return new UpdateCodeStatusResponse(true);
            } catch (Exception e) {
                throw new ServiceUnavailableException("Internal server error");
            }
        }
    }

    @Override
    public ConfirmStatusResponse checkConfirmStatus(String personIdentifier, String code, TokenCodeType type) {
        TokenCodeRepresentation codeConfirm = getTokenCodeService().getStatus(//
                personIdentifier, //
                code, //
                type //
        );

        if (codeConfirm == null) {
            return new ConfirmStatusResponse(CodeStatusEnum.WAITING);

        }
        return new ConfirmStatusResponse(codeConfirm.getStatus());
    }
}