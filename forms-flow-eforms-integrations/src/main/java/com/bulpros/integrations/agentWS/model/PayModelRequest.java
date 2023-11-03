package com.bulpros.integrations.agentWS.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PayModelRequest {

    private int companyId;
    private int operatorId;
    private String agentTransactionId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:sss'Z'")
    private Date agentTransactionDate;
    private int municipalityId;
    private SubjectsInstalments[] subjectsInstalments;
}
