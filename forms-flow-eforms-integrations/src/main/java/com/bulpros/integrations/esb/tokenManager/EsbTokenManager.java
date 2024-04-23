package com.bulpros.integrations.esb.tokenManager;

import com.bulpros.integrations.configuration.esb.EsbAuthorizedClientManagerConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EsbTokenManager {

    @Value("${com.bulpros.auditlog.eforms.oid}")
    private String serviceOID;
    private final RestTemplate restTemplateEsbToken;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    public String getAccessToken(String clientRegistrationName, String clientId, String scope, HashMap<String,String> headers) {
        EsbAuthorizedClientManagerConfiguration esbAuthorizedClientManager = new EsbAuthorizedClientManagerConfiguration();
        OAuth2AuthorizedClientManager authorizedClientManager =
                esbAuthorizedClientManager.authorizedClientManager(clientRegistrationRepository, authorizedClientRepository,
                        restTemplateEsbToken, headers);

        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationName)
                        .principal(clientId)
                        .attribute("scope", scope)
                        .build();

        OAuth2AuthorizedClient authorizedClient =
                authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = Objects.requireNonNull(authorizedClient).getAccessToken();

        return accessToken.getTokenValue();
    }

}
