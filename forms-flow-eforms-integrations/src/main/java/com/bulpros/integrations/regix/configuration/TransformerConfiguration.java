package com.bulpros.integrations.regix.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.xml.transform.TransformerFactory;
@Configuration
public class TransformerConfiguration {
    @Bean TransformerFactory transformerFactory(){
        return TransformerFactory.newInstance();
    }
}