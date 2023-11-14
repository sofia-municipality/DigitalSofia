package com.bulpros.integrations.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EvrotrustUnknownStatusErrorHandler {

    @Handler
    public void prepareErrorResponse(Exchange exchange) {
        String routeId = exchange.getFromRouteId();
        String routeGroup = exchange.getContext().getRoute(routeId).getGroup();
        Throwable throwable = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        log.error(throwable.getMessage(), throwable);
        Message msg = exchange.getMessage();
        ExceptionBody exceptionBody;
        UnknownHttpStatusCodeException exception = ((UnknownHttpStatusCodeException) throwable);
        if (exception.getRawStatusCode() == 443) {
            exceptionBody = ExceptionBody.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(prepareExceptionMessageCode(routeGroup, null))
                    .data(throwable.getMessage()).build();

            msg.setHeader(Exchange.HTTP_RESPONSE_CODE, "404");
            msg.setBody(exceptionBody);
        } else {
            exceptionBody = ExceptionBody.builder()
                    .status(exception.getRawStatusCode())
                    .error(exception.getResponseBodyAsString())
                    .message(prepareExceptionMessageCode(routeGroup, IntegrationsExCauseType.COMMUNICATION))
                    .data(exception.getResponseBodyAsString()).build();
            msg.setHeader(Exchange.HTTP_RESPONSE_CODE, "500");
            msg.setBody(exceptionBody);
        }
    }

    private String prepareExceptionMessageCode(String externalSystem, IntegrationsExCauseType exCauseType) {
        return Stream.of("ERROR", "INTEGRATIONS", externalSystem, exCauseType)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining("."));
    }
}
