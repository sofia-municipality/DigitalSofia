"""BPM service."""
import json
from typing import List

import requests
from flask import current_app

from admin_api.constants import HTTP_TIMEOUT
from admin_api.services.keycloak.keycloak import KeycloakService


class BpmService:  # pylint:disable=too-few-public-methods
    """BPM Service class."""

    def __init__(self):
        """Init object."""
        self._tenant_create_url = f'{current_app.config.get("FORMSFLOW_BPM_URL")}' \
                                  '/engine-rest-ext/v1/admin/tenant/authorization'
        self._kc = KeycloakService()

    def create_tenant(self, tenant_id: str, roles: List[str]):
        """Create tenant related details in BPM."""
        headers = {
            'Authorization': f'Bearer {self._kc.get_admin_token()}',
            'Content-Type': 'application/json',
        }
        bpm_payload = {
            'tenantKey': tenant_id,
            'adminRoles': ['camunda-admin'],
            'designerRoles': ['formsflow-designer'],
            'clientRoles': ['formsflow-client'],
            'reviewerRoles': ['formsflow-reviewer']
        }
        for role in roles:
            if role['name'] not in ('camunda-admin', 'formsflow-designer', 'formsflow-client') \
                    and role['name'] not in bpm_payload['reviewerRoles']:
                bpm_payload['reviewerRoles'].append(role['name'])
        payload = json.dumps(bpm_payload)
        response = requests.post(self._tenant_create_url, headers=headers, data=payload, timeout=HTTP_TIMEOUT)
        response.raise_for_status()
