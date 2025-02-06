from http import HTTPStatus

from flask import request
from flask_restx import Resource
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth,
)

from formsflow_api.resources.payment.namespace import API
from formsflow_api.resources.payment.models import payment_result_model

from formsflow_api.schemas import PaymentCancelledResolveSchema
from formsflow_api.services import AcstreService
from marshmallow import ValidationError

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