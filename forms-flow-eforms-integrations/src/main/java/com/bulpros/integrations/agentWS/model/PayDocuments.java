package com.bulpros.integrations.agentWS.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PayDocuments {

    private TaxSubject taxSubject;
    private int payDocumentId;
    private String documentNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date documentDate;
    private String partidaNo;
    private int documentSum;
    private PaidDebts[] paidDebts;
}
