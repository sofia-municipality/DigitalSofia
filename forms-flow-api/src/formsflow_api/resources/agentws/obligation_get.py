from http import HTTPStatus

from flask_restx import Resource
from formsflow_api_utils.utils.user_context import user_context
from formsflow_api_utils.utils import (
    cors_preflight,
    auth
)
from flask import current_app
from formsflow_api.resources.agentws.namespace import API

from formsflow_api.models.valid_debt_reg_id import VALID_DEBT_REG_ID
from formsflow_api.models.mateus_payment_group import MateusPaymentGroup
from formsflow_api.resources.assurance_level_decorator import require_assurance_level
from formsflow_api.schemas import MateusPaymentGroupWithPaymentsSchema, MateusPaymentRequestSchema
from formsflow_api.services import ObligationService
from formsflow_api.exceptions import EFormIntegrationException


@cors_preflight("GET,OPTIONS")
@API.route("/obligation/<int:group_id>", methods=["GET", "OPTIONS"])
class AgentWSObligationPaymentRequestsResource(Resource):

    @classmethod
    @auth.require
    @user_context
    @require_assurance_level("substantial,high")
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
    def get(cls, group_id, **kwargs):
        try:
            current_app.logger.info("Getting obligation group by id:: %s", group_id)

            client = ObligationService()
            record = MateusPaymentGroup.find_by_id(group_id)

            schema = MateusPaymentGroupWithPaymentsSchema()
            group = schema.dump(record, many=False)

            if group is None:
                response, status = {
                    "type": "Bad request error",
                    "key": "group_not_found",
                    "message": "Group not found",
                }, HTTPStatus.BAD_REQUEST
                return response, status

            if group.get("payments") is None or not group.get("payments"):
                response, status = {
                    "type": "Bad request error",
                    "key": "group_payments_not_found",
                    "message": "Group payments not found",
                }, HTTPStatus.BAD_REQUEST
                return response, status

            if group.get("status") != "Pending":
                result = {
                    "real_estate": [],
                    "vehicle": [],
                    "household_waste": [],
                }
                # Serialize the payments
                schema = MateusPaymentRequestSchema()
                payments = schema.dump(group["payments"], many=True)

                for payment in payments:
                    payment["status"] = group["status"]
                    result[VALID_DEBT_REG_ID[str(payment["kind_debt_reg_id"])]].append(payment)

                response = {
                    "status": group["status"],
                    "groups": result
                }

                return response, HTTPStatus.OK

            return {
                "status": group["status"],
                "groups": client.update_group_by_payment(group)
            }, HTTPStatus.OK
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
                "message": str(submission_err),
            }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
        return response, status
