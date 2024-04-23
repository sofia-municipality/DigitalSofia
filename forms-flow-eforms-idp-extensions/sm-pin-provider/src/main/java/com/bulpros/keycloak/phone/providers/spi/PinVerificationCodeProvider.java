package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.model.CodeStatusEnum;
import com.bulpros.keycloak.phone.providers.representations.TokenCodeRepresentation;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

public interface PinVerificationCodeProvider extends Provider {

    TokenCodeRepresentation ongoingProcess(String personIdentifier, TokenCodeType tokenCodeType);

    void changeStatusInProcess(String personIdentifier, String code, TokenCodeType tokenCodeType,
            CodeStatusEnum status);

    int deleteUserProcess(String personIdentifier, TokenCodeType tokenCodeType);

    TokenCodeRepresentation getStatus(String personIdentifier, String code, TokenCodeType tokenCodeType);

    // TODO: Fix logic
    boolean isAbusing(String personIdentifier, TokenCodeType tokenCodeType, String sourceAddr, int sourceHourMaximum,
            int targetHourMaximum);

    void persistCode(TokenCodeRepresentation tokenCode, TokenCodeType tokenCodeType, int tokenExpiresIn);

    void validateCode(UserModel user, String personIdentifier, String code);

    void validateCode(UserModel user, String personIdentifier, String code, TokenCodeType tokenCodeType);

    void validateProcess(String tokenCodeId, UserModel user);

    //void cleanUpAction(UserModel user, boolean isOTP);

    void tokenValidated(UserModel user, String personIdentifier, String tokenCodeId, boolean isOTP);
}
