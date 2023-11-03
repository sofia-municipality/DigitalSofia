package com.bulpros.integrations.agentWS.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
public class Obligations {

    private int debtinstalmentId;
    private String rnu;
    private int municipalityId;
    private String municipalityName;
    private String partidaNo;
    private String registerNo;
    private String propertyAddress;
    private String taxPeriodYear;
    private String kindDebtRegId;
    private String kindDebtRegName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date termPayDate;
    private int instNo;
    private int residual;
    private int interest;
    private int payOrder;

}
