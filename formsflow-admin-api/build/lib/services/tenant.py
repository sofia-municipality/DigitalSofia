"""Tenant service."""
from datetime import datetime, timedelta
from typing import Dict, List

from flask import abort, current_app
from sqlalchemy.orm.attributes import flag_modified

from admin_api.constants import Role
from admin_api.exceptions import BusinessException, Error
from admin_api.models import ClientType, RoleType, Tenant, TenantAdmin, TenantClient, TenantRole, TenantStatus
from admin_api.services import AnalyticsService, BpmService, FormService, KeycloakService
from admin_api.utils.user_context import UserContext, user_context


class TenantService:
    """Tenant service class."""

    def __init__(self, tenant_key: str = None):
        """Init object."""
        self.tenant_key = tenant_key
        self._kc = KeycloakService()
        self._bpm = BpmService()
        self._analytics = AnalyticsService()
        self._form = FormService()

    @user_context
    def find_authorized_tenants(self, limit, page, key, name,  # pylint:disable=too-many-arguments
                                user_name, **kwargs) -> Dict[str, any]:
        """Return authorized tenants."""
        user: UserContext = kwargs['user']
        key: str = user.tenant_key or key or None
        tenants, total = Tenant.find_tenants(limit, page, key, name, user_name)
        result = {
            'total': total,
            'page': page,
            'limit': limit,
            'tenants': []
        }

        for tenant in tenants:
            result['tenants'].append(tenant.as_dict())

        return result

    @staticmethod
    @user_context
    def find_tenant_by_key(tenant_key, **kwargs) -> Dict[str, any]:
        """Return tenant by key."""
        user: UserContext = kwargs['user']
        if tenant_key != user.tenant_key:
            abort(403)
        return Tenant.find_by_key(tenant_key).as_dict()

    @staticmethod
    @user_context
    def find_current_tenant(**kwargs) -> Dict[str, any]:
        """Return tenant by key."""
        user: UserContext = kwargs['user']
        tenant: Tenant = Tenant.find_by_key(user.tenant_key)
        if not tenant:
            abort(403)
        role_type = RoleType.DESIGNER if user.has_role(Role.DESIGNER.value) else (
            RoleType.REVIEWER if user.has_role(Role.REVIEWER.value) else RoleType.CLIENT)
        tenant_roles: List[TenantRole] = TenantRole.find_by_tenant_id_and_type(tenant.id, role_type)
        response = tenant.as_dict()
        response['form'] = []
        for tenant_role in tenant_roles:
            response['form'].append({'roleId': tenant_role.role_id, 'type': tenant_role.type.value})

        return response

    def create_tenant(self, payload):
        """Create a tenant record in all related components."""
        # Create a tenant model with it's details.
        tenant_key = payload['key']
        tenant_name = payload['name']
        expiry_dt: datetime = datetime.now() + timedelta(days=current_app.config.get('TRIAL_PERIOD')) \
            if payload.get('trial', False) \
            else None

        current_app.logger.debug("Creating tenant with Key : %s", tenant_key)
        self._populate_default_roles(payload)

        # first check for duplicates
        existing_tenant = Tenant.find_by_key_or_name(tenant_key, tenant_name)
        if existing_tenant:
            raise BusinessException(Error.DUPLICATE_TENANT_KEY)

        tenant = Tenant(
            key=tenant_key,
            name=payload['name'],
            details=payload.get('details'),
            status=TenantStatus.PENDING.value,
            expiry_dt=expiry_dt
        ).flush()

        # Web client
        current_app.logger.debug("Save web client details")
        self._create_tenant_client(tenant, ClientType.WEB)
        # Analytics client
        current_app.logger.debug("Save Analytics client details")
        self._create_tenant_client(tenant, ClientType.ANALYTICS)
        # Bpm client
        current_app.logger.debug("Save BPM client details")
        bpm_client = self._create_tenant_client(tenant, ClientType.BPM)

        # Create BPM records
        try:
            current_app.logger.debug("Create BPM authorizations")
            self._bpm.create_tenant(tenant_id=tenant.key, roles=payload['details']['roles'])
        except Exception as e:
            current_app.logger.error(e)
            raise BusinessException(Error.INVALID_BPM_AUTHORIZATION) from e

        skip_analytics: bool = payload['details'].get('skipAnalytics', False)
        if not skip_analytics:
            # Create Analytics records
            current_app.logger.debug("Create Analytics records")
            admin_user_name = f'{tenant_key}-admin'
            email = f"admin@{tenant_key}"
            try:
                self._analytics.create_org(tenant_key, tenant.name, admin_user_name, email)
            except Exception as e:
                current_app.logger.error(e)
                raise BusinessException(Error.INVALID_ANALYTICS_ORG) from e

        # Create Form.io records
        self._form.create_tenant_roles(tenant)

        # Create Keycloak records
        current_app.logger.debug("Create Keycloak records")
        create_default_users: bool = payload['details'].get('createDefaultUsers', False)
        if not payload['details'].get('skipKeycloakSteps', False):
            try:
                self._kc.create_tenant_clients(tenant_key=tenant_key, bpm_secret=bpm_client.secret,
                                               tenant_roles=payload['details']['roles'],
                                               create_default_users=create_default_users)
            except Exception as e:
                current_app.logger.error(e)
                raise BusinessException(Error.INVALID_KEYCLOAK_REQUEST) from e

            if create_default_users:
                tenant_admin: TenantAdmin = TenantAdmin(user_name=f'{tenant_key}-admin', tenant_id=tenant.id)
                tenant_admin.flush()

        # Change tenant status to ACTIVE
        tenant.status = TenantStatus.ACTIVE.value
        tenant.save()

    @staticmethod
    def _populate_default_roles(payload):
        if not payload.get('details'):
            payload['details']['roles'] = []
        payload['details']['roles'].append(
            {
                'name': 'formsflow-reviewer',
                'description': 'Provides access to use the formsflow.ai solution. '
                               'Identifies the staff to work on applications and forms submissions.'
            })
        payload['details']['roles'].append(
            {
                'name': 'formsflow-designer',
                'description': 'Provides access to use the formsflow.ai solution. '
                               'Access to wok on form designer studio.'
            })
        payload['details']['roles'].append(
            {
                'name': 'formsflow-client',
                'description': 'Provides access to use the formsflow.ai solution. '
                               'Required to access and submit forms'
            })
        payload['details']['roles'].append(
            {
                'name': 'camunda-admin',
                'description': 'Camunda administrator for the tenant.'
            })

    @staticmethod
    def _create_tenant_client(tenant: Tenant, client_type: ClientType) -> TenantClient:
        secret = current_app.config.get('BPM_CLIENT_SECRET') if client_type == ClientType.BPM else None
        tenant_client: TenantClient = TenantClient(
            tenant_id=tenant.id,
            client_id=f'{tenant.key}-forms-flow-{client_type.value.lower()}',
            client_type=client_type,
            secret=secret
        ).flush()
        return tenant_client

    @staticmethod
    @user_context
    def update_tenant(tenant_key, payload, **kwargs):
        """Update tenant by key."""
        user: UserContext = kwargs['user']
        if tenant_key != user.tenant_key:
            abort(403)
        tenant = Tenant.find_by_key(tenant_key)
        if tenant:
            # Update the `applicationTitle` if it exists in the payload
            if "applicationTitle" in payload["details"]:
                tenant.details["applicationTitle"] = payload["details"]["applicationTitle"]
            # Update the `customLogo` if it exists in the payload
            if "customLogo" in payload["details"]:
                tenant.details["customLogo"] = payload["details"]["customLogo"]
            # Flag the `details` attribute as modified
            flag_modified(tenant, "details")
            tenant.save()
            return tenant.as_dict()
        return None
