from http import HTTPStatus

import re

from flask_restx import Resource
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
)
from flask import current_app, request
from formsflow_api.resources.agentws.namespace import API

from formsflow_api.models.valid_debt_reg_id import VALID_DEBT_REG_ID
from formsflow_api.resources.assurance_level_decorator import require_assurance_level
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.schemas import MateusPaymentRequestWithGroupSchema
from formsflow_api.models import MateusPaymentRequest
from math import fsum


@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class AgentWSResource(Resource):

    @classmethod
    @auth.require
    @user_context
    @require_assurance_level("substantial,high")
    @API.doc(
        params={
            "limit": {
                "in": "query",
                "description": "Number of obligations",
                "default": "1",
            },
        }
    )
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
    def get(cls, **kwargs):
        try:
            user: UserContext = kwargs["user"]
            limit = request.args.get('limit', None)
            person_identifier = user.token_info["personIdentifier"]

            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            person_identifier = match[0]
            client = EFormIntegrationsService()

            response = client.get_obligations(person_identifier, limit)

            obligations = response.get('obligations', [])

            current_app.logger.info(f"Obligations for {person_identifier}: {obligations}")

            if obligations:
                # Format obligations
                formated_obligations = {
                    'total': 0,
                    'data': {
                    }
                }
                current_app.logger.debug("Incoming obligations from Mateus")
                schema = MateusPaymentRequestWithGroupSchema()

                for obligation in obligations:
                    debt_type_id = obligation.get('kindDebtRegId', None)
                    if not obligation:
                        current_app.logger.critical("Obligation doesn't have kindDebtRegId key")
                        current_app.logger.critical(obligation.get('debtInstalmentId'))
                        continue

                    # if payment request has already been generated for a obligation
                    amount = round(fsum([obligation["residual"], obligation["interest"]]), 2)
                    reason = str(obligation["taxPeriodYear"]) + "-" + str(obligation["instNo"]) + "-" + str(
                        obligation["partidaNo"])
                    payment = MateusPaymentRequest.find_by_reason(reason, debt_type_id, amount)
                    payment = schema.dump(payment, many=False)

                    if payment:
                        # Check if payment exists in our system,
                        # is in pending or paid status, and mateus has not been notified
                        is_payment_eligible_for_notification = (
                                "group" in payment and
                                payment["group"].get("status") in ["Pending", "Paid"] and
                                not payment["group"].get("is_notified", False)
                        )

                        if is_payment_eligible_for_notification:
                            current_app.logger.debug(
                                "Payment request is pending or paid but mateus is not notified for obligation %s",
                                obligation.get('debtInstalmentId'))
                            obligation["status"] = payment["group"]["status"]
                            obligation["hasPaymentRequest"] = True

                    valid_debt_type = VALID_DEBT_REG_ID.get(debt_type_id, None)
                    interest = obligation.get('interest', 0)
                    residual = obligation.get('residual', 0)
                    obligation_cost = round(fsum([interest, residual]), 2)

                    # Check if it is a debt type we must handle
                    if valid_debt_type and obligation_cost > 0:
                        # Check if we already have it
                        if valid_debt_type not in formated_obligations['data']:
                            formated_obligations['data'][valid_debt_type] = {
                                'total': 0,
                                'data': {}
                            }

                        # Check if  partidaNo is already added
                        batch_id = obligation.get('partidaNo')
                        if batch_id not in formated_obligations['data'][valid_debt_type]['data']:
                            formated_obligations['data'][valid_debt_type]['data'][batch_id] = {
                                'total': 0,
                                'data': []
                            }

                        # Add item to correct end node
                        formated_obligations['data'][valid_debt_type]['data'][batch_id]['data'].append(obligation)

                        # Add totals
                        formated_obligations['total'] = round(
                            fsum(
                                (
                                    obligation_cost,
                                    formated_obligations['total']
                                )
                            ),
                            2)
                        formated_obligations['data'][valid_debt_type]['total'] = round(
                            fsum(
                                (
                                    obligation_cost,
                                    formated_obligations['data'][valid_debt_type]['total']
                                )
                            ),
                            2)
                        formated_obligations['data'][valid_debt_type]['data'][batch_id]['total'] = round(
                            fsum(
                                (
                                    obligation_cost,
                                    formated_obligations['data'][valid_debt_type]['data'][batch_id]['total']
                                )
                            ),
                            2)

                # Sort obligations
                for debt_type, debt_type_dict in formated_obligations['data'].items():
                    for batch_id in debt_type_dict['data'].keys():
                        formated_obligations['data'][debt_type]['data'][batch_id]['data'] = sorted(
                            formated_obligations['data'][debt_type]['data'][batch_id]['data'],
                            key=lambda d: d['payOrder']
                        )

                response['obligations'] = formated_obligations
                return response, HTTPStatus.OK

            return {
                "taxSubject": response['taxSubject'],
                "obligations": {
                    "total": 0,
                    "data": {}
                },
                "hasMore": response['hasMore']
            }, HTTPStatus.OK
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
                "message": str(submission_err),
            }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status
