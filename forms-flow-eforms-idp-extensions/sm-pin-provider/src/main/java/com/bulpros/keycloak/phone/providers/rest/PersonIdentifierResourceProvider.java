package com.bulpros.keycloak.phone.providers.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class PersonIdentifierResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public PersonIdentifierResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new PersonIdentifierResource(session);
    }

    @Override
    public void close() {
    }
}
