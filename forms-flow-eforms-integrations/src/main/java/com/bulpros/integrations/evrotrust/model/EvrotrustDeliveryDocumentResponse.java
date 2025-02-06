package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EvrotrustDeliveryDocumentResponse {

    private String threadID;
    private List<Transactions> transactions;

    @Getter
    @Setter
    public static class Transactions {
        String transactionID;
        String identificationNumber;
    }
}
