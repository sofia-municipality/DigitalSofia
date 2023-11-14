package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvrotrustWithdrawDocumentStatusRequest extends WithdrawDocumentStatusRequest {

    private String vendorNumber;

    public EvrotrustWithdrawDocumentStatusRequest(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }
}
