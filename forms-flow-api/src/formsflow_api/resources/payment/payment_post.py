from http import HTTPStatus

from flask import current_app, request
from flask_restx import Resource
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth,
)

from formsflow_api.resources.payment.namespace import API
from formsflow_api.resources.payment.models import payment_result_model

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.services import AcstreService

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

            ### Search from generated files, by
            request_json = request.get_json()
            response = client.create_payment(application_id, request_json)

            ### TODO Set None
            payment_request = request_json.get("paymentRequest")
            payment_data = payment_request.get("paymentData") if payment_request else None

            current_app.logger.debug(payment_data)
            if payment_data:
                acstre_client = AcstreService()
                acstre_client.send_payment_generated_message_to_bpm(
                    application_id=application_id,
                    payment_access_code=response.get("accessCode"),
                    payment_access_code_deadline_date=payment_data.get("expirationDate"),
                    payment_sum=payment_data.get("amount")
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
