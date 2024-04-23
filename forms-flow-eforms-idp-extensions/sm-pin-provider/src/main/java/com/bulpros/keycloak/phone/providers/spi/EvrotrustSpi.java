package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.authentication.authenticators.directgrant.EvrotrustAuthenticator;
import com.bulpros.keycloak.phone.authentication.authenticators.directgrant.EvrotrustAuthenticatorFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class EvrotrustSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "evrotrustAuth";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return EvrotrustAuthenticator.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return EvrotrustAuthenticatorFactory.class;
    }
}
