from http import HTTPStatus

from flask import current_app, request
from flask_restx import Resource
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, auth,
)

from formsflow_api.resources.payment.namespace import API
from formsflow_api.resources.payment.models import payment_status_model

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.schemas import PaymentSchema
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.services import AcstreService

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

            application_id = query_data.get("application_id")
            payment_status = response.get("status")
            if application_id:
                acstre_client = AcstreService()
                acstre_client.send_payment_status_update_message(
                    application_id=application_id,
                    payment_status=payment_status
                )

            return response, HTTPStatus.OK
        except EFormIntegrationException as err:
            response, status = {
                "type": "EForm IntegrationException",
                "message": err.message,
                "data": err.data
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