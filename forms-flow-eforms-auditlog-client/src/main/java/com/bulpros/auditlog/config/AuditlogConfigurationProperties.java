package com.bulpros.auditlog.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:auditlog-resource-${spring.profiles.active}.properties")
public class AuditlogConfigurationProperties {

    @Value("${com.bulpros.eforms-integrations.auditlog.prefix:'/auditlog'}")
    private String auditlogPreffix;

}