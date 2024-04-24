package com.bulpros.keycloak.phone.providers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonIdentifierResponse {
    private boolean userExist;
    private boolean hasPin;
    private boolean hasContactInfo;
    @JsonProperty(value = "isVerified")
    private boolean isVerified;
}