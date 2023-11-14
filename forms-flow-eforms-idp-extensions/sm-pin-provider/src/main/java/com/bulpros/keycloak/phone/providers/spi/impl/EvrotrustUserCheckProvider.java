package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.common.CommonUtils;
import com.bulpros.keycloak.phone.providers.exception.CustomProviderException;
import com.bulpros.keycloak.phone.providers.model.UserCheckModel;
import com.bulpros.keycloak.phone.providers.model.UserExtendedModel;
import com.bulpros.keycloak.phone.providers.spi.UserCheckProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.keycloak.Config.Scope;

public class EvrotrustUserCheckProvider implements UserCheckProvider {
    private static final Logger logger = Logger.getLogger(EvrotrustUserCheckProvider.class);
    private final KeycloakSession session;

    private final Scope config;
    private final HttpClient httpClient;

    private final String integrationsUrl;

    EvrotrustUserCheckProvider(KeycloakSession session,Scope config) {
        this.session = session;
        this.config = config;
        this.integrationsUrl = config.get("integrationsUrl", "");
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
        httpClient = HttpClient.newBuilder().build();
    }

    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public UserExtendedModel getEvrotrustUser(UserCheckModel userCheckModel) throws CustomProviderException {
       try {
            String body = CommonUtils.ObjMapper().writeValueAsString(userCheckModel);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(this.integrationsUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
               String responseBody = response.body();
               return CommonUtils.ObjMapper().readValue(responseBody,UserExtendedModel.class);
            }
            else throw new CustomProviderException("Evrotrust User Check Provider could not read user data! " +
                   "Request status: " + response.statusCode() + response.body());
       } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new CustomProviderException("Evrotrust User Check Provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
    }

}
