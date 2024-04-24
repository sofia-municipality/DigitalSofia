package com.bulpros.keycloak.phone.providers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetStatusModel {
    private String personIdentifier;
    private String code;
}
