package com.bulpros.keycloak.phone.providers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExtendedModel {
    private Boolean isRegistered;
    private Boolean isIdentified;
    private Boolean isRejected;
    private Boolean isSupervised;
    private Boolean isReadyToSign;
    private Boolean hasConfirmedPhone;
    private Boolean hasConfirmedEmail;
}
