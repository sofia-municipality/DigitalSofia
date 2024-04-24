package com.bulpros.keycloak.phone.providers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCodeStatusResponse {
    private boolean isCodeUpdated;
}
