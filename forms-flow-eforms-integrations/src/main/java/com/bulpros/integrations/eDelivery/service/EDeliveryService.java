package com.bulpros.integrations.eDelivery.service;

import com.bulpros.integrations.eDelivery.model.SearchProfileResponse;
import com.bulpros.integrations.eDelivery.model.SendMessageOnBehalfOfRequest;
import com.bulpros.integrations.eDelivery.model.SendMessageOnBehalfOfResponse;
import com.bulpros.integrations.eDelivery.model.UploadFileOnBehalfOfResponse;
import com.bulpros.integrations.esb.tokenManager.EsbTokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

@Component("eDeliveryService")
@Scope(value = "prototype")
@RequiredArgsConstructor
@Slf4j
public class EDeliveryService {

    private final EsbTokenManager tokenManager;
    private final RestTemplate restTemplateEsb;

    @Value("${edelivery.registration.name}")
    private String eDeiveryClientRegistrationName;
    @Value("${spring.security.oauth2.client.registration.edelivery.client-id}")
    private String eDeliveryClientId;
    @Value("${spring.security.oauth2.client.registration.edelivery.scope}")
    private String eDeliveryScope;

    @Value("${com.bulpros.eDelivery.search.profile.url}")
    private String searchProfileUrl;

    @Value("${com.bulpros.eDelivery.send.message.on-behalf-of.url}")
    private String sendMessageOnBehalfOfUrl;


    @Value("${com.bulpros.eDelivery.upload.obo.blobs.url}")
    private String uploadOnBehalfOfBlobUrl;

    public SearchProfileResponse searchUserProfile(String identifier, String targetGroup) {
        HashMap<String, String> customHeaders = new HashMap<>();
        HttpHeaders headers = prepareHeaderWithBearerToken(customHeaders);

        UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(searchProfileUrl)
                .queryParam("identifier", identifier)
                .queryParam("targetGroupId", targetGroup);
        ResponseEntity<SearchProfileResponse> response =
                restTemplateEsb.exchange(request.toUriString(), HttpMethod.GET,
                        new HttpEntity<>(headers), SearchProfileResponse.class);

        return response.getBody();
    }

    public UploadFileOnBehalfOfResponse uploadFileOnBehalfOf(Exchange exchange) {

        Message message = exchange.getIn();
        String fileName = "";
        DataHandler dh = null;
        try {
            ByteArrayInputStream fileByteStream = message.getBody(ByteArrayInputStream.class);
            MimeBodyPart mimeMessage = new MimeBodyPart(fileByteStream);
            dh = mimeMessage.getDataHandler();
            fileName = dh.getName();
        } catch (MessagingException e){
            log.error("Could not extract file name from request!");
        }
        String representedPersonId = message.getHeader("representedPersonId").toString();
        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("representedPersonId", representedPersonId);
        HttpHeaders headers = prepareHeaderWithBearerToken(customHeaders);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        byte[] bytes = new byte[0];
        try {
            bytes = ((ByteArrayInputStream) dh.getInputStream()).readAllBytes();
        } catch (IOException e) {
            log.error("Could not read input data!");
        }

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(fileName)
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(bytes, fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<UploadFileOnBehalfOfResponse> responseFileId = restTemplateEsb
                .postForEntity(uploadOnBehalfOfBlobUrl, requestEntity, UploadFileOnBehalfOfResponse.class);
        return responseFileId.getBody();
    }


    public SendMessageOnBehalfOfResponse sendMessageOnBehalfOf(SendMessageOnBehalfOfRequest sendMessageOnBehalfOfRequest) {
        HashMap<String, String> customHeaders = new HashMap<>();
        String representedPersonId = sendMessageOnBehalfOfRequest.getSenderProfileId();
        customHeaders.put("representedPersonId",  representedPersonId);
        HttpHeaders headers = prepareHeaderWithBearerToken(customHeaders);
        headers.add("content-type", "application/json");

        UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(sendMessageOnBehalfOfUrl);
        HttpEntity<SendMessageOnBehalfOfRequest> body = new HttpEntity<>(sendMessageOnBehalfOfRequest, headers);
        ResponseEntity<String> response =
                restTemplateEsb.exchange(request.toUriString(), HttpMethod.POST, body, String.class);

        return new SendMessageOnBehalfOfResponse(Integer.parseInt(response.getBody()));
    }

    private HttpHeaders prepareHeaderWithBearerToken(HashMap<String, String> customHeaders) {
        String bearerToken = tokenManager.getAccessToken(eDeiveryClientRegistrationName, eDeliveryClientId, eDeliveryScope, customHeaders);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return headers;
    }
}


