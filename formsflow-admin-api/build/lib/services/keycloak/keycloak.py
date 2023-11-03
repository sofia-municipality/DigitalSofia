"""This exposes the Keycloak Admin APIs."""
import json
from typing import Dict, List

import requests
from flask import current_app

from admin_api.constants import HTTP_TIMEOUT

from . import get_import_json


class KeycloakService:
    """This class manages all the Keycloak service API calls."""

    def __init__(self):
        """Initializing the service."""
        config = current_app.config
        self._token_url = f'{config.get("KEYCLOAK_URL")}/auth/realms/' \
                          f'{config.get("KEYCLOAK_URL_REALM")}/protocol/openid-connect/token'
        self._create_client_url = f'{config.get("KEYCLOAK_URL")}/auth/admin/realms/' \
                                  f'{config.get("KEYCLOAK_URL_REALM")}/clients'
        self._import_url = f'{config.get("KEYCLOAK_URL")}/auth/admin/realms/' \
                           f'{config.get("KEYCLOAK_URL_REALM")}/partialImport'
        self._keycloak_admin_client = config.get('KEYCLOAK_ADMIN_CLIENT')
        self._keycloak_admin_secret = current_app.config.get('KEYCLOAK_ADMIN_SECRET')

    def get_admin_token(self) -> str:
        """Return admin token."""
        headers = {'Content-Type': 'application/x-www-form-urlencoded'}
        payload = {
            'client_id': self._keycloak_admin_client,
            'client_secret': self._keycloak_admin_secret,
            'grant_type': 'client_credentials',
        }

        response = requests.post(self._token_url, headers=headers, data=payload, timeout=HTTP_TIMEOUT)
        data = json.loads(response.text)
        assert (token := data.get('access_token', None))
        return token

    def create_tenant_clients(self, tenant_key: str, bpm_secret: str, tenant_roles: List[Dict[str, str]],
                              create_default_users: bool = False):
        """Method to create tenant clients for the tenant.

        : tenant_key: Tenant key identifier
        : tenant_roles: Additional roles for tenant on top of default roles
        """
        headers = {
            'Authorization': f'Bearer {self.get_admin_token()}',
            'Content-Type': 'application/json;charset=UTF-8',
            'Accept': 'application/json',
            'Origin': current_app.config.get("KEYCLOAK_URL")
        }

        payload = get_import_json(
            tenant_key=tenant_key,
            bpm_secret=bpm_secret,
            roles=tenant_roles,
            web_url=current_app.config.get('FORMSFLOW_WEB_URL'),
            camunda_url=current_app.config.get('FORMSFLOW_BPM_URL'),
            analytics_url=current_app.config.get('FORMSFLOW_INSIGHTS_URL'),
            create_default_users=create_default_users
        )
        response = requests.post(self._import_url, headers=headers, data=payload, timeout=HTTP_TIMEOUT)

        response.raise_for_status()

        return response.json()
