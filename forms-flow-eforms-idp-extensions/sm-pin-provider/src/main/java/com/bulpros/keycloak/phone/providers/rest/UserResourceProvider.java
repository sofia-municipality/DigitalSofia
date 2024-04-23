package com.bulpros.keycloak.phone.providers.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class UserResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public UserResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new UserResource(session);
    }

    @Override
    public void close() {
    }
}
