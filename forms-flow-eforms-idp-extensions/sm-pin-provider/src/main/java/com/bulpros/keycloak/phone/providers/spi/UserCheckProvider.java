package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.exception.CustomProviderException;
import com.bulpros.keycloak.phone.providers.model.UserCheckModel;
import com.bulpros.keycloak.phone.providers.model.UserExtendedModel;
import org.keycloak.provider.Provider;

public interface UserCheckProvider extends Provider {
        UserExtendedModel getEvrotrustUser(UserCheckModel userCheckModel) throws CustomProviderException;

}
