package com.bulpros.auditlog.repository.client;

import com.bulpros.auditlog.config.AuditlogConfigurationProperties;
import com.bulpros.auditlog.exception.AuditlogException;
import com.bulpros.auditlog.model.AuditlogRequest;
import com.bulpros.auditlog.model.AuditlogResponse;
import com.bulpros.auditlog.model.EventTypeEnum;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Date;


public class AuditlogClient {
    private RestTemplate restTemplate;
    private URI url;
    private AuditlogRequest request;
    private AuditlogConfigurationProperties configurationProperties = new AuditlogConfigurationProperties();

    private AuditlogClient(URI url){
        this.url = url;
    }

    public static class Builder {
        private RestTemplate restTemplate;
        private URI url;
        private AuditlogRequest request;

        public Builder(URI url) {
            this.restTemplate = new RestTemplate();
            this.request = new AuditlogRequest();
            this.url = url;
        }

        public Builder eventTime(Date time) {
            this.request.setEventTime(time);
            return this;
        }

        public Builder eventType(EventTypeEnum eventType) {
            this.request.setEventType(eventType);
            return this;
        }

        public Builder eventDescription(String eventDescription) {
            this.request.setEventDescription(eventDescription);
            return this;
        }

        public Builder authnReqID(String authnReqID) {
            this.request.setAuthnReqID(authnReqID);
            return this;
        }

        public Builder authnRespID(String authnRespID) {
            this.request.setAuthnRespID(authnRespID);
            return this;
        }

        public Builder documentRegId(String documentRegId) {
            this.request.setDocumentRegId(documentRegId);
            return this;
        }

        public Builder serviceOID(String serviceOID) {
            this.request.getService().setServiceOID(serviceOID);
            return this;
        }

        public Builder spName(String spName) {
            this.request.getService().setSpName(spName);
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.request.getService().setServiceName(serviceName);
            return this;
        }

        public Builder authnRequesterOID(String authnRequesterOID) {
            this.request.getService().setAuthnRequesterOID(authnRequesterOID);
            return this;
        }

        public Builder adminiOID(String adminiOID) {
            this.request.getService().setAdminiOID(adminiOID);
            return this;
        }

        public Builder adminLegalName(String adminLegalName) {
            this.request.getService().setAdminLegalName(adminLegalName);
            return this;
        }

        public AuditlogClient build() {
            AuditlogClient auditlogClient = new AuditlogClient(this.url);
            auditlogClient.url = this.url;
            auditlogClient.restTemplate = this.restTemplate;
            auditlogClient.request = this.request;
            return  auditlogClient;
        }
    }

    public AuditlogResponse getResponse(){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json");
            HttpEntity<AuditlogRequest> entity = new HttpEntity<>(this.request, httpHeaders);
            var prefix = configurationProperties.getAuditlogPreffix();
            if(prefix == null) prefix="/auditlog";
            return this.restTemplate
                    .postForObject(UriComponentsBuilder.fromHttpUrl(this.url.toString())
                    .path(prefix)
                    .path("/register-event").toUriString(), entity, AuditlogResponse.class);
        }catch (Exception exception) {
            throw new AuditlogException("Could not log data in auditlog. Reason: " + exception.getMessage());
        }
    }
}
