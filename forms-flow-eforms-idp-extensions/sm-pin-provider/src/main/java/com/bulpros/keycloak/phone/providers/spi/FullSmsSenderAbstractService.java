package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.exception.MessageSendException;

public abstract class FullSmsSenderAbstractService implements MessageSenderService{

    private final String realmDisplay;

    public FullSmsSenderAbstractService(String realmDisplay) {
        this.realmDisplay = realmDisplay;
    }

    public abstract void sendMessage(String phoneNumber, String message) throws MessageSendException;


    @Override
    public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code , int expires, String kind) throws MessageSendException{
        //TODO template from keycloak message bundle
        final String MESSAGE = String.format("[%s] - " + type.label + " code: %s, expires: %s minute ",realmDisplay , code, expires / 60);
        sendMessage(phoneNumber,MESSAGE);
    }
}
