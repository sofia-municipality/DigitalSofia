package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReversalModelResponse {

    private String status;
    private String code;
    private String message;
}
