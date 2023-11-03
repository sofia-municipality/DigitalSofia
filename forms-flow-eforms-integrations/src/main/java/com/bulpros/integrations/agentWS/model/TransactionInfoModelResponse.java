package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionInfoModelResponse {

    private String transactionStatus;
    private PayTransaction payTransaction;
    private PayDocuments[] payDocuments;
}
