package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.common.CommonUtils;
import com.bulpros.keycloak.phone.providers.model.ConfirmStatusResponse;
import com.bulpros.keycloak.phone.providers.model.GetStatusModel;
import com.bulpros.keycloak.phone.providers.spi.CodeCheckProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StatusCodeCheckProvider implements CodeCheckProvider {
    private static final Logger logger = Logger.getLogger(StatusCodeCheckProvider.class);
    private final KeycloakSession session;

    private final Scope config;
    private final String keycloakHomeUrl;
    private final String checkConfirmStatusPath;
    private final HttpClient httpClient;

    StatusCodeCheckProvider(KeycloakSession session, Scope config) {
        this.session = session;
        this.config = config;
        this.keycloakHomeUrl = config.get("keycloakHomeUrl", "http://localhost:8080/auth");
        this.checkConfirmStatusPath = config.get("checkConfirmStatusPath",
                "/realms/eServices/message/authentication-code/check-confirm-status");
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
        httpClient = HttpClient.newBuilder().build();
    }

    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public void close() {
    }

    @Override
    public ConfirmStatusResponse checkConfirmStatus(String personIdentifier, String code) {
        GetStatusModel getStatusModel = new GetStatusModel(personIdentifier, code);
        String body;
        HttpResponse<String> response;
        try {
            body = CommonUtils.ObjMapper().writeValueAsString(getStatusModel);
            HttpRequest request = HttpRequest.newBuilder() //
                    .header("content-type", "application/json") //
                    .uri(new URI(keycloakHomeUrl + checkConfirmStatusPath)) //
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() == 200) {
            String responseBody = response.body();
            try {
                return CommonUtils.ObjMapper().readValue(responseBody, ConfirmStatusResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
