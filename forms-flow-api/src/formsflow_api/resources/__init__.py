"""Exposes all of the resource endpoints mounted in Flask-Blueprint style.

Uses restx namespaces to mount individual api endpoints into the service.
"""

from flask_jwt_oidc import AuthError
from flask_restx import Api
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils.constants import ALLOW_ALL_ORIGINS

from formsflow_api.resources.anonymous_application import API as PUBLIC_API
from formsflow_api.resources.application import API as APPLICATION_API
from formsflow_api.resources.application_history import (
    API as APPLICATION_HISTORY_API,
)
from formsflow_api.resources.authorization import API as AUTHORIZATION_API
from formsflow_api.resources.checkpoint import API as CHECKPOINT_API
from formsflow_api.resources.dashboards import API as DASHBOARDS_API
from formsflow_api.resources.documents import API as DOCUMENTS_API
from formsflow_api.resources.draft import API as DRAFT_API
from formsflow_api.resources.faq import API as FAQ_API
from formsflow_api.resources.filter import API as FILTER_API
from formsflow_api.resources.form_embed import API as FORM_EMBED_API
from formsflow_api.resources.form_process_mapper import API as FORM_API
from formsflow_api.resources.formio import API as FORMIO_API
from formsflow_api.resources.groups import API as KEYCLOAK_GROUPS_API
from formsflow_api.resources.metrics import API as APPLICATION_METRICS_API
from formsflow_api.resources.process import API as PROCESS_API
from formsflow_api.resources.roles import API as KEYCLOAK_ROLES_API
from formsflow_api.resources.user import API as KEYCLOAK_USER_API
from formsflow_api.resources.page_block import API as PAGE_BLOCK_API
from formsflow_api.resources.translations import API as TRANSLATIONS_API
from formsflow_api.resources.regix import API as REGIX_API
from formsflow_api.resources.logs import API as LOGS_API
from formsflow_api.resources.eurotrust import API as EUROTRUST_API
from formsflow_api.resources.address import API as ADDRESSES_API
from formsflow_api.resources.services import API as SERVICES_API
from formsflow_api.resources.kep import API as KEP_API
from formsflow_api.resources.agentws import API as AGENTWS_API
from formsflow_api.resources.application_processing import API as APPLICATION_PROCESSING_API
from formsflow_api.resources.payment import API as PAYMENT_API
from formsflow_api.resources.eDelivery import API as EDELIVERY_API
from formsflow_api.resources.external_services import API as EXTERNAL_SERVICES
# from formsflow_api.resources.firebase import API as FIREBASE_API

# This will add the Authorize button to the swagger docs
# oauth2 & openid may not yet be supported by restplus
AUTHORIZATIONS = {"apikey": {"type": "apiKey", "in": "header", "name": "Authorization"}}

API = Api(
    title="formsflow.ai API",
    version="1.0",
    description="The API for formsflow.ai. Checkout: formsflow.ai to know more",
    security=["apikey"],
    authorizations=AUTHORIZATIONS,
    doc="/",
)


@API.errorhandler(BusinessException)
def handle_business_exception(error: BusinessException):
    """Handle Business exception."""
    return (
        {"message": error.error},
        error.status_code,
        {"Access-Control-Allow-Origin": ALLOW_ALL_ORIGINS},
    )


@API.errorhandler(AuthError)
def handle_auth_error(error: AuthError):
    """Handle Auth exception."""
    return (
        {
            "type": "Invalid Token Error",
            "message": "Access to API Denied. Check if the "
            "bearer token is passed for Authorization or has expired.",
        },
        error.status_code,
        {"Access-Control-Allow-Origin": ALLOW_ALL_ORIGINS},
    )


API.add_namespace(APPLICATION_API, path="/application")
API.add_namespace(APPLICATION_HISTORY_API, path="/application")
API.add_namespace(APPLICATION_METRICS_API, path="/metrics")
API.add_namespace(CHECKPOINT_API, path="/checkpoint")
API.add_namespace(DASHBOARDS_API, path="/dashboards")
API.add_namespace(FORM_API, path="/form")
API.add_namespace(KEYCLOAK_GROUPS_API, path="/groups")
API.add_namespace(PROCESS_API, path="/process")
API.add_namespace(PUBLIC_API, path="/public")
API.add_namespace(KEYCLOAK_USER_API, path="/user")
API.add_namespace(DRAFT_API, path="/draft")
API.add_namespace(FORMIO_API, path="/formio")
API.add_namespace(AUTHORIZATION_API, path="/authorizations")
API.add_namespace(FILTER_API, path="/filter")
API.add_namespace(KEYCLOAK_ROLES_API, path="/roles")
API.add_namespace(FORM_EMBED_API, path="/embed")
API.add_namespace(PAGE_BLOCK_API, path="/page-blocks")
API.add_namespace(FAQ_API, path="/faq")
API.add_namespace(TRANSLATIONS_API, path="/translations")
API.add_namespace(REGIX_API, path="/regix")
API.add_namespace(EUROTRUST_API, path="/eurotrust")
API.add_namespace(DOCUMENTS_API, path="/documents")
API.add_namespace(ADDRESSES_API, path="/addresses")
API.add_namespace(SERVICES_API, path="/services")
API.add_namespace(KEP_API, path="/signature")
API.add_namespace(AGENTWS_API, path="/agentws")
API.add_namespace(APPLICATION_PROCESSING_API, path="/application-processing")
API.add_namespace(PAYMENT_API, path="/payment")
API.add_namespace(EDELIVERY_API, path="/edelivery")
API.add_namespace(EXTERNAL_SERVICES, path="/external-services")
API.add_namespace(LOGS_API, path="/logs")
# API.add_namespace(FIREBASE_API, path="/firebase")
