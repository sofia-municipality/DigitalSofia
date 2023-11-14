package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.providers.spi.UserCheckProvider;
import com.bulpros.keycloak.phone.providers.spi.UserCheckProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class EvrotrustUserCheckProviderFactory implements UserCheckProviderFactory {

    private Config.Scope config;
    @Override
    public UserCheckProvider create(KeycloakSession session) {
        return new EvrotrustUserCheckProvider(session, config);
    }

    @Override
    public void init(Config.Scope scope) {
        this.config = scope;
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "evrotrust";
    }
}
