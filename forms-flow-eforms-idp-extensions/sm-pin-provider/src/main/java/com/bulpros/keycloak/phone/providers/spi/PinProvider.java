package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.model.*;
import org.keycloak.provider.Provider;

import java.util.Optional;

public interface PinProvider extends Provider {

    //TODO on key login support
    //boolean Verification(String phoneNumber, String token);

    boolean isDuplicatePhoneAllowed();

    boolean validPhoneNumber();

    boolean compatibleMode();

    int otpExpires();

    Optional<String> canonicalizePhoneNumber();

    Optional<String> defaultPhoneRegion();

    Optional<String> phoneNumberRegex();

    int sendTokenCode(String phoneNumber, String sourceAddr, TokenCodeType type, String kind);

    SendTokenResponse sendTokenCode(String personIdentifier, String fcm, String sourceAddr, TokenCodeType type);

    UpdateCodeStatusResponse updateCodeStatus(String personIdentifier, String code, CodeStatusEnum status, TokenCodeType type);

    CheckForGeneratedCodeResponse checkForGeneratedCode(String personIdentifier, TokenCodeType type);

    ConfirmStatusResponse checkConfirmStatus(String personIdentifier, String code, TokenCodeType type);
}
