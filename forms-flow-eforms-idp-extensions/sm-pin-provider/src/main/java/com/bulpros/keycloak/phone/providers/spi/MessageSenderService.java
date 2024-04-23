package com.bulpros.keycloak.phone.providers.spi;

import com.bulpros.keycloak.phone.providers.exception.MessageSendException;
import org.keycloak.provider.Provider;

import java.util.HashMap;

/**
 * Message
 */
public interface MessageSenderService extends Provider {
    
    void sendCompleteMessage(String fcm, HashMap<String, String> message) throws MessageSendException;
}
