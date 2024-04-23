package com.bulpros.keycloak.phone.providers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckForGeneratedCodeResponse {
    private boolean codeExists;
    private String code;
    private long expiresIn;
}
