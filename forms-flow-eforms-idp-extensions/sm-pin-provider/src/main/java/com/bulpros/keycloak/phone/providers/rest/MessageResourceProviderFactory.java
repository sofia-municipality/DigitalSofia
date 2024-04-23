package com.bulpros.keycloak.phone.providers.rest;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class MessageResourceProviderFactory implements RealmResourceProviderFactory {

    private static final Logger logger = Logger.getLogger(MessageResourceProviderFactory.class);

    @Override
    public String getId() {
        return "message";
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new MessageResourceProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}