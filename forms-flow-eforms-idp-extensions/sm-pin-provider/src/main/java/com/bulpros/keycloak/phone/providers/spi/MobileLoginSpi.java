package com.bulpros.keycloak.phone.providers.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class MobileLoginSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "mobileLogin";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return MobileLoginProvider.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return MobileLoginProviderFactory.class;
    }
}