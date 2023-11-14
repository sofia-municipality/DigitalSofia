"""Resource for tenants endpoints."""
from http import HTTPStatus

from flask import jsonify, request
from flask_restx import Namespace, Resource, cors

from admin_api.exceptions import BusinessException
from admin_api.services.tenant import TenantService
from admin_api.utils.auth import jwt as _jwt
from admin_api.utils.util import cors_preflight

API = Namespace('tenants', description='Tenants')


@cors_preflight('GET,POST')
@API.route('', methods=['GET', 'POST', 'OPTIONS'])
class Tenants(Resource):
    """Endpoint resource for tenants."""

    @staticmethod
    @cors.crossdomain(origin='*')
    @_jwt.requires_auth
    def get():
        """Return all authorized tenants."""
        name: str = request.args.get('name', None)
        key: str = request.args.get('key', None)
        user_name: str = request.args.get('userName', None)
        page: int = int(request.args.get('page', '1'))
        limit: int = int(request.args.get('limit', '10'))

        return jsonify(TenantService().find_authorized_tenants(limit, page, key, name, user_name)), HTTPStatus.OK

    @staticmethod
    @cors.crossdomain(origin='*')
    @_jwt.requires_auth
    def post():
        """Return all authorized tenants.

        Request :
        {
            'key': 'Tenant key',
            'name': 'Tenant name',
            'details' : {
            'roles': [
                {
                    'name': 'Role name',
                    'description': 'Role Description'
                }
            ]
        }
        """
        try:
            TenantService().create_tenant(request.get_json())
            return jsonify({}), HTTPStatus.OK
        except BusinessException as e:
            return e.response()


@cors_preflight('GET,PUT')
@API.route('/<string:tenant_key>', methods=['GET', 'PUT', 'OPTIONS'])
class Tenant(Resource):
    """Endpoint resource for tenant."""

    @staticmethod
    @cors.crossdomain(origin='*')
    @_jwt.requires_auth
    def get(tenant_key: str):
        """Return tenant by key."""
        return jsonify(TenantService().find_tenant_by_key(tenant_key)), HTTPStatus.OK

    @staticmethod
    @cors.crossdomain(origin='*')
    @_jwt.requires_auth
    def put(tenant_key: str):
        """Update tenant by key.

        Update applicationTitle and Logo of the tenant.
        {
            "details": {
                "applicationTitle": "Test Title",
                "customLogo": {
                    "type": "url",
                    "logo": "http://test.svg"
                }
            }
        }
        """
        tenant = TenantService().update_tenant(tenant_key, request.get_json())
        return jsonify(tenant), HTTPStatus.OK
