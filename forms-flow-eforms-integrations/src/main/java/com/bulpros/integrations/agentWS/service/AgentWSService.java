package com.bulpros.integrations.agentWS.service;

import com.bulpros.integrations.agentWS.model.*;
import com.bulpros.integrations.exceptions.ParametersException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component("AgentWSService")
@RequiredArgsConstructor
public class AgentWSService {
    private static final  String PAYDOCUMENTS = "paydocuments";
    private static final  String PAYDOCUMENTS_PAIDDEBTS = "paydocuments,paiddebts";
    private final RestTemplate restTemplateAgentWS;
    @Value("${com.bulpros.agentWS.obligations.url}")
    private String obligationsUrl;
    @Value("${com.bulpros.agentWS.payment.pay.url}")
    private String paymentPayUrl;
    @Value("${com.bulpros.agentWS.payment.transactioninfo.url}")
    private String paymentTransactionInfoUrl;
    @Value("${com.bulpros.agentWS.reversal.url}")
    private String reversalUrl;

    public ObligationsModelResponse getObligations(String idn, Integer limit) {
        UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(obligationsUrl)
                .queryParam("idn", idn);
        if(Objects.nonNull(limit)){
            request.queryParam("limit", limit);
        }
        ResponseEntity<ObligationsModelResponse> response =
                restTemplateAgentWS.exchange(request.toUriString(), HttpMethod.GET,
                        null, ObligationsModelResponse.class);
        return response.getBody();
    }

    public PayModelResponse payObligations(PayModelRequest payRequest) {
        UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(paymentPayUrl);
        HttpEntity<PayModelRequest> body = new HttpEntity<>(payRequest, null);

        ResponseEntity<PayModelResponse> response =
                restTemplateAgentWS.exchange(request.toUriString(), HttpMethod.POST,
                        body, PayModelResponse.class);

        return response.getBody();
    }

    public TransactionInfoModelResponse getTransactionInfo(Integer agtid, String ac, String date, String expand) {
        UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(paymentTransactionInfoUrl);
        if(Objects.isNull(agtid) && Objects.isNull(ac)) {
            throw new ParametersException("Field \"agtid\" or \"ac\" is mandatory!");
        }
        if(Objects.nonNull(agtid)) {
            request.queryParam("agtid", agtid);
        }
        if(Objects.nonNull(ac)) {
            request.queryParam("ac", ac);
        }
        request
                .queryParam("agtid", agtid)
                .queryParam("ac", ac)
                .queryParam("date", date);

        if( Objects.nonNull(expand) ) {
            if(!PAYDOCUMENTS.equals(expand.strip()) && !PAYDOCUMENTS_PAIDDEBTS.equals(expand.strip())){
                throw new ParametersException("Possible values about parameter \"expand\" are \"paydocuments\" and \"paydocuments,paiddebts");
            }
            request.queryParam("expand", expand);
        }
        ResponseEntity<TransactionInfoModelResponse> response =
                restTemplateAgentWS.exchange(request.toUriString(), HttpMethod.GET,
                        null, TransactionInfoModelResponse.class);
        return response.getBody();
    }

    public ReversalModelResponse deletePayment(Integer agtid, String ac, String date, String opid, String reason) {

        UriComponentsBuilder request = UriComponentsBuilder.fromHttpUrl(reversalUrl);
        if(Objects.nonNull(agtid)) {
            request.queryParam("agtid", agtid);
        }
        if(Objects.nonNull(ac)) {
            request.queryParam("ac", ac);
        }
        request
                .queryParam("date", date)
                .queryParam("opid", opid)
                .queryParam("reason", reason);
        ResponseEntity<ReversalModelResponse> response =
                restTemplateAgentWS.exchange(request.toUriString(), HttpMethod.DELETE,
                        null, ReversalModelResponse.class);
        return response.getBody();
    }
}
