package com.bulpros.integrations.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationMailRequest {

    private String to;
    private String subject;
    private String body;
    private Attachment attachment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String content;
        private String name;
        private String contentType;
    }
}
