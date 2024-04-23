package com.bulpros.integrations.camel.routes.evrotrust;


import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveryDocumentRoute extends RouteBuilder {

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .bean(EFormsIntegrationsErrorHandler.class);

        from("servlet:/evrotrust/document/delivery?httpMethodRestrict=POST")
                .routeGroup("EVROTRUST").routeId("EvrotrustDataStatus")
                .process(exchange -> {
                    exchange.getMessage().setHeader("Access-Control-Allow-Origin", "*");
                    exchange.getMessage().setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
                    exchange.getMessage().setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
                })
                .to("bean:EvrotrustService?method=deliverDocument")
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));
    }
}
