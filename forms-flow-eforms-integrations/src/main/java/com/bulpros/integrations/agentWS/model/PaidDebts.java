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
    private float payInstSum;
    private float payInterestSum;
    private float payDiscSum;
    private float balInstSum;
    private float balInterestSum;
}
