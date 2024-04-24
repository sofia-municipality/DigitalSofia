package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.providers.spi.MobileLoginProvider;
import com.bulpros.keycloak.phone.providers.spi.MobileLoginProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultMobileLoginProviderFactory implements MobileLoginProviderFactory {

    private Config.Scope config;

    @Override
    public MobileLoginProvider create(KeycloakSession session) {
        return new DefaultMobileLoginProvider(session, config);
    }

    @Override
    public void init(Config.Scope config) {
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
        return "mobileLogin";
    }
}
