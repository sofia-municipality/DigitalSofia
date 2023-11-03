"""Resource for current tenant endpoints."""
from http import HTTPStatus
from typing import Dict

import jwt
from flask import after_this_request, current_app, jsonify
from flask_restx import Namespace, Resource, cors

from admin_api.models import RoleType
from admin_api.services.tenant import TenantService
from admin_api.utils.auth import jwt as _jwt
from admin_api.utils.user_context import UserContext, user_context
from admin_api.utils.util import cors_preflight


API = Namespace('tenant', description='Current Tenant')


@cors_preflight('GET')
@API.route('', methods=['GET', 'OPTIONS'])
class CurrentTenant(Resource):
    """Endpoint resource for tenant."""

    @staticmethod
    @cors.crossdomain(origin='*')
    @_jwt.requires_auth
    def get():
        """Return current tenant."""

        @after_this_request
        @user_context
        def add_jwt_token_as_header(response, **kwargs):
            if response.status_code != HTTPStatus.OK:
                return response

            user: UserContext = kwargs["user"]
            _role_ids = [role['roleId'] for role in list(
                filter(lambda item: item["type"] != RoleType.RESOURCE_ID.value, response.json.get('form')))]
            _resource_id = next(
                role['roleId'] for role in response.json.get('form') if role['type'] == RoleType.RESOURCE_ID.value)

            unique_user_id = user.email or f"{user.user_name}@formsflow.ai"  # Email is not mandatory in keycloak
            project_id: str = current_app.config.get('FORMIO_PROJECT_URL')
            payload: Dict[str, any] = {
                "external": True,
                "form": {
                    "_id": _resource_id
                },
                "user": {
                    "_id": unique_user_id,
                    "roles": _role_ids
                },
                "tenantKey": user.tenant_key
            }
            if project_id:
                payload["project"] = {"_id": project_id}

            response.headers['x-jwt-token'] = jwt.encode(payload=payload,
                                                         key=current_app.config.get('FORMIO_JWT_SECRET'),
                                                         algorithm="HS256")
            response.headers["Access-Control-Expose-Headers"] = "x-jwt-token"
            return response

        return jsonify(TenantService().find_current_tenant()), HTTPStatus.OK
