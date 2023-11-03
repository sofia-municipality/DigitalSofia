package com.bulpros.integrations.camel.routes.agentWS;

import com.bulpros.integrations.agentWS.model.ObligationsModelResponse;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GetObligationsRoute extends RouteBuilder {

    public void configure() {
        onException(Exception.class)
                .handled(true)
                .bean(EFormsIntegrationsErrorHandler.class);

        restConfiguration().component("servlet")
                .clientRequestValidation(true)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*");
        rest("/AgentWS/obligations")
                .get()
                .param().name("idn").type(RestParamType.query)
                    .description("Legal person eik")
                    .dataType("string").required(true).endParam()
                .param().name("limit").type(RestParamType.query)
                    .description("Requested limit ")
                    .dataType("int").required(false).endParam()
                .outType(ObligationsModelResponse.class)
                .route().routeGroup("AGENTWS").routeId("AgentWSObligations")
                .to("bean:AgentWSService?method=getObligations(${header.idn}, ${header.limit})");
    }
}
