package com.bulpros.integrations.camel.routes.evrotrust;


import com.bulpros.integrations.evrotrust.model.CheckSignDocumentsStatusResponse;
import com.bulpros.integrations.evrotrust.model.DeliveryDocumentExtendedRequest;
import com.bulpros.integrations.evrotrust.model.DeliveryReceiptsStatusRequest;
import com.bulpros.integrations.evrotrust.model.EvrotrustDeliveryDocumentResponse;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import com.bulpros.integrations.exceptions.EvrotrustUnknownStatusErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.UnknownHttpStatusCodeException;

@Component
@Slf4j
public class DeliveryDocumentRoute extends RouteBuilder {

    @Override
    public void configure() {
        onException(UnknownHttpStatusCodeException.class)
                .handled(true)
                .bean(EvrotrustUnknownStatusErrorHandler.class);
        onException(Exception.class)
                .handled(true)
                .bean(EFormsIntegrationsErrorHandler.class);

        restConfiguration().component("servlet")
                .clientRequestValidation(true)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*");

        rest("/evrotrust/document/delivery")
                .post()
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .type(DeliveryDocumentExtendedRequest.class)
                .outType(EvrotrustDeliveryDocumentResponse.class)
                .route().routeGroup("EVROTRUST").routeId("EvrotrustDeliveryDocument")
                .to("bean:EvrotrustService?method=deliverDocument(${body})");
    }
}
