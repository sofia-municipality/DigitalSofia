package com.bulpros.integrations.evrotrust.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseModelConfirmPersonalDataRequest {

    private ConfirmPersonalDataRequest.Includes includes;
    @JsonProperty("BIOrequired")
    private int biorequired;
    private UserCheckExtendedRequest user;
    private String urlCallback;
    private String identificationReason;

    @Getter
    @Setter
    public static class Includes {
        private boolean names;
        private boolean latinNames;
        private boolean phones;
        private boolean emails;
        private boolean address;
        private boolean documentType;
        private boolean documentNumber;
        private boolean documentIssuerName;
        private boolean documentValidDate;
        private boolean documentIssueDate;
        private boolean documentCountry;

        private boolean identificationNumber;
        private boolean gender;
        private boolean nationality;
        private boolean documentPicture;
        private boolean documentSignature;
        private boolean picFront;
        private boolean picBack;

        private boolean picIDCombined;
        private boolean dateOfBirth;
        private boolean placeOfBirth;
    }
}
