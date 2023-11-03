package org.camunda.bpm.extension.hooks.services;

import java.util.Map;

public interface NotificationService {
    void sendMailNotification(String formKey, String email, String attachmentUrl, Map<String, Object> variables);
}
