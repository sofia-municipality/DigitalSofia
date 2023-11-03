package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaxSubject {

    private int taxsubjectid;
    private String idn;
    private String name;
    private String permanentaddress;

}
