package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.providers.spi.PinVerificationCodeProvider;
import com.bulpros.keycloak.phone.providers.spi.PinVerificationCodeProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultVerificationCodeProviderFactory implements PinVerificationCodeProviderFactory {

    @Override
    public PinVerificationCodeProvider create(KeycloakSession session) {
        return new DefaultPinVerificationCodeProvider(session);
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }
}
