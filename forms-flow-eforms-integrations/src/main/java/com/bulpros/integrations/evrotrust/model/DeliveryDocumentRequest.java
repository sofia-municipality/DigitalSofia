package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DeliveryDocumentRequest {

    private Document document;
    private User user;


    @Getter
    @Setter
    public static class Document {
        String description;
        Date dateExpire;
    }

    @Getter
    @Setter
    public static class User {
        String identificationNumber;
    }
}
