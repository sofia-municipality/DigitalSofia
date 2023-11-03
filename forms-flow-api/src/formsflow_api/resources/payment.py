"""API endpoints for processing applications resource."""

from http import HTTPStatus

from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth,
)

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.schemas import PaymentSchema
from formsflow_api.services.external import EFormIntegrationsService

API = Namespace("Payment", description="Payment")
payment_result_model = API.model(
    "PaymentResult",
    {
        "paymentId": fields.String(),
        "registrationTime": fields.Integer(),
        "accessCode": fields.String(),
    }
)
payment_status_model = API.model(
    "PaymentStatusResult",
    {
        "paymentId": fields.String(),
        "status": fields.String(),
        "changeTime": fields.Integer(),
    }
)


@cors_preflight("POST,OPTIONS")
@API.route("/<int:application_id>", methods=["POST", "OPTIONS"])
class PaymentResource(Resource):
    """Resource for creating payments."""

    @staticmethod
    @profiletime
    @auth.require
    @user_context
    @API.response(200, "CREATED:- Successful request.", model=payment_result_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post(application_id, **kwargs):
        try:
            client = EFormIntegrationsService()
            current_app.logger.info('Here Resource')
            response = client.create_payment(application_id, request.get_json())

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


@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class PaymentStatusResource(Resource):
    """Resource for getting payments status."""

    @staticmethod
    @profiletime
    @auth.require
    @API.response(200, "CREATED:- Successful request.", model=payment_status_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get():
        try:
            query_data = PaymentSchema().load(request.args) or {}
            client = EFormIntegrationsService()
            response = client.get_payment_status(query_data['payment_id'])

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
