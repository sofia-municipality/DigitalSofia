package com.bulpros.keycloak.pin.providers.sender;

import com.bulpros.keycloak.configuration.FirebaseService;
import com.bulpros.keycloak.phone.providers.exception.MessageSendException;
import com.bulpros.keycloak.phone.providers.spi.FullMessageSenderAbstractService;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.jboss.logging.Logger;
import org.keycloak.Config;

import java.util.HashMap;

public class FirebaseMessageSenderService extends FullMessageSenderAbstractService {
    private final Config.Scope config;
    private final String accountSettingsFilePath;
    private static final Logger logger = Logger.getLogger(FirebaseMessageSenderService.class);

    public FirebaseMessageSenderService(String realmDisplay, Config.Scope config) {
        super(realmDisplay);
        this.config = config;
        this.accountSettingsFilePath = config.get("accountSettingsFilePath", "");
    }

    @Override
    public void sendMessage(String fcm, HashMap<String, String> message) throws MessageSendException {
        logger.debug(String.format("To: %s >>> %s", fcm, message));

        final Notification notification = Notification.builder().setTitle("Digital Sofia") //
                .setBody("Имате искане за достъп до системата!") //
                .build(); //
        Message.Builder firebaseBuilder = Message.builder();
        firebaseBuilder.setNotification(notification);
        message.forEach(firebaseBuilder::putData);
        Message firebaseMessage = firebaseBuilder.setToken(fcm).build();

        try {
            String response = FirebaseService.getFirebaseService(accountSettingsFilePath).send(firebaseMessage);
            logger.debug("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            throw new MessageSendException(500, "Firebase service could not send message!", e.getMessage());
        }
    }

    @Override
    public void close() {
    }
}
