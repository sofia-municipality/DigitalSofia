package com.bulpros.integrations.evrotrust.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvrotrustDeliveryDocumentRequest {
    private Document document;
    private String vendorNumber;
    private User user;
    private String publicKey;

    @Getter
    @Setter
    public static class Document {
        private String description;
        private Long dateExpire;
        private String checksumDocument;
    }

    @Getter
    @Setter
    public static class User {
        private String identificationNumber;
        private String country;
        private String email;
        private String phone;
    }

    public EvrotrustDeliveryDocumentRequest(String publicKey, String vendorNumber, String vendorApiKey,
            DeliveryDocumentRequest deliveryDocumentRequest, byte[] file) {
        this.publicKey = publicKey;
        this.vendorNumber = vendorNumber;

        this.user = new EvrotrustDeliveryDocumentRequest.User();
        this.user.identificationNumber = deliveryDocumentRequest.getUser().getIdentificationNumber();
        this.document = new EvrotrustDeliveryDocumentRequest.Document();
        this.document.dateExpire = deliveryDocumentRequest.getDocument().getDateExpire().getTime() / 1000;
        this.document.description = deliveryDocumentRequest.getDocument().getDescription();
        HmacUtils hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_512, vendorApiKey);
        this.document.checksumDocument = hmac.hmacHex(file);
    }
}
