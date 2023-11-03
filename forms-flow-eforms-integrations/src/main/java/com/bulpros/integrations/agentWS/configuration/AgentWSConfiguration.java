package com.bulpros.integrations.agentWS.configuration;

import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

@Configuration
@Getter
public class AgentWSConfiguration {

    @Value("${com.bulpros.agentWS.keyStore.file}")
    private FileSystemResource agentWSKeystoreFile;

    @Value("${com.bulpros.agentWS.keyStore.password}")
    private String agentWSKeystorePassword;

    @Value("${com.bulpros.agentWS.keyStore.type}")
    private String agentWSKeystoreType;

    @Value("${com.bulpros.agentWS.retryMaxSeconds}")
    private Integer agentWSMaxSeconds;

    @Value("${com.bulpros.agentWS.retryWaitSeconds}")
    private Integer agentWSWaitSeconds;

    @Value("${com.bulpros.agentWS.hostname.verification.on}")
    private Boolean hostnameVerificationOn;

    @Bean
    public RestTemplate restTemplateAgentWS() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(agentWSKeystoreType);
        keyStore.load(agentWSKeystoreFile.getInputStream(), agentWSKeystorePassword.toCharArray());

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(keyStore, acceptingTrustStrategy)
                .loadKeyMaterial(keyStore, agentWSKeystorePassword.toCharArray())
                .build();

        SSLConnectionSocketFactory socketFactory;
        if (hostnameVerificationOn) {
            socketFactory = new SSLConnectionSocketFactory(sslContext);
        }else{
            socketFactory = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE); // <- this turns hostname verification off, not for prod
        }

        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
