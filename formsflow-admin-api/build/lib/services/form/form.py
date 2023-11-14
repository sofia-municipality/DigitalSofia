"""Form service."""
import json

import requests
from flask import current_app

from admin_api.constants import HTTP_TIMEOUT
from admin_api.models import RoleType, Tenant, TenantRole


class FormService:  # pylint:disable=too-few-public-methods
    """Form Service class."""

    def __init__(self):
        """Init object."""
        self._form_admin_login_endpoint = f'{current_app.config.get("FORMIO_URL")}/user/login'
        self._form_role_endpoint = f'{current_app.config.get("FORMIO_URL")}/role'
        self._user_endpoint = f'{current_app.config.get("FORMIO_URL")}/user'

    def _get_jwt_token(self):
        """Return JWT Token for form.io."""
        response = requests.post(self._form_admin_login_endpoint, headers={
            "Content-Type": "application/json"
        }, data=json.dumps({
            "data": {
                "email": current_app.config.get("FORMIO_USERNAME"),
                "password": current_app.config.get("FORMIO_PASSWORD")
            }
        }), timeout=HTTP_TIMEOUT)
        response.raise_for_status()
        return response.headers.get("x-jwt-token")

    def create_tenant_roles(self, tenant: Tenant):
        """Create tenant roles in form."""
        token = self._get_jwt_token()
        # Only one anonymous role is needed. Before creating role, check if one exists.
        if not TenantRole.find_anonymous_role():
            self._populate_anon_role(token)
        # Only one resource id is needed. Before creating, check if one exists.
        if not TenantRole.find_resource_id():
            self._populate_resource_id(token)

        # Create designer role
        self._create_tenant_role(RoleType.DESIGNER, tenant, token)
        # Create client role
        self._create_tenant_role(RoleType.CLIENT, tenant, token)
        # Create reviewer role
        self._create_tenant_role(RoleType.REVIEWER, tenant, token)

    def _create_tenant_role(self, role: RoleType, tenant: Tenant, token: str):
        """Create tenant role in form.io"""
        headers = {
            'x-jwt-token': f'{token}',
            'Content-Type': 'application/json',
        }

        role_payload = {
            'title': f'{tenant.key}{role.value.capitalize()}',
            'tenantKey': tenant.key,
            'description': f"{tenant.key}-{self._get_description(role)}",
            "admin": False,
            "designer": False,
            "default": False
        }
        if role == RoleType.DESIGNER:
            role_payload['designer'] = True
        response = requests.post(self._form_role_endpoint, headers=headers, data=json.dumps(role_payload),
                                 timeout=HTTP_TIMEOUT)

        response.raise_for_status()
        role_response = response.json()
        TenantRole(
            tenant=tenant,
            role_id=role_response.get('_id'),
            type=role
        ).flush()

    def _populate_anon_role(self, token: str):
        """Populate anonymous role from form.io"""
        headers = {
            'x-jwt-token': f'{token}',
            'Content-Type': 'application/json',
        }
        # Get all roles.
        response = requests.get(f"{self._form_role_endpoint}?machineName=anonymous", headers=headers,
                                timeout=HTTP_TIMEOUT)
        response.raise_for_status()
        role_response = response.json()
        TenantRole(
            tenant=None,
            role_id=role_response[0].get('_id'),
            type=RoleType.ANONYMOUS
        ).flush()

    def _populate_resource_id(self, token: str):
        """Populate Form.io resource id"""
        headers = {
            'x-jwt-token': f'{token}',
            'Content-Type': 'application/json',
        }
        # Get all roles.
        response = requests.get(f"{self._user_endpoint}", headers=headers, timeout=HTTP_TIMEOUT)
        response.raise_for_status()
        role_response = response.json()
        TenantRole(
            tenant=None,
            role_id=role_response.get('_id'),
            type=RoleType.RESOURCE_ID
        ).flush()

    @staticmethod
    def _get_description(role: RoleType):
        return {
            RoleType.DESIGNER: 'A person who has create  form access',
            RoleType.CLIENT: 'A person who has own form submission Access.',
            RoleType.REVIEWER: 'A person who has all Submission edit  access for the tenant.'
        }.get(role)
