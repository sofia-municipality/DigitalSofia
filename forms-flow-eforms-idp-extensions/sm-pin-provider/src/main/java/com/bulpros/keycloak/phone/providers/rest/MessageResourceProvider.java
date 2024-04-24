package com.bulpros.keycloak.phone.providers.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class MessageResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    MessageResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new SmsResource(session);
    }

    @Override
    public void close() {
    }
}