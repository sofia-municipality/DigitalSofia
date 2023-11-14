/*******************************************************************************
 * Copyright (c) 2022 Digitall Nature Bulgaria
 *
 * This program and the accompanying materials
 * are made available under the terms of the Apache License 2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Stefan Tabakov
 *    Nedka Taskova
 *    Stanimir Stoyanov
 *    Pavel Koev
 *    Igor Radomirov
 *******************************************************************************/
package com.bulpros.keycloak.providers.broker;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProvider;
import org.keycloak.broker.saml.SAMLIdentityProviderConfig;
import org.keycloak.dom.saml.v2.assertion.*;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;
import org.keycloak.protocol.saml.SAMLDecryptionKeysLocator;
import org.keycloak.protocol.saml.SamlPrincipalType;
import org.keycloak.protocol.saml.SamlService;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.keycloak.saml.processing.core.saml.v2.constants.X500SAMLProfileConstants;
import org.keycloak.saml.processing.core.saml.v2.util.AssertionUtil;
import org.keycloak.saml.validators.ConditionsValidator;
import org.keycloak.saml.validators.DestinationValidator;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.w3c.dom.Element;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

public class EAuthSAMLEndpoint extends SAMLEndpoint {
    public final static String SAML_PROVIDER_ID = "SAML_PROVIDER_ID";
    private final KeycloakSession session;
    private final DestinationValidator destinationValidator;
    private final boolean DEPRECATED_ENCRYPTION = Boolean.getBoolean("keycloak.saml.deprecated.encryption");

    public EAuthSAMLEndpoint(KeycloakSession session, SAMLIdentityProvider provider, SAMLIdentityProviderConfig config,
            IdentityProvider.AuthenticationCallback callback, DestinationValidator destinationValidator) {
        super(session, provider, config, callback, destinationValidator);
        this.session = session;
        this.destinationValidator = destinationValidator;
    }

    @POST
    @Consumes({ "application/x-www-form-urlencoded" })
    @Override
    public Response postBinding(@FormParam("SAMLRequest") String samlRequest,
            @FormParam("SAMLResponse") String samlResponse, @FormParam("RelayState") String relayState) {
        return (new EAuthSAMLEndpoint.EAuthPostBinding()).execute(samlRequest, samlResponse, relayState, null);
    }

    protected class EAuthPostBinding extends SAMLEndpoint.PostBinding {
        protected EAuthPostBinding() {
            super();
        }

