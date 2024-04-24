package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.providers.spi.CodeCheckProvider;
import com.bulpros.keycloak.phone.providers.spi.CodeCheckProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class StatusCodeCheckProviderFactory implements CodeCheckProviderFactory {

    private Config.Scope config;

    @Override
    public CodeCheckProvider create(KeycloakSession session) {
        return new StatusCodeCheckProvider(session, config);
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
        return "confirmCode";
    }
}
