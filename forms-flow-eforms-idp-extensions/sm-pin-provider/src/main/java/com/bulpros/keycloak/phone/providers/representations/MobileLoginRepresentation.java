package com.bulpros.keycloak.phone.providers.representations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileLoginRepresentation {
    private String id;
    private String realmId;
    private String personIdentifier;
    private int sumLoginCount;
    private String grantType;
    private String clientId;
    private String scope;
    private Date createdAt;

    public static MobileLoginRepresentation forMobileLogin(String personIdentifier, String grantType,
            String clientId, String scope) {

        MobileLoginRepresentation mobileLoginRepresentation = new MobileLoginRepresentation();

        mobileLoginRepresentation.id = KeycloakModelUtils.generateId();
        mobileLoginRepresentation.personIdentifier = personIdentifier;
        mobileLoginRepresentation.grantType = grantType;
        mobileLoginRepresentation.clientId = clientId;
        mobileLoginRepresentation.scope = scope;


        return mobileLoginRepresentation;
    }
}
