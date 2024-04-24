package com.bulpros.keycloak.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class FirebaseService {
    private static final Logger logger = Logger.getLogger(FirebaseService.class);
    static FirebaseMessaging messaging;

    public static FirebaseMessaging getFirebaseService(String accountSettingsFilePath) {
        if (Objects.nonNull(messaging))
            return messaging;

        FirebaseApp firebaseApp = getFirebaseApp(getGoogleCredentials(accountSettingsFilePath));
        messaging = getFirebaseMessaging(firebaseApp);
        return messaging;
    }

    static GoogleCredentials getGoogleCredentials(String accountSettingsFilePath) {
        try {

            InputStream targetStream = getFileFromResourceAsStream(accountSettingsFilePath);
            return GoogleCredentials.fromStream(targetStream);

        } catch (IOException e) {
            logger.error("Firebase service could not read account service file!!");
        }

        return null;
    }

    private static InputStream getFileFromResourceAsStream(String fileName) throws FileNotFoundException {
        try {
            File file = new File(fileName);
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("Firebase service account file not found! " + fileName);
            throw e;
        }
    }

    private static FirebaseApp getFirebaseApp(GoogleCredentials credentials) {
        FirebaseApp firebaseApp = null;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))
                    firebaseApp = app;
            }
        } else {
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            firebaseApp = FirebaseApp.initializeApp(options);
        }
        return firebaseApp;
    }

    private static FirebaseMessaging getFirebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
