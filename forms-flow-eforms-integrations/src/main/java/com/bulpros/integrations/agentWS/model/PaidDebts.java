package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaidDebts {

    private int debtInstalmentId;
    private String kindDebtRegId;
    private String kindDebtRegName;
    private int payInstSum;
    private int payInterestSum;
    private int payDiscSum;
    private int balInstSum;
    private int balInterestSum;
}
