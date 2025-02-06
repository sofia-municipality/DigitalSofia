package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DeliveryDocumentExtendedRequest extends DeliveryDocumentRequest {
    private DocumentToDeliver documentToDeliver;

    @Getter
    @Setter
    public static class DocumentToDeliver {
        String content;
        String contentType;
        String name;
    }
}
