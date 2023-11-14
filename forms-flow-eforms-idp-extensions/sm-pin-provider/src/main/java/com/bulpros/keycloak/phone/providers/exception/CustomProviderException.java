package com.bulpros.keycloak.phone.providers.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomProviderException extends Exception {

    public CustomProviderException(String message) {
        super(message);
    }

    public CustomProviderException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
