package com.bulpros.integrations.configuration.esb;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@RequiredArgsConstructor
public class EsbAuthorizedClientManagerConfiguration {

    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            RestTemplate restTemplateEsbToken,
            HashMap<String, String> headers) {

        DefaultClientCredentialsTokenResponseClient accessTokenResponseClient =
                new DefaultClientCredentialsTokenResponseClient();
        if (restTemplateEsbToken != null) {
            accessTokenResponseClient.setRestOperations(restTemplateEsbToken);
        }
        EsbGrantRequestEntityConverter esbGrantRequestEntityConverter = new EsbGrantRequestEntityConverter();
        esbGrantRequestEntityConverter.setCustomHeaders(headers);
        accessTokenResponseClient.setRequestEntityConverter(esbGrantRequestEntityConverter);

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(builder -> builder.accessTokenResponseClient(accessTokenResponseClient))
                        .build();

        OAuth2AuthorizedClientService service =
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, service);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
