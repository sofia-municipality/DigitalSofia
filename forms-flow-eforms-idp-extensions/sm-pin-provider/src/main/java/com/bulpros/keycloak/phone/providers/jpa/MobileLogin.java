package com.bulpros.keycloak.phone.providers.jpa;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "MOBILE_LOGIN")
@NamedQueries({ //
        @NamedQuery(name = "changeSumLoginCount", //
                query = "UPDATE MobileLogin t SET t.sumLoginCount = :sumLoginCount " + //
                        "WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " + //
                        "AND t.clientId = :clientId " + //
                        "AND t.grantType = :grantType " + //
                        "AND t.scope =:scope" //
        ), //
        @NamedQuery(name = "ongoingMobileLogin", //
                query = "FROM MobileLogin t WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " + //
                        "AND t.clientId = :clientId " + //
                        "AND t.grantType = :grantType " + //
                        "AND t.scope =:scope" //
        ), //
        @NamedQuery(name = "deleteMobileLogin", //
                query = "DELETE MobileLogin t WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " //
        ) //
})
public class MobileLogin {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "REALM_ID", nullable = false)
    private String realmId;

    @Column(name = "PERSON_IDENTIFIER", nullable = false)
    private String personIdentifier;

    @Column(name = "SUM_LOGIN_COUNT")
    private Integer sumLoginCount;

    @Column(name = "GRANT_TYPE", nullable = false)
    private String grantType;

    @Column(name = "CLIENT_ID", nullable = false)
    private String clientId;

    @Column(name = "SCOPE", nullable = false)
    private String scope;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;
}
