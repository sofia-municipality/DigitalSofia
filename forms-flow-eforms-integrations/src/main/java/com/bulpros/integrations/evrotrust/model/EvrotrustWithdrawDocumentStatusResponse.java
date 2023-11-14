package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvrotrustWithdrawDocumentStatusResponse {

    private Integer status; // 1 - Successful withdraw; 2 - Not successful withdraw; 3 - Document already withdrawn.

}
