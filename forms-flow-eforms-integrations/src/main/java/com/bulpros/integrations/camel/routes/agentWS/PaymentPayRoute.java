package com.bulpros.integrations.camel.routes.agentWS;

import com.bulpros.integrations.agentWS.model.PayModelRequest;
import com.bulpros.integrations.agentWS.model.PayModelResponse;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentPayRoute extends RouteBuilder {

    public void configure() {
        onException(Exception.class)
                .handled(true)
                .bean(EFormsIntegrationsErrorHandler.class);

        restConfiguration().component("servlet")
                .clientRequestValidation(true)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*");
        rest("/AgentWS/payment/pay")
                .post()
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .type(PayModelRequest.class)
                .outType(PayModelResponse.class)
                .route().routeGroup("AGENTWS").routeId("AgentWSPay")
                .to("bean:AgentWSService?method=payObligations(${body})");
    }
}
