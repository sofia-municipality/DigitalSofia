"""API endpoints for processing applications resource."""
import json
from http import HTTPStatus
import base64

from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth,
)
import hmac
import hashlib

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.models import PaymentRequest, Application
from formsflow_api.schemas import PaymentSchema, PaymentCancelledResolveSchema
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.services import AcstreService
from marshmallow import ValidationError

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

payment_status_callback_model = API.model(
    "PaymentStatusCallback",
    {
        "clientId": fields.String(),
        "hmac": fields.String(),
        "data": fields.String(),
    },
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


@cors_preflight("POST,OPTIONS")
@API.route("/<int:application_id>/payment-cancelled-handler", methods=["POST", "OPTIONS"])
class PaymentCancelledHandlerResource(Resource):
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
        ### Verify attribute is passed
        try:
            application_json = request.get_json()
            payment_cancelled_resolve_schema = PaymentCancelledResolveSchema()
            data = payment_cancelled_resolve_schema.load(application_json)

            status = data.get("status")

            if status:
                is_paid = True if status == "paid" else False
                acstre_client = AcstreService()
                acstre_client.handle_payment_status_change(
                    application_id=application_id, 
                    is_paid=is_paid,
                    is_final=True
                )

            return {
                "is_paid": is_paid
            }, HTTPStatus.OK
        except ValidationError as err:
                return (
                    {
                        "type": "Bad request error",
                        "message": "Unprocessable Entity",
                        "data": err.messages
                    },
                    HTTPStatus.UNPROCESSABLE_ENTITY
                )

@cors_preflight("POST,OPTIONS")
@API.route("/payment-status-callback", methods=["POST", "OPTIONS"])
class PaymentStatusCallbackResource(Resource):
    """Resource for creating payments."""

    @staticmethod
    @profiletime
    @API.doc(body=payment_status_callback_model)
    @API.response(200, "CREATED:- Successful request.", model=payment_result_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        403,
        "Forbidden:- Request forbidden -- authorization will not help",
    )
    def post(**kwargs):
        try:
            current_app.logger.info("PaymentStatusCallbackResource@post")
            query_params = request.args
            request_form = request.form
            current_app.logger.info(request.form.keys())
            # Check if data was signed with our secret key

            signature = hmac.new(
                current_app.config.get("EPAYMENT_SECRET_KEY").encode("UTF-8"),
                msg=request_form["data"].encode("UTF-8"),
                digestmod=hashlib.sha256
            )
            base64_bytes = base64.b64encode(signature.digest())
            base64_string = base64_bytes.decode("ascii")
            if base64_string != request_form["hmac"]:
                return {
                    "type": "Permission Denied",
                    "message": f"Invalid signature",
                }, HTTPStatus.FORBIDDEN

            # Check if payment_id exists in our database
            payment_id = query_params.get("fieldId")
            payment_request = PaymentRequest.get_by_payment_id(payment_id)
            if payment_request:
                payment_request = payment_request.to_json()
            else:
                return {
                    "type": "Bad request error",
                    "message": "Invalid submission request passed",
                }, HTTPStatus.BAD_REQUEST

            #decode status from data and sent it to process
            data = base64.b64decode(request_form["data"]).decode("utf-8")
            data = json.loads(data)

            # Sent new status to original application
            acstre_client = AcstreService()
            acstre_client.send_payment_status_update_message(
                application_id=payment_request["application_id"], 
                payment_status=data.get("status")
            )

            return payment_request, HTTPStatus.OK

        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status