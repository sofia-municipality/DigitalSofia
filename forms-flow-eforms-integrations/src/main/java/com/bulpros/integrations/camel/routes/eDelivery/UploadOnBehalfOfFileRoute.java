package com.bulpros.integrations.camel.routes.eDelivery;

import com.bulpros.integrations.eDelivery.model.UploadFileOnBehalfOfResponse;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UploadOnBehalfOfFileRoute extends RouteBuilder {

    public void configure() {
        onException(Exception.class)
                .handled(true)
                .bean(EFormsIntegrationsErrorHandler.class);

        restConfiguration().component("servlet")
                .clientRequestValidation(true)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*");

        rest("/eDelivery/upload/obo/blobs")
                .post()
                .bindingMode(RestBindingMode.off)
                .consumes(MediaType.MULTIPART_FORM_DATA_VALUE)
                .outType(UploadFileOnBehalfOfResponse.class)
                .route().routeGroup("EDELIVERY").routeId("EDeliveryUploadFileOnBehalfOf")
                .to("bean:eDeliveryService?method=uploadFileOnBehalfOf")
                .marshal().json();
    }

}
