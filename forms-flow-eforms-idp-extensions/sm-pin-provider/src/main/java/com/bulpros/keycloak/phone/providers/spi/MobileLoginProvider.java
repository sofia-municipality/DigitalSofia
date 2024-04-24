package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.representations.MobileLoginRepresentation;
import org.keycloak.provider.Provider;

public interface MobileLoginProvider extends Provider {

    MobileLoginRepresentation ongoingMobileLogin(String personIdentifier, String clientId, String grantType,
            String scope);

    void changeSumLoginCount(String personIdentifier, String clientId, String grantType, String scope,
            int sumLoginCount);

    int deleteMobileLogin(String personIdentifier);

    void persistCode(MobileLoginRepresentation mobileLogin);

    int getMaxLoginCount();

    int getLoginExpiresIn();
}