        @Override
        protected Response handleLoginResponse(String samlResponse, SAMLDocumentHolder holder,
                ResponseType responseType, String relayState, String clientId) {
            try {
                AuthenticationSessionModel authSession;
                if (clientId != null && !clientId.trim().isEmpty()) {
                    authSession = EAuthSAMLEndpoint.this.samlIdpInitiatedSSO(clientId);
                } else {
                    authSession = EAuthSAMLEndpoint.this.callback.getAndVerifyAuthenticationSession(relayState);
                }

                EAuthSAMLEndpoint.this.session.getContext().setAuthenticationSession(authSession);
                if (!EAuthSAMLEndpoint.this.isSuccessfulSamlResponse(responseType)) {
                    String statusMessage = responseType.getStatus() != null && responseType.getStatus()
                            .getStatusMessage() != null
                            ? responseType.getStatus().getStatusMessage() : "identityProviderUnexpectedErrorMessage";
                    return EAuthSAMLEndpoint.this.callback.error(statusMessage);
                } else if (responseType.getAssertions() != null && !responseType.getAssertions().isEmpty()) {
                    boolean assertionIsEncrypted = AssertionUtil.isAssertionEncrypted(responseType);
                    if (EAuthSAMLEndpoint.this.config.isWantAssertionsEncrypted() && !assertionIsEncrypted) {
                        EAuthSAMLEndpoint.logger.error("The assertion is not encrypted, which is required.");
                        EAuthSAMLEndpoint.this.event.event(EventType.IDENTITY_PROVIDER_RESPONSE);
                        EAuthSAMLEndpoint.this.event.error("invalid_saml_response");
                        return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession, Response.Status.BAD_REQUEST,
                                "invalidRequesterMessage");
                    } else {
                        Element assertionElement;
                        if (assertionIsEncrypted) {
                            try {
                                if (EAuthSAMLEndpoint.this.DEPRECATED_ENCRYPTION) {
                                    KeyManager.ActiveRsaKey keys = EAuthSAMLEndpoint.this.session.keys()
                                            .getActiveRsaKey(EAuthSAMLEndpoint.this.realm);
                                    assertionElement = AssertionUtil.decryptAssertion(responseType,
                                            keys.getPrivateKey());
                                } else {
                                    assertionElement = AssertionUtil.decryptAssertion(responseType,
                                            new SAMLDecryptionKeysLocator(EAuthSAMLEndpoint.this.session,
                                                    EAuthSAMLEndpoint.this.realm,
                                                    EAuthSAMLEndpoint.this.config.getEncryptionAlgorithm()));
                                }
                            } catch (org.keycloak.saml.common.exceptions.ProcessingException processingException) {
                                SAMLEndpoint.logger.warnf(processingException,
                                        "Not possible to decrypt SAML assertion. Please check realm keys of usage ENC in the realm '%s' and make sure there is a key able to decrypt the assertion encrypted by identity provider '%s'",
                                        EAuthSAMLEndpoint.this.realm.getName(),
                                        EAuthSAMLEndpoint.this.config.getAlias());
                                throw new WebApplicationException(processingException, Response.Status.BAD_REQUEST);
                            }
                        } else {
                            assertionElement = DocumentUtil.getElement(holder.getSamlDocument(),
                                    new QName(JBossSAMLConstants.ASSERTION.get()));
                        }

                        String responseIssuer = responseType.getIssuer() != null
                                ? responseType.getIssuer().getValue()
                                : null;
                        boolean responseIssuerValidationSuccess = EAuthSAMLEndpoint.this.config.getIdpEntityId() == null || responseIssuer != null && responseIssuer.equals(
                                EAuthSAMLEndpoint.this.config.getIdpEntityId());
                        if (!responseIssuerValidationSuccess) {
                            SAMLEndpoint.logger.errorf("Response Issuer validation failed: expected %s, actual %s",
                                    EAuthSAMLEndpoint.this.config.getIdpEntityId(), responseIssuer);
                            EAuthSAMLEndpoint.this.event.event(EventType.IDENTITY_PROVIDER_RESPONSE);
                            EAuthSAMLEndpoint.this.event.error("invalid_saml_response");
                            return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession,
                                    Response.Status.BAD_REQUEST, "invalidRequesterMessage");
                        } else {
                            String expectedRequestId = authSession.getClientNote("SAML_REQUEST_ID_BROKER");
                            boolean inResponseToValidationSuccess = EAuthSAMLEndpoint.this.validateInResponseToAttribute(
                                    responseType, expectedRequestId);
                            if (!inResponseToValidationSuccess) {
                                EAuthSAMLEndpoint.this.event.event(EventType.IDENTITY_PROVIDER_RESPONSE);
                                EAuthSAMLEndpoint.this.event.error("invalid_saml_response");
                                return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession,
                                        Response.Status.BAD_REQUEST, "invalidRequesterMessage");
                            } else {
                                boolean signed = AssertionUtil.isSignedElement(assertionElement);
                                boolean assertionSignatureNotExistsWhenRequired = EAuthSAMLEndpoint.this.config.isWantAssertionsSigned() && !signed;
                                boolean signatureNotValid = signed && EAuthSAMLEndpoint.this.config.isValidateSignature() && !AssertionUtil.isSignatureValid(
                                        assertionElement, this.getIDPKeyLocator());
                                boolean hasNoSignatureWhenRequired = !signed && EAuthSAMLEndpoint.this.config.isValidateSignature() && !this.containsUnencryptedSignature(
                                        holder);
                                if (!assertionSignatureNotExistsWhenRequired && !signatureNotValid && !hasNoSignatureWhenRequired) {
                                    if (AssertionUtil.isIdEncrypted(responseType)) {
                                        try {
                                            if (EAuthSAMLEndpoint.this.DEPRECATED_ENCRYPTION) {
                                                KeyManager.ActiveRsaKey keysx = EAuthSAMLEndpoint.this.session.keys()
                                                        .getActiveRsaKey(EAuthSAMLEndpoint.this.realm);
                                                AssertionUtil.decryptId(responseType, (data) -> {
                                                    return Collections.singletonList(keysx.getPrivateKey());
                                                });
                                            } else {
                                                AssertionUtil.decryptId(responseType,
                                                        new SAMLDecryptionKeysLocator(EAuthSAMLEndpoint.this.session,
                                                                EAuthSAMLEndpoint.this.realm,
                                                                EAuthSAMLEndpoint.this.config.getEncryptionAlgorithm()));
                                            }
                                        } catch (ProcessingException decryptProcessingException) {
                                            SAMLEndpoint.logger.warnf(decryptProcessingException,
                                                    "Not possible to decrypt SAML encryptedId. Please check realm keys of usage ENC in the realm '%s' and make sure there is a key able to decrypt the encryptedId encrypted by identity provider '%s'",
                                                    EAuthSAMLEndpoint.this.realm.getName(),
                                                    EAuthSAMLEndpoint.this.config.getAlias());
                                            throw new WebApplicationException(decryptProcessingException, Response.Status.BAD_REQUEST);
                                        }
                                    }

                                    AssertionType assertion = responseType.getAssertions().get(0).getAssertion();
                                    String assertionIssuer = assertion.getIssuer() != null ? assertion.getIssuer()
                                            .getValue() : null;
                                    boolean assertionIssuerValidationSuccess = EAuthSAMLEndpoint.this.config.getIdpEntityId() == null || assertionIssuer != null && assertionIssuer.equals(
                                            EAuthSAMLEndpoint.this.config.getIdpEntityId());
                                    if (!assertionIssuerValidationSuccess) {
                                        SAMLEndpoint.logger.errorf(
                                                "Assertion Issuer validation failed: expected %s, actual %s",
                                                EAuthSAMLEndpoint.this.config.getIdpEntityId(), assertionIssuer);
                                        EAuthSAMLEndpoint.this.event.event(EventType.IDENTITY_PROVIDER_RESPONSE);
                                        EAuthSAMLEndpoint.this.event.error("invalid_saml_response");
                                        return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession,
                                                Response.Status.BAD_REQUEST, "invalidRequesterMessage");
                                    } else {
                                        NameIDType subjectNameID = EAuthSAMLEndpoint.this.getSubjectNameID(assertion);
                                        String principal = EAuthSAMLEndpoint.this.getPrincipal(assertion);
                                        if (principal == null) {
                                            SAMLEndpoint.logger.errorf("no principal in assertion; expected: %s",
                                                    EAuthSAMLEndpoint.this.expectedPrincipalType());
                                            EAuthSAMLEndpoint.this.event.event(EventType.IDENTITY_PROVIDER_RESPONSE);
                                            EAuthSAMLEndpoint.this.event.error("invalid_saml_response");
                                            return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession,
                                                    Response.Status.BAD_REQUEST, "invalidRequesterMessage");
                                        } else {
                                            BrokeredIdentityContext identity = new BrokeredIdentityContext(principal);
                                            identity.getContextData().put("SAML_LOGIN_RESPONSE", responseType);
                                            identity.getContextData().put("SAML_ASSERTION", assertion);
                                            identity.setAuthenticationSession(authSession);
                                            identity.setUsername(principal);
                                            if (subjectNameID != null && subjectNameID.getFormat() != null && subjectNameID.getFormat()
                                                    .toString()
                                                    .equals(JBossSAMLURIConstants.NAMEID_FORMAT_EMAIL.get())) {
                                                identity.setEmail(subjectNameID.getValue());
                                            }

                                            if (EAuthSAMLEndpoint.this.config.isStoreToken()) {
                                                identity.setToken(samlResponse);
                                            }

                                            ConditionsValidator.Builder cvb = (new ConditionsValidator.Builder(
                                                    assertion.getID(), assertion.getConditions(),
                                                    EAuthSAMLEndpoint.this.destinationValidator)).clockSkewInMillis(
                                                    1000 * EAuthSAMLEndpoint.this.config.getAllowedClockSkew());

                                            try {
                                                String issuerURL = EAuthSAMLEndpoint.this.getEntityId(
                                                        EAuthSAMLEndpoint.this.session.getContext().getUri(),
                                                        EAuthSAMLEndpoint.this.realm);
                                                cvb.addAllowedAudience(URI.create(issuerURL));
                                                if (responseType.getDestination() != null) {
                                                    cvb.addAllowedAudience(URI.create(responseType.getDestination()));
                                                }
                                            } catch (IllegalArgumentException illegalArgumentException) {
                                            }

                                            if (!cvb.build().isValid()) {
                                                SAMLEndpoint.logger.error("Assertion expired.");
                                                EAuthSAMLEndpoint.this.event.event(
                                                        EventType.IDENTITY_PROVIDER_RESPONSE);
                                                EAuthSAMLEndpoint.this.event.error("invalid_saml_response");
                                                return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession,
                                                        Response.Status.BAD_REQUEST, "expiredCodeMessage");
                                            } else {
                                                AuthnStatementType authn = null;
                                                Iterator<StatementAbstractType> iterator = assertion.getStatements().iterator();

                                                while (iterator.hasNext()) {
                                                    Object statement = iterator.next();
                                                    if (statement instanceof AuthnStatementType) {
                                                        authn = (AuthnStatementType) statement;
                                                        identity.getContextData().put("SAML_AUTHN_STATEMENT", authn);
                                                        break;
                                                    }
                                                }

                                                String email;
                                                if (assertion.getAttributeStatements() != null) {
                                                    email = getX500Attribute(assertion, X500SAMLProfileConstants.EMAIL);
                                                    if (email != null) {
                                                        identity.setEmail(email);
                                                    }
                                                }

                                                email = EAuthSAMLEndpoint.this.config.getAlias() + "." + principal;
                                                identity.setBrokerUserId(email);
                                                identity.setIdpConfig(EAuthSAMLEndpoint.this.config);
                                                identity.setIdp(EAuthSAMLEndpoint.this.provider);
                                                if (authn != null && authn.getSessionIndex() != null) {
                                                    identity.setBrokerSessionId(
                                                            EAuthSAMLEndpoint.this.config.getAlias() + "." + authn.getSessionIndex());
                                                }

                                                try {
                                                    String providerId = assertionElement.getElementsByTagNameNS(
                                                                    "urn:oasis:names:tc:SAML:2.0:assertion", "Subject").item(0)
                                                            .getTextContent();
                                                    logger.debug("EAuth Provider id: " + providerId);
                                                    var contextData = identity.getContextData();
                                                    if (!contextData.isEmpty()) {
                                                        identity.getContextData().put("SAML_PROVIDER_ID", providerId);
                                                    }
                                                } catch (Exception e) {
                                                    logger.warn(
                                                            "Could not read provider id from SAML response. Reason: " + e.getMessage());
                                                }

                                                return EAuthSAMLEndpoint.this.callback.authenticated(identity);
                                            }
                                        }
                                    }
                                } else {
                                    SAMLEndpoint.logger.error("validation failed");
                                    EAuthSAMLEndpoint.this.event.event(EventType.IDENTITY_PROVIDER_RESPONSE);
                                    EAuthSAMLEndpoint.this.event.error("invalid_signature");
                                    return ErrorPage.error(EAuthSAMLEndpoint.this.session, authSession,
                                            Response.Status.BAD_REQUEST, "invalidRequesterMessage", new Object[0]);
                                }
                            }
                        }
                    }
                } else {
                    return EAuthSAMLEndpoint.this.callback.error("identityProviderUnexpectedErrorMessage");
                }
            } catch (WebApplicationException webApplicationException) {
                return webApplicationException.getResponse();
            } catch (Exception exception) {
                throw new IdentityBrokerException("Could not process response from SAML identity provider.", exception);
            }
        }
    }

    private boolean validateInResponseToAttribute(ResponseType responseType, String expectedRequestId) {
        if (expectedRequestId != null && !expectedRequestId.isEmpty()) {
            if (responseType.getInResponseTo() == null) {
                logger.error(
                        "Response Validation Error: InResponseTo attribute was expected but not present in received response");
                return false;
            } else {
                String responseInResponseToValue = responseType.getInResponseTo();
                if (responseInResponseToValue.isEmpty()) {
                    logger.error(
                            "Response Validation Error: InResponseTo attribute was expected but it is empty in received response");
                    return false;
                } else if (!responseInResponseToValue.equals(expectedRequestId)) {
                    logger.error(
                            "Response Validation Error: received InResponseTo attribute does not match the expected request ID");
                    return false;
                } else if (responseType.getAssertions().isEmpty()) {
                    return true;
                } else {
                    SubjectType subjectElement = ((ResponseType.RTChoiceType) responseType.getAssertions()
                            .get(0)).getAssertion().getSubject();
                    if (subjectElement != null && subjectElement.getConfirmation() != null && !subjectElement.getConfirmation()
                            .isEmpty()) {
                        SubjectConfirmationType subjectConfirmationElement = subjectElement.getConfirmation()
                                .get(0);
                        if (subjectConfirmationElement != null) {
                            SubjectConfirmationDataType subjectConfirmationDataElement = subjectConfirmationElement.getSubjectConfirmationData();
                            if (subjectConfirmationDataElement != null && subjectConfirmationDataElement.getInResponseTo() != null) {
                                String subjectConfirmationDataInResponseToValue = subjectConfirmationDataElement.getInResponseTo();
                                if (subjectConfirmationDataInResponseToValue.isEmpty()) {
                                    logger.error(
                                            "Response Validation Error: SubjectConfirmationData InResponseTo attribute was expected but it is empty in received response");
                                    return false;
                                }

                                if (!subjectConfirmationDataInResponseToValue.equals(expectedRequestId)) {
                                    logger.error(
                                            "Response Validation Error: received SubjectConfirmationData InResponseTo attribute does not match the expected request ID");
                                    return false;
                                }
                            }
                        }
                    }

                    return true;
                }
            }
        } else {
            return true;
        }
    }

    private AuthenticationSessionModel samlIdpInitiatedSSO(String clientUrlName) {
        EAuthSAMLEndpoint.this.event.event(EventType.LOGIN);
        CacheControlUtil.noBackButtonCacheControlHeader(session);
        Optional<ClientModel> oClient = EAuthSAMLEndpoint.this.session.clients()
                .searchClientsByAttributes(EAuthSAMLEndpoint.this.realm,
                        Collections.singletonMap("saml_idp_initiated_sso_url_name", clientUrlName), 0, 1).findFirst();
        if (!oClient.isPresent()) {
            EAuthSAMLEndpoint.this.event.error("client_not_found");
            Response responsex = ErrorPage.error(EAuthSAMLEndpoint.this.session, null,
                    Response.Status.BAD_REQUEST, "clientNotFoundMessage");
            throw new WebApplicationException(responsex);
        } else {
            LoginProtocolFactory factory = (LoginProtocolFactory) EAuthSAMLEndpoint.this.session.getKeycloakSessionFactory()
                    .getProviderFactory(LoginProtocol.class, "saml");
            SamlService samlService = (SamlService) factory.createProtocolEndpoint(session,
                    EAuthSAMLEndpoint.this.event);
            ResteasyProviderFactory.getInstance().injectProperties(samlService);
            AuthenticationSessionModel authSession = samlService.getOrCreateLoginSessionForIdpInitiatedSso(
                    EAuthSAMLEndpoint.this.session, EAuthSAMLEndpoint.this.realm, oClient.get(), null);
            if (authSession == null) {
                EAuthSAMLEndpoint.this.event.error("invalid_redirect_uri");
                Response response = ErrorPage.error(EAuthSAMLEndpoint.this.session, (AuthenticationSessionModel) null,
                        Response.Status.BAD_REQUEST, "invalidRedirectUriMessage", new Object[0]);
                throw new WebApplicationException(response);
            } else {
                return authSession;
            }
        }
    }

    private boolean isSuccessfulSamlResponse(ResponseType responseType) {
        return responseType != null && responseType.getStatus() != null && responseType.getStatus()
                .getStatusCode() != null && responseType.getStatus().getStatusCode()
                .getValue() != null && Objects.equals(responseType.getStatus().getStatusCode().getValue().toString(),
                JBossSAMLURIConstants.STATUS_SUCCESS.get());
    }

    private String expectedPrincipalType() {
        SamlPrincipalType principalType = EAuthSAMLEndpoint.this.config.getPrincipalType();
        switch (principalType) {
        case SUBJECT:
            return principalType.name();
        case ATTRIBUTE:
        case FRIENDLY_ATTRIBUTE:
            return String.format("%s(%s)", principalType.name(), EAuthSAMLEndpoint.this.config.getPrincipalAttribute());
        default:
            return null;
        }
    }

    private NameIDType getSubjectNameID(AssertionType assertion) {
        SubjectType subject = assertion.getSubject();
        SubjectType.STSubType subType = subject.getSubType();
        return subType != null ? (NameIDType) subType.getBaseID() : null;
    }

    private String getX500Attribute(AssertionType assertion, X500SAMLProfileConstants attribute) {
        return this.getFirstMatchingAttribute(assertion, attribute::correspondsTo);
    }

    private String getFirstMatchingAttribute(AssertionType assertion, Predicate<AttributeType> predicate) {
        return assertion.getAttributeStatements().stream().map(AttributeStatementType::getAttributes)
                .flatMap(Collection::stream).map(AttributeStatementType.ASTChoiceType::getAttribute).filter(predicate)
                .map(AttributeType::getAttributeValue).flatMap(Collection::stream).findFirst().map(Object::toString)
                .orElse(null);
    }

    private String getEntityId(UriInfo uriInfo, RealmModel realm) {
        String configEntityId = EAuthSAMLEndpoint.this.config.getEntityId();
        return configEntityId != null && !configEntityId.isEmpty()
                ? configEntityId
                : UriBuilder.fromUri(uriInfo.getBaseUri()).path("realms").path(realm.getName()).build()
                        .toString();
    }

    private String getPrincipal(AssertionType assertion) {
        SamlPrincipalType principalType = EAuthSAMLEndpoint.this.config.getPrincipalType();
        if (principalType != null && !principalType.equals(SamlPrincipalType.SUBJECT)) {
            return principalType.equals(SamlPrincipalType.ATTRIBUTE)
                    ? this.getAttributeByName(assertion, EAuthSAMLEndpoint.this.config.getPrincipalAttribute())
                    : this.getAttributeByFriendlyName(assertion, EAuthSAMLEndpoint.this.config.getPrincipalAttribute());
        } else {
            NameIDType subjectNameID = this.getSubjectNameID(assertion);
            return subjectNameID != null ? subjectNameID.getValue() : null;
        }
    }

    private String getAttributeByName(AssertionType assertion, String name) {
        return this.getFirstMatchingAttribute(assertion, (attribute) -> {
            return Objects.equals(attribute.getName(), name);
        });
    }

    private String getAttributeByFriendlyName(AssertionType assertion, String friendlyName) {
        return this.getFirstMatchingAttribute(assertion, (attribute) -> {
            return Objects.equals(attribute.getFriendlyName(), friendlyName);
        });
    }
}
