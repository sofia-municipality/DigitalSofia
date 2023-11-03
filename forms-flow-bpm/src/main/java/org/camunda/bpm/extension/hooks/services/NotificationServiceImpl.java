package org.camunda.bpm.extension.hooks.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.extension.commons.connector.HTTPServiceInvoker;
import org.camunda.bpm.extension.hooks.exceptions.ApplicationServiceException;
import org.camunda.bpm.extension.hooks.exceptions.NotificationServiceException;
import org.camunda.bpm.extension.hooks.services.model.NotificationMailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    @Resource(name = "bpmObjectMapper")
    private ObjectMapper bpmObjectMapper;

    @Autowired
    private WebClient unauthenticatedWebClient;

    @Value("${formsflow.ai.formio.url}")
    private String formsflowFormioUrl;

    @Value("${integrations-service.url}")
    private String integrationsUrl;

    @Value("${integrations-service.notification.prefix}")
    private String notificationPrefix;

    private final TemplateEngine templateEngine;
    private final HTTPServiceInvoker httpServiceInvoker;

    @Override
    public void sendMailNotification(String formAlias, String email, String attachmentUrl, Map<String, Object> variables) {
        ResponseEntity<String> response = httpServiceInvoker.execute(formsflowFormioUrl + "/" + formAlias, HttpMethod.GET, null);
        String responseString = response.getBody();
        String mailTemplate = "";
        String subjectTemplate = "";

        if (StringUtils.isNotEmpty(responseString)) {
            try {
                JsonNode node = bpmObjectMapper.readTree(responseString);
                JsonNode components = node.get("components");
                Iterator<JsonNode> elements = components.elements();

                while (elements.hasNext()) {
                    JsonNode element = elements.next();
                    if (element.has("key") && element.has("html")) {
                        if (element.get("key").textValue().equals("body")) {
                            mailTemplate = element.get("html").textValue();
                        } else if (element.get("key").textValue().equals("subject")) {
                            subjectTemplate = element.get("html").textValue();
                        }
                    }
                }
            } catch (JsonProcessingException e) {
                throw new NotificationServiceException("Notification Service could not parse form template" +
                        "to JSON object! Message: " + e.getMessage());
            }
        }

        try {
            final org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariables(variables);
            final String mailContent = templateEngine.process(mailTemplate, context);
            final String subjectContent = templateEngine.process(subjectTemplate, context);
            var requestBody = new NotificationMailRequest();
            requestBody.setTo(email);
            requestBody.setSubject(subjectContent);
            requestBody.setBody(mailContent);
            if (!attachmentUrl.isEmpty()) {
                requestBody.setAttachment(getAttachmentInformation(attachmentUrl));
            }
            Mono<ResponseEntity<String>> entityMono = unauthenticatedWebClient.post()
                    .uri(UriComponentsBuilder.fromHttpUrl(this.integrationsUrl + this.notificationPrefix)
                            .path("/post-mail-notification").toUriString())
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError,
                            mailResponse -> Mono.error(new ApplicationServiceException(mailResponse.toString())))
                    .toEntity(String.class);
            entityMono.block();
        } catch (Exception exception) {
            throw new NotificationServiceException("Notification Service could not send email! Message: " + exception.getMessage());
        }
    }

    private NotificationMailRequest.Attachment getAttachmentInformation(String url) throws JsonProcessingException {
        NotificationMailRequest.Attachment attachment = new NotificationMailRequest.Attachment();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Mono<ResponseEntity<String>> entityMono = unauthenticatedWebClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + ((JwtAuthenticationToken) authentication).getToken().getTokenValue())
                .retrieve()
                .toEntity(String.class);
        ResponseEntity<String> response = entityMono.block();

        ObjectMapper mapper = new ObjectMapper();
        if (response.getBody() != null) {
            JsonNode file = mapper.readTree(response.getBody()).get("data").get("file");
            attachment.setName(file.get(0).get("originalName").asText());
            attachment.setContentType(file.get(0).get("type").asText());
            attachment.setContent(splitAttachmentContentString(file.get(0).get("url").asText()));
        }
        return attachment;
    }

    private String splitAttachmentContentString(String url) {
        // The incomming url should look something like this, but longer
        // "data:application/pdf;base64,JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PAovVHlwZSAvUGFnZXMKL0NvdW50IDEKL0tpZHMgWyA0IDAgUiBdCj4"
        // There is only one comma, therefore we split the string by it and get the second part - the base64 content
        String[] content = url.split(";base64,");
        return content[1];
    }
}
