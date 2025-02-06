import json
from http import HTTPStatus
import base64

from flask import current_app, request
from flask_restx import Resource
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime,
)
import hmac
import hashlib

from formsflow_api.resources.payment.namespace import API
from formsflow_api.resources.payment.models import payment_status_callback_model, payment_result_model

from formsflow_api.models import PaymentRequest, Application, MateusPaymentGroup, Region
from formsflow_api.services import AcstreService
from formsflow_api.utils import decode_secret
from formsflow_api.schemas import RegionSchema, PaymentSchema

from enum import Enum


class ClientPaymentType(Enum):
    PAYMENT_REQUEST = "PaymentRequest"
    MATEUS_PAYMENT_GROUP = "MateusPaymentGroup"


def verify_signature(secret_key, data, hmac_signature):
    signature = hmac.new(
        secret_key.encode("UTF-8"),
        msg=data.encode("UTF-8"),
        digestmod=hashlib.sha256
    )
    base64_bytes = base64.b64encode(signature.digest())
    base64_string = base64_bytes.decode("ascii")
    return base64_string == hmac_signature


def verify_payment(payment_id, client_payment_type, data):
    current_app.logger.info(f"Payment ID: {payment_id}, client_payment_type: {client_payment_type}, data: {data}")
    if client_payment_type == ClientPaymentType.PAYMENT_REQUEST:
        payment_data = PaymentRequest.get_by_payment_id(payment_id)
        schema = PaymentSchema()
        application_payment = schema.dump(payment_data, many=False)

        current_app.logger.info(application_payment)

        if application_payment:
            current_app.logger.info("Payment found in PaymentRequest")
            acstre_client = AcstreService()
            acstre_client.send_payment_status_update_message(
                application_id=application_payment.get("applicationId"),
                payment_status=data.get("Status").lower()
            )

            return {"success": True}, HTTPStatus.OK
    elif client_payment_type == ClientPaymentType.MATEUS_PAYMENT_GROUP:
        payment = MateusPaymentGroup.get_by_payment_id(payment_id)

        current_app.logger.info(payment)

        if payment:
            current_app.logger.info("Payment found in MateusPaymentGroup")
            return {"success": True}, HTTPStatus.OK

    return {
        "type": "Bad request error",
        "message": "Invalid payment Id",
        "success": False
    }, HTTPStatus.BAD_REQUEST

def check_for_eservice_client_id(eservice_client_id):
    region_entity = Region.get_by_client_id(eservice_client_id)
    if region_entity:
        region_schema = RegionSchema()
        region = region_schema.dump(region_entity, many=False)
        secret = region.get("secret_key")
        key = current_app.config.get("REGIONS_SECRET_KEY")
        decoded_secret_key = decode_secret(encoded_secret=secret, key=key)

        return True, ClientPaymentType.PAYMENT_REQUEST, decoded_secret_key
    if eservice_client_id == current_app.config.get("MATEUS_PAYMENT_CLIENT_ID"):
        return True, ClientPaymentType.MATEUS_PAYMENT_GROUP, current_app.config.get("MATEUS_PAYMENT_CLIENT_SECRET")
    return None, None, None


def verify_client_and_signature(request_form):
    client_id = request_form["ClientId"]
    data = request_form["Data"]
    hmac_signature = request_form["Hmac"]

    client_exists, client_payment_type, secret_key = check_for_eservice_client_id(client_id)

    current_app.logger.debug(f"Client exists: {client_exists}, client_payment_type: {client_payment_type}")
    if not client_exists:
        current_app.logger.warning("Invalid eServiceClientId")
        return {
            "type": "Bad request error",
            "message": "Invalid eServiceClientId",
            "success": False
        }, HTTPStatus.BAD_REQUEST, None

    if not verify_signature(secret_key, data, hmac_signature):
        current_app.logger.warning("Invalid signature")
        return {
            "type": "Permission Denied",
            "message": f"Invalid signature for client {client_id}",
            "success": False
        }, HTTPStatus.FORBIDDEN, None

    current_app.logger.info("Signature verified")
    return None, None, client_payment_type


def decode_and_parse_data(data):
    decoded_data = base64.b64decode(data).decode("utf-8")
    return json.loads(decoded_data)


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

            request_form = request.form

            current_app.logger.info(f"Query params: {request.args}")
            current_app.logger.info(f"Request form: {request_form}")
            current_app.logger.info(request_form.keys())

            error, status, client_payment_type = verify_client_and_signature(request_form)

            current_app.logger.debug(f"Error response: {error}, Response status: {status}, client_payment_type: {client_payment_type}")
            # If the client and signature are not verified, return the error
            if error:
                return error, status

            # Decode and parse the data if the client and signature are verified
            data = decode_and_parse_data(request_form["Data"])
            payment_id = data.get("Id")

            current_app.logger.debug(
                f"payment_id: {payment_id}, client_payment_type: {client_payment_type}, data: {data}")
            return verify_payment(payment_id, client_payment_type, data)
        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                "type": "Bad request error",
                "message": "Invalid submission request passed",
                "success": False
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.error(f"Exception occurred: {submission_err}")
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status
        except Exception as e:
            current_app.logger.error(f"Exception occurred: {e}")
