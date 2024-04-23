package com.bulpros.keycloak.phone.providers.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class CodeStatusCheckSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "codeCheck";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return CodeCheckProvider.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return CodeCheckProviderFactory.class;
    }
}