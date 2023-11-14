"""BPM service."""
import json

import requests
from flask import current_app

from admin_api.constants import HTTP_TIMEOUT


class AnalyticsService:  # pylint:disable=too-few-public-methods
    """BPM Service class."""

    def __init__(self):
        """Init object."""
        self._org_create_url = f'{current_app.config.get("FORMSFLOW_INSIGHTS_URL")}/api/organization'

    def create_org(self, tenant_key: str, name: str, username: str, email: str):
        """Create org related details in Analytics."""
        analytics_admin_token = current_app.config.get("FORMSFLOW_INSIGHTS_API_KEY")
        headers = {
            "Authorization": analytics_admin_token,
            "Content-Type": "application/json"
        }
        config = current_app.config
        saml_descriptor = f'{config.get("KEYCLOAK_URL")}/auth/realms/' \
                          f'{config.get("KEYCLOAK_URL_REALM")}/protocol/saml/descriptor'
        org_payload = {
            'slug': tenant_key,
            'name': name,
            'samlDescriptor': saml_descriptor,
            'userName': username,
            'email': email,
            'password': current_app.config.get('TEMP_PASSWORD'),
            'userApiKey': analytics_admin_token
        }
        payload = json.dumps(org_payload)
        response = requests.post(self._org_create_url, headers=headers, data=payload, timeout=HTTP_TIMEOUT)
        response.raise_for_status()
