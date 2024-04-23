package com.bulpros.keycloak.phone.providers.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "PIN_MESSAGE_TOKEN_CODE")
@NamedQueries({ //
        @NamedQuery(name = "ongoingProcess", //
                query = "FROM TokenCode t WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " + //
                        "AND t.expiresAt >= :now AND t.type = :type" //
        ), //
        @NamedQuery(name = "deleteUserProcess", //
                query = "DELETE TokenCode t WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " + //
                        "AND t.type = :type" //
        ), //
        @NamedQuery(name = "changeStatusInProcess", //
                query = "UPDATE TokenCode t SET t.status = :status " + //
                        "WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " + //
                        "AND t.code = :code " + //
                        "AND t.type = :type" //
        ), //
        @NamedQuery(name = "getStatus", //
                query = "FROM TokenCode t WHERE t.realmId = :realmId " + //
                        "AND t.personIdentifier = :personIdentifier " + //
                        "AND t.type = :type " + //
                        "AND t.code = :code" //
        ), //
        @NamedQuery(name = "processesSinceTarget", //
                query = "SELECT COUNT(t) FROM TokenCode t WHERE t.realmId = :realmId " + //
                        "AND t.pin = :pin " + //
                        "AND t.createdAt >= :date AND t.type = :type" //
        ), //
        @NamedQuery(name = "processesSinceSource", //
                query = "SELECT COUNT(t) FROM TokenCode t WHERE t.realmId = :realmId " + //
                        "AND t.ip = :addr " + //
                        "AND t.createdAt >= :date AND t.type = :type" //
        ) //
})
public class TokenCode {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "REALM_ID", nullable = false)
    private String realmId;

    @Column(name = "PERSON_IDENTIFIER", nullable = false)
    private String personIdentifier;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EXPIRES_AT", nullable = false)
    private Date expiresAt;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "IP")
    private String ip;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "HOST")
    private String host;
}
