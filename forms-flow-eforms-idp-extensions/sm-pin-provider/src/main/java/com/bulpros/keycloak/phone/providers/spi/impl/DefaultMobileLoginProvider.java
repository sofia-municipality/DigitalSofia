package com.bulpros.keycloak.phone.providers.spi.impl;

import com.bulpros.keycloak.phone.providers.jpa.MobileLogin;
import com.bulpros.keycloak.phone.providers.representations.MobileLoginRepresentation;
import com.bulpros.keycloak.phone.providers.spi.MobileLoginProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.time.Instant;
import java.util.Date;

public class DefaultMobileLoginProvider implements MobileLoginProvider {

    private static final Logger logger = Logger.getLogger(DefaultMobileLoginProvider.class);
    private final KeycloakSession session;
    private final int loginExpiresIn;
    private final int maxLoginCount;

    DefaultMobileLoginProvider(KeycloakSession session, Config.Scope config) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
        this.loginExpiresIn = config.getInt("loginExpiresIn", 7200);
        this.maxLoginCount = config.getInt("maxLoginCount", 10);
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public MobileLoginRepresentation ongoingMobileLogin(String personIdentifier, String clientId, String grantType,
            String scope) {
        try {
            MobileLogin entity = getEntityManager().createNamedQuery("ongoingMobileLogin", MobileLogin.class) //
                    .setParameter("realmId", getRealm().getId()) //
                    .setParameter("personIdentifier", personIdentifier) //
                    .setParameter("clientId", clientId) //
                    .setParameter("grantType", grantType) //
                    .setParameter("scope", scope) //
                    .getSingleResult();
            MobileLoginRepresentation mobileLoginRepresentation = new MobileLoginRepresentation();

            mobileLoginRepresentation.setId(entity.getId());
            mobileLoginRepresentation.setPersonIdentifier(entity.getPersonIdentifier());
            mobileLoginRepresentation.setSumLoginCount(entity.getSumLoginCount());
            mobileLoginRepresentation.setGrantType(entity.getGrantType());
            mobileLoginRepresentation.setClientId(entity.getClientId());
            mobileLoginRepresentation.setScope(entity.getScope());
            mobileLoginRepresentation.setCreatedAt(entity.getCreatedAt());

            return mobileLoginRepresentation;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.error("Ongoing Mobile Login exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void changeSumLoginCount(String personIdentifier, String clientId, String grantType, String scope,
            int sumLoginCount) {
        getEntityManager().createNamedQuery("changeSumLoginCount") //
                .setParameter("sumLoginCount", sumLoginCount).setParameter("realmId", getRealm().getId()) //
                .setParameter("personIdentifier", personIdentifier) //
                .setParameter("clientId", clientId) //
                .setParameter("grantType", grantType) //
                .setParameter("scope", scope) //
                .executeUpdate();
    }

    @Override
    public int deleteMobileLogin(String personIdentifier) {
        return getEntityManager().createNamedQuery("deleteMobileLogin") //
                .setParameter("realmId", getRealm().getId()) //
                .setParameter("personIdentifier", personIdentifier) //
                .executeUpdate();
    }

    @Override
    public void persistCode(MobileLoginRepresentation mobileLogin) {

        MobileLogin entity = new MobileLogin();
        Instant now = Instant.now();

        entity.setId(mobileLogin.getId());
        entity.setRealmId(getRealm().getId());
        entity.setPersonIdentifier(mobileLogin.getPersonIdentifier());
        entity.setSumLoginCount(1);
        entity.setGrantType(mobileLogin.getGrantType());
        entity.setClientId(mobileLogin.getClientId());
        entity.setScope(mobileLogin.getScope());
        entity.setCreatedAt(Date.from(now));

        getEntityManager().persist(entity);
    }

    @Override
    public int getMaxLoginCount() {
        return this.maxLoginCount;
    }

    @Override
    public int getLoginExpiresIn() {
        return this.loginExpiresIn;
    }

    @Override
    public void close() {
    }
}
