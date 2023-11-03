package com.bulpros.integrations.evrotrust.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvrotrustConfirmPersonalDataRequest extends BaseModelConfirmPersonalDataRequest {

    private SignInfo signInfo;
    private String vendorNumber;
    private String publicKey;
    private Document document;

    @Getter
    @Setter
    public static class Document {
        private Long dateExpire;
    }

    @Getter
    @Setter
    public static class SignInfo {
        private String algorithm;
    }

    public EvrotrustConfirmPersonalDataRequest(String publicKey, String vendorNumber,
            ConfirmPersonalDataRequest confirmPersonalDataRequest) {
        this.publicKey = publicKey;
        this.vendorNumber = vendorNumber;

        this.signInfo = new EvrotrustConfirmPersonalDataRequest.SignInfo();
        this.signInfo.algorithm = "SHA256";

        this.document = new EvrotrustConfirmPersonalDataRequest.Document();
        this.document.dateExpire = confirmPersonalDataRequest.getDocument().getDateExpire().getTime() / 1000;
    }
}
