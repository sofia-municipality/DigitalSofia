package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ObligationsModelResponse {

    private TaxSubject taxSubject;
    private Obligations [] obligations;
    private boolean hasMore;
}
