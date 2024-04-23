package com.bulpros.keycloak.phone.providers.jpa;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MobileLoginJpaEntityProviderFactory implements JpaEntityProviderFactory {

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        return new MobileLoginJpaEntityProvider();
    }

    @Override
    public String getId() {
        return "mobileLoginEntityProvider";
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}