package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.exception.MessageSendException;

import java.util.HashMap;

public abstract class FullMessageSenderAbstractService implements MessageSenderService {

    private final String realmDisplay;

    public FullMessageSenderAbstractService(String realmDisplay) {
        this.realmDisplay = realmDisplay;
    }

    public abstract void sendMessage(String phoneNumber, HashMap<String, String> message) throws MessageSendException;

    @Override
    public void sendCompleteMessage(String fcm, HashMap<String, String> message) throws MessageSendException {
        sendMessage(fcm, message);
    }
}
