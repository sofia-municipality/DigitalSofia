"""Exposes all of the resource endpoints mounted in Flask-Blueprint style.

Uses restplus namespaces to mount individual api endpoints into the service.

All services have 2 defaults sets of endpoints:
 - ops
 - meta
That are used to expose operational health information about the service, and meta information.
"""
from importlib.resources import path

from flask import Blueprint

from .apihelper import Api
from .meta import API as META_API
from .ops import API as OPS_API
from .tenant import API as TENANTS_API
from .current_tenant import API as TENANT_API


__all__ = ('API_BLUEPRINT', 'OPS_BLUEPRINT')

# This will add the Authorize button to the swagger docs
AUTHORIZATIONS = {'apikey': {'type': 'apiKey', 'in': 'header', 'name': 'Authorization'}}

OPS_BLUEPRINT = Blueprint('API_OPS', __name__, url_prefix='/ops')

API_OPS = Api(
    OPS_BLUEPRINT,
    title='Service OPS API',
    version='1.0',
    description='The Core API for the Reports System',
    security=['apikey'],
    authorizations=AUTHORIZATIONS,
)

API_OPS.add_namespace(OPS_API, path='/')

API_BLUEPRINT = Blueprint('API', __name__, url_prefix='/api/v1')

API = Api(
    API_BLUEPRINT,
    title='Formsflow Admin API',
    version='1.0',
    description='The Core API for the formsflow Admin',
    security=['apikey'],
    authorizations=AUTHORIZATIONS,
)

API.add_namespace(META_API, path='/meta')
API.add_namespace(TENANTS_API, path='/tenants')
API.add_namespace(TENANT_API, path='/tenant')
