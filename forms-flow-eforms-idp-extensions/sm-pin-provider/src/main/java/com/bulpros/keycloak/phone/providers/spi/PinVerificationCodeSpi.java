package com.bulpros.keycloak.phone.providers.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class PinVerificationCodeSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "pinVerificationCode";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return PinVerificationCodeProvider.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return PinVerificationCodeProviderFactory.class;
    }
}