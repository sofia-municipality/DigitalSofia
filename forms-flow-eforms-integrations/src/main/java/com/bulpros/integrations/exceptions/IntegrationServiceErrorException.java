package com.bulpros.integrations.exceptions;

public class IntegrationServiceErrorException extends RuntimeException {
    public IntegrationServiceErrorException(String message) {
        super(message);
    }
}