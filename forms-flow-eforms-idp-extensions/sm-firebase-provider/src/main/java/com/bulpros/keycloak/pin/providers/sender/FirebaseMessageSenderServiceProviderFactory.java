package com.bulpros.keycloak.pin.providers.sender;

import com.bulpros.keycloak.phone.providers.spi.MessageSenderService;
import com.bulpros.keycloak.phone.providers.spi.MessageSenderServiceProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class FirebaseMessageSenderServiceProviderFactory implements MessageSenderServiceProviderFactory {
    private Config.Scope config;

    @Override
    public MessageSenderService create(KeycloakSession keycloakSession) {
        return new FirebaseMessageSenderService(keycloakSession.getContext().getRealm().getDisplayName(), config);
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
        return "firebaseSender";
    }
}
