package org.camunda.bpm.extension.hooks.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Notification Service Exception.
 * Specialized exception class for email calls.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NotificationServiceException extends RuntimeException {

    public NotificationServiceException(String message) {
        super(message);
    }
}
