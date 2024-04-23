package com.bulpros.integrations.evrotrust.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EvrotrustDeliveryReceiptsStatusRequest extends DeliveryReceiptsStatusRequest {
    private String vendorNumber;
    public EvrotrustDeliveryReceiptsStatusRequest(String vendorNumber, List<String> threadIDs){
        super(threadIDs);
        this.vendorNumber = vendorNumber;
    }

}
