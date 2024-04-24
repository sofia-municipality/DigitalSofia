package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DownloadRequest {

    private String vendorNumber;
    private String transactionID;

    public DownloadRequest(String vendorNumber, String transactionID) {
        this.vendorNumber = vendorNumber;
        this.transactionID = transactionID;
    }
}
