package com.bulpros.integrations.camel.routes.opendata;

import com.bulpros.integrations.camel.routes.opendata.model.ResourceData;
import com.bulpros.integrations.ePayment.model.*;
import com.bulpros.integrations.exceptions.EFormsIntegrationsErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OpenDataRoute extends RouteBuilder {

    @Value("${com.bulpros.opendata.cron.job}")
    private String cronJob;
    @Value("${com.bulpros.opendata.active.integration}")
    private Boolean autoStartup;

    @Override
    public void configure() {
        from("quartz://openData/openDataSchedule?cron=" + cronJob + "&trigger.timeZone=Europe/Sofia")
                .autoStartup(autoStartup)
                .to("bean:openDataService?method=createResource()");


    }
}
