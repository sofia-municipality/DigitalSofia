from http import HTTPStatus
from math import fsum

import re
import uuid

from flask_restx import Resource, fields
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    cors_preflight,
    auth
)
from flask import current_app, request
from formsflow_api.resources.agentws.namespace import API

from formsflow_api.models import MateusPaymentRequest, db
from formsflow_api.models.mateus_payment_group import MateusPaymentGroup
from formsflow_api.resources.assurance_level_decorator import require_assurance_level
from formsflow_api.schemas import MateusPaymentRequestWithGroupSchema
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.services import PaymentValidationService
from formsflow_api.exceptions import EFormIntegrationException, CommonException
from formsflow_api.transformers import MateusPaymentRequestTransformer
from formsflow_api.utils import validate_person_identifier

payment_create_body = API.model(
    "PaymentCreateBody",
    {
        "payment_requests": fields.List(fields.Raw, required=True, min_items=1),
        "taxSubjectId": fields.Integer(required=True)
    },
)


@cors_preflight("POST,OPTIONS")
@API.route("/payment-request", methods=["POST", "OPTIONS"])
class AgentWSPaymentRequestResource(Resource):

    @classmethod
    @auth.require
    @user_context
    @require_assurance_level("substantial,high")
    @API.doc(body=payment_create_body)
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        400,
        "BAD_REQUEST - Invalid Personal Identifier bound to user."
    )
    @API.response(
        404,
        "NOT_FOUND - Cannot found subject by passed parameters"
    )
    @API.response(
        403,
        "Forbidden:- Request forbidden -- authorization will not help",
    )
    def post(cls, **kwargs):
        try:
            user: UserContext = kwargs["user"]

            # validate person identifier
            person_identifier = validate_person_identifier(user.token_info["personIdentifier"])

            # get obligation from request data
            obligations = request.get_json()

            if not obligations.get("payment_requests"):
                raise CommonException(
                    message="payment_requests is missing in request body or payment_requests is empty.",
                    key="payment_request_missing",
                )

            if not obligations.get("taxSubjectId"):
                raise CommonException(
                    message="taxSubjectId is missing in request body or taxSubjectId is not an integer.",
                    key="tax_subject_id_missing",
                )

            payment_validation_service = PaymentValidationService(obligations.get("payment_requests"),
                                                                  person_identifier)

            # validate incoming obligations
            payment_validation_service.validate_incoming_obligations()

            # create group payment id
            payment_id = str(uuid.uuid4())

            # Start payment transaction
            db.session.begin()

            # create payment group
            group = MateusPaymentGroup.create_from_dict({
                "person_identifier": person_identifier,
                "status": "New",
                "tax_subject_id": obligations["taxSubjectId"],
                "payment_id": payment_id,
            }).to_json()

            current_app.logger.info(group)

            # get obligations service
            client = EFormIntegrationsService()
            # get schema for payment request
            p_schema = MateusPaymentRequestWithGroupSchema()

            # create transformer for obligation before storing it
            obligation_transformer = MateusPaymentRequestTransformer()
            transformed_obligations = []

            # create lists for amount and reason for payment request
            payment_amount = []
            payment_reason = []

            # check if payment request already exists and if it is pending or paid then return error
            for p_request in obligations["payment_requests"]:
                amount = round(fsum([p_request["residual"], p_request["interest"]]), 2)
                reason = str(p_request["taxPeriodYear"]) + "-" + str(p_request["instNo"]) + "-" + str(
                    p_request["partidaNo"])
                payment = MateusPaymentRequest.find_by_reason(reason, p_request["kindDebtRegId"], amount)
                if payment is not None:
                    payment = p_schema.dump(payment, many=False)

                    if payment["group"]["status"].capitalize() == 'Pending' or payment["group"][
                        "status"].capitalize() == 'Paid' and not payment["group"].get("is_notified"):
                        current_app.logger.info(payment)

                        response, status = {
                            "type": "Bad request error",
                            "key": "payment_request_already_created",
                            "message": "Payment request already created",
                        }, HTTPStatus.BAD_REQUEST
                        return response, status

                # prepare payment request data for new group payment
                # calculate obligation amount and reason 
                obligation_amount = round(fsum([p_request["residual"], p_request["interest"]]), 2)
                obligation_reason = str(p_request["taxPeriodYear"]) + "-" + str(p_request["instNo"]) + "-" + str(
                    p_request["partidaNo"])

                # transform obligation
                transformed_obligation = obligation_transformer.transform(group["id"], p_request)

                # append transformed obligation, amount and reason
                transformed_obligations.append(transformed_obligation)
                payment_reason.append(obligation_reason)
                payment_amount.append(obligation_amount)

            current_app.logger.info(payment_amount)
            current_app.logger.info(payment_reason)

            response = client.create_payment_from_obligations(transformed_obligations, payment_amount, payment_reason,
                                                              group, payment_id, person_identifier, user)

            # commit payment transaction
            db.session.commit()

            return response, HTTPStatus.OK
        except EFormIntegrationException as err:
            db.session.rollback()

            response, status = {
                "type": "EForm IntegrationException",
                "message": err.message,
                "data": err.data
            }, err.error_code

            current_app.logger.warning(response)
            current_app.logger.warning(err)

            return response, status
        except CommonException as comm_err:
            response, status = comm_err.to_dict(), comm_err.code

            current_app.logger.info(response)
            current_app.logger.error(comm_err)

            return response, status
        except BaseException as submission_err:  # pylint: disable=broad-except
            db.session.rollback()

            response, status = {
                "type": "Bad request error",
                "message": str(submission_err),
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)

            return response, status
