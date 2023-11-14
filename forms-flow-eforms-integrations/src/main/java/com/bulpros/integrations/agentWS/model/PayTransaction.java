package com.bulpros.integrations.agentWS.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PayTransaction {

    private int payTransactionId;
    private int paidSum;
    private String authorizationCode;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionTime;
    private int agentTransactionId;
}
