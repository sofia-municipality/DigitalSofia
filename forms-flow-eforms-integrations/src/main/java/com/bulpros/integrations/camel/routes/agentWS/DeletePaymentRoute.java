package com.bulpros.integrations.camel.routes.agentWS;

import com.bulpros.integrations.agentWS.model.ReversalModelResponse;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DeletePaymentRoute extends RouteBuilder {

    public void configure() {
        onException(Exception.class)
                .handled(true)
                .bean(EFormsIntegrationsErrorHandler.class);

        restConfiguration().component("servlet")
                .clientRequestValidation(true)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*");
        rest("/AgentWS/reversal")
                .delete()
                .param().name("agtid").type(RestParamType.query)
                    .description("Unique transaction id")
                    .dataType("int").required(false).endParam()
                .param().name("ac").type(RestParamType.query)
                    .description("Authorization Code")
                    .dataType("string").required(false).endParam()
                .param().name("date").type(RestParamType.query)
                    .description("Transaction Date")
                    .dataType("string").dataFormat("date").required(true).endParam()
                .param().name("opid").type(RestParamType.query)
                    .description("Unique operator id")
                    .dataType("string").required(true).endParam()
                .param().name("reason").type(RestParamType.query)
                    .description("Reason for reversal transaction")
                    .dataType("string").required(true).endParam()
            .outType(ReversalModelResponse.class)
                .route().routeGroup("AGENTWS").routeId("AgentWSReversal")
                .to("bean:AgentWSService?method=deletePayment(${header.agtid}, ${header.ac},"
                    + "${header.date}, ${header.opid}, ${header.reason})");
    }
}
