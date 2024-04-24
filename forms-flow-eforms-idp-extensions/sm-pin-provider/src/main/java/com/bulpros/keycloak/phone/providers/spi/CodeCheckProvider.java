package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.exception.CustomProviderException;
import com.bulpros.keycloak.phone.providers.model.ConfirmStatusResponse;
import org.keycloak.provider.Provider;

public interface CodeCheckProvider extends Provider {
    ConfirmStatusResponse checkConfirmStatus(String personIdentifier, String code) throws CustomProviderException;
}
