package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Instalments {

    private int debtInstalmentId;
    private float paidInstalmentSum;
    private float paidInterestSum;
}
