"""API endpoints for processing applications resource."""

from http import HTTPStatus

from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth,
)

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.services.external import EFormIntegrationsService

API = Namespace("EDelivery", description="EDelivery")

edelivery_response_model = API.model(
    "EDeliveryResponseModel",
    {
        "SendMessageOnBehalfToPersonResult": fields.Integer(),
    }
)


@cors_preflight("POST,OPTIONS")
@API.route("", methods=["POST", "OPTIONS"])
class EDeliveryResource(Resource):
    """Resource for edelivery."""

    @staticmethod
    @profiletime
    @auth.require
    @user_context
    @API.response(200, "CREATED:- Successful request.", model=edelivery_response_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post(**kwargs):
        try:
            client = EFormIntegrationsService()
            response = client.sent_to_eDelivery(request.get_json())

            return response, HTTPStatus.OK
        except EFormIntegrationException as err:
            response, status = {
                                   "type": "EForm IntegrationException",
                                   "message": err.message
                               }, err.error_code
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status