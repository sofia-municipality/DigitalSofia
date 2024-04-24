package com.bulpros.integrations.agentWS.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class Obligations {
    @JsonProperty(value = "debtInstalmentId")
    @JsonAlias("debtinstalmentId")
    private int debtInstalmentId;
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
    private float residual;
    private float interest;
    private int payOrder;

}
