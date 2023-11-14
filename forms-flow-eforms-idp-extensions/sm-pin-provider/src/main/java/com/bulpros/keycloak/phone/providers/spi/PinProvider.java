package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
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



}
