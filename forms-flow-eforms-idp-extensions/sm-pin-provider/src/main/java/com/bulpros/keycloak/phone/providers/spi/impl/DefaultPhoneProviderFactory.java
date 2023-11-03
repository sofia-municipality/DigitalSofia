package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.providers.spi.PinProvider;
import com.bulpros.keycloak.phone.providers.spi.PinProviderFactory;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultPhoneProviderFactory implements PinProviderFactory {

    private Scope config;

    @Override
    public PinProvider create(KeycloakSession session) {
        return new DefaultPhoneProvider(session, config);
    }

    @Override
    public void init(Scope config) {
        this.config = config;
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
