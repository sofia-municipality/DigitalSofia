"""Keycloak representation."""

import json
from typing import Dict, List

from flask import current_app

from admin_api.constants import Role


def get_import_json(tenant_key: str, bpm_secret: str,  # pylint:disable=too-many-arguments
                    web_url: str,
                    camunda_url: str,
                    analytics_url: str,
                    roles: List[Dict],
                    create_default_users: bool = False):
    """Return partial import json for keycloak."""
    partial_import_json = {
        "roles": {
            "client": {
                f"{tenant_key}-forms-flow-web": [

                ],
                f"{tenant_key}-forms-flow-bpm": []
            }
        },
        "users": [
            {
                "username": f"service-account-{tenant_key}-forms-flow-bpm",
                "enabled": True,
                "totp": False,
                "emailVerified": False,
                "serviceAccountClientId": f"{tenant_key}-forms-flow-bpm",
                "disableableCredentialTypes": [],
                "requiredActions": [],
                "clientRoles": {
                    "realm-management": [
                        "view-users",
                        "query-users",
                        "manage-clients",
                        "query-clients",
                        "manage-users"
                    ],
                    f"{tenant_key}-forms-flow-web": [
                        "formsflow-reviewer",
                        "camunda-admin"
                    ]
                },
                "notBefore": 0,
                "groups": []
            }
        ],
        "clients": [
            {
                "clientId": f"{analytics_url}/saml/callback?org_slug={tenant_key}",
                "description": "Redash-Analytics",
                "adminUrl": f"{analytics_url}/saml/callback?org_slug={tenant_key}",
                "surrogateAuthRequired": False,
                "enabled": True,
                "alwaysDisplayInConsole": False,
                "clientAuthenticatorType": "client-secret",
                "redirectUris": [
                    f"{analytics_url}/*"
                ],
                "webOrigins": [],
                "notBefore": 0,
                "bearerOnly": False,
                "consentRequired": False,
                "standardFlowEnabled": True,
                "implicitFlowEnabled": False,
                "directAccessGrantsEnabled": True,
                "serviceAccountsEnabled": False,
                "publicClient": True,
                "frontchannelLogout": False,
                "protocol": "saml",
                "attributes": {
                    "saml.force.post.binding": "false",
                    "saml.multivalued.roles": "false",
                    "oauth2.device.authorization.grant.enabled": "false",
                    "backchannel.logout.revoke.offline.tokens": "false",
                    "saml.server.signature.keyinfo.ext": "false",
                    "use.refresh.tokens": "true",
                    "oidc.ciba.grant.enabled": "false",
                    "backchannel.logout.session.required": "false",
                    "saml.signature.algorithm": "RSA_SHA256",
                    "client_credentials.use_refresh_token": "false",
                    "saml.client.signature": "false",
                    "saml.assertion.signature": "true",
                    "id.token.as.detached.signature": "false",
                    "saml.encrypt": "false",
                    "saml.server.signature": "false",
                    "exclude.session.state.from.auth.response": "false",
                    "saml.artifact.binding.identifier": "45RbBjdYkPNyr7MamFji+v7T9n4=",
                    "saml.artifact.binding": "false",
                    "saml_force_name_id_format": "false",
                    "tls.client.certificate.bound.access.tokens": "false",
                    "saml.authnstatement": "true",
                    "display.on.consent.screen": "false",
                    "saml_name_id_format": "username",
                    "saml.onetimeuse.condition": "false",
                    "saml_signature_canonicalization_method": "http://www.w3.org/2001/10/xml-exc-c14n#WithComments"
                },
                "authenticationFlowBindingOverrides": {},
                "fullScopeAllowed": True,
                "nodeReRegistrationTimeout": -1,
                "protocolMappers": [
                    {
                        "name": "X500 surname",
                        "protocol": "saml",
                        "protocolMapper": "saml-user-property-mapper",
                        "consentRequired": False,
                        "config": {
                            "user.attribute": "lastName",
                            "friendly.name": "LastName",
                            "attribute.name": "LastName"
                        }
                    },
                    {
                        "name": "X500 givenName",
                        "protocol": "saml",
                        "protocolMapper": "saml-user-property-mapper",
                        "consentRequired": False,
                        "config": {
                            "user.attribute": "firstName",
                            "friendly.name": "FirstName",
                            "attribute.name": "FirstName"
                        }
                    }
                ],
                "defaultClientScopes": [
                    "role_list"
                ],
                "optionalClientScopes": []
            },
            {
                "clientId": f"{tenant_key}-forms-flow-bpm",
                "description": "Camunda Process Engine Components",
                "surrogateAuthRequired": False,
                "enabled": True,
                "alwaysDisplayInConsole": False,
                "clientAuthenticatorType": "client-secret",
                "secret": bpm_secret,
                "redirectUris": [
                    f"{camunda_url}/*"
                ],
                "webOrigins": [
                    "*"
                ],
                "notBefore": 0,
                "bearerOnly": False,
                "consentRequired": False,
                "standardFlowEnabled": True,
                "implicitFlowEnabled": False,
                "directAccessGrantsEnabled": True,
                "serviceAccountsEnabled": True,
                "publicClient": False,
                "frontchannelLogout": False,
                "protocol": "openid-connect",
                "attributes": {
                    "saml.assertion.signature": "false",
                    "id.token.as.detached.signature": "false",
                    "saml.force.post.binding": "false",
                    "saml.multivalued.roles": "false",
                    "saml.encrypt": "false",
                    "oauth2.device.authorization.grant.enabled": "false",
                    "saml.server.signature": "false",
                    "backchannel.logout.revoke.offline.tokens": "false",
                    "saml.server.signature.keyinfo.ext": "false",
                    "use.refresh.tokens": "true",
                    "exclude.session.state.from.auth.response": "false",
                    "oidc.ciba.grant.enabled": "false",
                    "saml.artifact.binding": "false",
                    "backchannel.logout.session.required": "false",
                    "client_credentials.use_refresh_token": "false",
                    "saml_force_name_id_format": "false",
                    "saml.client.signature": "false",
                    "tls.client.certificate.bound.access.tokens": "false",
                    "saml.authnstatement": "false",
                    "display.on.consent.screen": "false",
                    "saml.onetimeuse.condition": "false"
                },
                "authenticationFlowBindingOverrides": {},
                "fullScopeAllowed": True,
                "nodeReRegistrationTimeout": -1,
                "protocolMappers": [
                    {
                        "name": "client-roles-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usermodel-client-role-mapper",
                        "consentRequired": False,
                        "config": {
                            "multivalued": "true",
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "roles",
                            "usermodel.clientRoleMapping.clientId": f"{tenant_key}-forms-flow-web"
                        }
                    },
                    {
                        "name": "Client ID",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usersessionmodel-note-mapper",
                        "consentRequired": False,
                        "config": {
                            "user.session.note": "clientId",
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "clientId",
                            "jsonType.label": "String"
                        }
                    },
                    {
                        "name": "Client Host",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usersessionmodel-note-mapper",
                        "consentRequired": False,
                        "config": {
                            "user.session.note": "clientHost",
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "clientHost",
                            "jsonType.label": "String"
                        }
                    },
                    {
                        "name": "username",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usermodel-property-mapper",
                        "consentRequired": False,
                        "config": {
                            "userinfo.token.claim": "true",
                            "user.attribute": "username",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "preferred_username",
                            "jsonType.label": "String"
                        }
                    },
                    {
                        "name": "formsflow-bpm-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-audience-mapper",
                        "consentRequired": False,
                        "config": {
                            "included.client.audience": "forms-flow-bpm",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "included.custom.audience": "forms-flow-bpm",
                            "userinfo.token.claim": "false"
                        }
                    },
                    {
                        "name": "formsflow-web-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-audience-mapper",
                        "consentRequired": False,
                        "config": {
                            "included.client.audience": "forms-flow-web",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "included.custom.audience": "forms-flow-web",
                            "userinfo.token.claim": "false"
                        }
                    },
                    {
                        "name": "Client IP Address",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usersessionmodel-note-mapper",
                        "consentRequired": False,
                        "config": {
                            "user.session.note": "clientAddress",
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "clientAddress",
                            "jsonType.label": "String"
                        }
                    },
                    {
                        "name": "tenantKey mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-hardcoded-claim-mapper",
                        "consentRequired": False,
                        "config": {
                            "claim.value": tenant_key,
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "tenantKey",
                            "jsonType.label": "String",
                            "access.tokenResponse.claim": "false"
                        }
                    }
                ],
                "defaultClientScopes": [
                    "web-origins",
                    "roles",
                    "profile",
                    "email"
                ],
                "optionalClientScopes": [
                    "address",
                    "phone",
                    "offline_access",
                    "microprofile-jwt"
                ]
            },
            {
                "clientId": f"{tenant_key}-forms-flow-web",
                "description": "React based FormIO web components",
                "surrogateAuthRequired": False,
                "enabled": True,
                "alwaysDisplayInConsole": False,
                "clientAuthenticatorType": "client-secret",
                "redirectUris": [
                    f"{web_url}/*"
                ],
                "webOrigins": [
                    web_url
                ],
                "notBefore": 0,
                "bearerOnly": False,
                "consentRequired": False,
                "standardFlowEnabled": True,
                "implicitFlowEnabled": False,
                "directAccessGrantsEnabled": True,
                "serviceAccountsEnabled": False,
                "publicClient": True,
                "frontchannelLogout": False,
                "protocol": "openid-connect",
                "attributes": {
                    "saml.assertion.signature": "false",
                    "id.token.as.detached.signature": "false",
                    "saml.force.post.binding": "false",
                    "saml.multivalued.roles": "false",
                    "saml.encrypt": "false",
                    "oauth2.device.authorization.grant.enabled": "false",
                    "saml.server.signature": "false",
                    "backchannel.logout.revoke.offline.tokens": "false",
                    "saml.server.signature.keyinfo.ext": "false",
                    "use.refresh.tokens": "true",
                    "exclude.session.state.from.auth.response": "false",
                    "oidc.ciba.grant.enabled": "false",
                    "saml.artifact.binding": "false",
                    "backchannel.logout.session.required": "false",
                    "client_credentials.use_refresh_token": "false",
                    "saml_force_name_id_format": "false",
                    "saml.client.signature": "false",
                    "tls.client.certificate.bound.access.tokens": "false",
                    "saml.authnstatement": "false",
                    "display.on.consent.screen": "false",
                    "saml.onetimeuse.condition": "false"
                },
                "authenticationFlowBindingOverrides": {},
                "fullScopeAllowed": True,
                "nodeReRegistrationTimeout": -1,
                "protocolMappers": [
                    {
                        "name": "camunda-rest-api",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-audience-mapper",
                        "consentRequired": False,
                        "config": {
                            "included.client.audience": "forms-flow-bpm",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "included.custom.audience": "forms-flow-bpm",
                            "userinfo.token.claim": "false"
                        }
                    },
                    {
                        "name": "formsflow-web-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-audience-mapper",
                        "consentRequired": False,
                        "config": {
                            "included.client.audience": "forms-flow-web",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "included.custom.audience": "forms-flow-web",
                            "userinfo.token.claim": "false"
                        }
                    },
                    {
                        "name": "client-roles-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usermodel-client-role-mapper",
                        "consentRequired": False,
                        "config": {
                            "multivalued": "true",
                            "userinfo.token.claim": "true",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "claim.name": "roles",
                            "usermodel.clientRoleMapping.clientId": f"{tenant_key}-forms-flow-web"
                        }
                    },
                    {
                        "name": "tenantKey mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-hardcoded-claim-mapper",
                        "consentRequired": False,
                        "config": {
                            "claim.value": tenant_key,
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "tenantKey",
                            "jsonType.label": "String",
                            "access.tokenResponse.claim": "false"
                        }
                    }
                ],
                "defaultClientScopes": [
                    "web-origins",
                    "roles",
                    "profile",
                    "email"
                ],
                "optionalClientScopes": [
                    "address",
                    "phone",
                    "offline_access",
                    "microprofile-jwt"
                ]
            },
            {
                "clientId": f"{tenant_key}-forms-flow-admin-web",
                "description": "React based FormIO web components",
                "surrogateAuthRequired": False,
                "enabled": True,
                "alwaysDisplayInConsole": False,
                "clientAuthenticatorType": "client-secret",
                "redirectUris": [
                    f"{web_url}/*"
                ],
                "webOrigins": [
                    web_url
                ],
                "notBefore": 0,
                "bearerOnly": False,
                "consentRequired": False,
                "standardFlowEnabled": True,
                "implicitFlowEnabled": False,
                "directAccessGrantsEnabled": True,
                "serviceAccountsEnabled": False,
                "publicClient": True,
                "frontchannelLogout": False,
                "protocol": "openid-connect",
                "attributes": {
                    "saml.assertion.signature": "false",
                    "id.token.as.detached.signature": "false",
                    "saml.force.post.binding": "false",
                    "saml.multivalued.roles": "false",
                    "saml.encrypt": "false",
                    "oauth2.device.authorization.grant.enabled": "false",
                    "saml.server.signature": "false",
                    "backchannel.logout.revoke.offline.tokens": "false",
                    "saml.server.signature.keyinfo.ext": "false",
                    "use.refresh.tokens": "true",
                    "exclude.session.state.from.auth.response": "false",
                    "oidc.ciba.grant.enabled": "false",
                    "saml.artifact.binding": "false",
                    "backchannel.logout.session.required": "false",
                    "client_credentials.use_refresh_token": "false",
                    "saml_force_name_id_format": "false",
                    "saml.client.signature": "false",
                    "tls.client.certificate.bound.access.tokens": "false",
                    "saml.authnstatement": "false",
                    "display.on.consent.screen": "false",
                    "saml.onetimeuse.condition": "false"
                },
                "authenticationFlowBindingOverrides": {},
                "fullScopeAllowed": True,
                "nodeReRegistrationTimeout": -1,
                "protocolMappers": [
                    {
                        "name": "camunda-rest-api",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-audience-mapper",
                        "consentRequired": False,
                        "config": {
                            "included.client.audience": "forms-flow-bpm",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "included.custom.audience": "forms-flow-bpm",
                            "userinfo.token.claim": "false"
                        }
                    },
                    {
                        "name": "formsflow-web-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-audience-mapper",
                        "consentRequired": False,
                        "config": {
                            "included.client.audience": "forms-flow-web",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "included.custom.audience": "forms-flow-web",
                            "userinfo.token.claim": "false"
                        }
                    },
                    {
                        "name": "client-roles-mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-usermodel-client-role-mapper",
                        "consentRequired": False,
                        "config": {
                            "multivalued": "true",
                            "userinfo.token.claim": "true",
                            "id.token.claim": "false",
                            "access.token.claim": "true",
                            "claim.name": "roles",
                            "usermodel.clientRoleMapping.clientId": f"{tenant_key}-forms-flow-web"
                        }
                    },
                    {
                        "name": "tenantKey mapper",
                        "protocol": "openid-connect",
                        "protocolMapper": "oidc-hardcoded-claim-mapper",
                        "consentRequired": False,
                        "config": {
                            "claim.value": tenant_key,
                            "userinfo.token.claim": "true",
                            "id.token.claim": "true",
                            "access.token.claim": "true",
                            "claim.name": "tenantKey",
                            "jsonType.label": "String",
                            "access.tokenResponse.claim": "false"
                        }
                    }
                ],
                "defaultClientScopes": [
                    "web-origins",
                    "roles",
                    "profile",
                    "email"
                ],
                "optionalClientScopes": [
                    "address",
                    "phone",
                    "offline_access",
                    "microprofile-jwt"
                ]
            }
        ],
        "ifResourceExists": "FAIL"
    }
    custom_roles = []
    for role in roles:
        partial_import_json["roles"]["client"][f"{tenant_key}-forms-flow-web"].append(
            {
                "name": role['name'],
                "description": role.get('description', 'Formsflow custom role.'),
                "composite": False,
                "clientRole": True,
                "attributes": {}
            })
        if role['name'] not in (Role.REVIEWER.value, Role.DESIGNER.value, Role.CLIENT.value):
            custom_roles.append(role['name'])
    if create_default_users:
        for user in ('designer', 'client', 'reviewer'):
            # Add custom roles to reviewer user
            user_roles = [f"formsflow-{user}"]
            if user == 'reviewer' and roles:
                user_roles = [*user_roles, *custom_roles]
            partial_import_json["users"].append(_get_user_rep(tenant_key, user, user_roles))

        partial_import_json["users"].append(_get_user_rep(tenant_key, "admin", ["camunda-admin"]))
    return json.dumps(partial_import_json)


def _get_user_rep(tenant_key, username, roles):
    return {
        "username": f"{tenant_key}-{username}",
        "firstName": tenant_key.title(),
        "lastName": username.title(),
        "email": f"{username}@{tenant_key}.formsflow.ai",
        "enabled": True,
        "credentials": [
            {
                "value": current_app.config.get('TEMP_PASSWORD'),
                "type": "password"
            }
        ],
        "requiredActions": ["UPDATE_PASSWORD"],
        "clientRoles": {
            f"{tenant_key}-forms-flow-web": roles
        }
    }
