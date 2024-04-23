package com.bulpros.integrations.camel.routes.evrotrust;

import com.bulpros.integrations.evrotrust.model.UserCheckExtendedRequest;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import com.bulpros.integrations.exceptions.EvrotrustUnknownStatusErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

@Component
@Slf4j
public class UserCheckRoute extends RouteBuilder {

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

        rest("/evrotrust/user/exist")
                .post()
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .type(UserCheckExtendedRequest.class)
                .route().routeId("EvrotrustPostUserExist")
                .setHeader(Exchange.HTTP_RESPONSE_CODE,simple(String.valueOf(HTTP_NO_CONTENT)))
                .to("bean:EvrotrustService?method=userExistPost(${body})");
        }
}
