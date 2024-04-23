"""API endpoints for RegiX integration."""

from http import HTTPStatus

import re

from flask_restx import Namespace, Resource
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    profiletime
)
from flask import current_app, request

from formsflow_api.models import MateusPaymentRequest
from formsflow_api.models.mateus_payment_group import MateusPaymentGroup
from formsflow_api.models.valid_debt_reg_id import VALID_DEBT_REG_ID
from formsflow_api.schemas import MateusPaymentGroupSchema, MateusPaymentRequestSchema
from formsflow_api.services import ObligationService
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.exceptions import EFormIntegrationException
from math import fsum
from formsflow_api.resources.assurance_level_decorator import require_assurance_level

#from formsflow_api.services.obligation import ObligationService

API = Namespace("AgentWS", description="AgentWS")


@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class AgentWSResource(Resource):

    @classmethod
    @auth.require
    @user_context
    # @require_assurance_level("substantial,high")
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
            ### Format obligations
            formated_obligations = {
                'total': 0,
                'data': {

                }
            }
            if obligations:
                schema = MateusPaymentRequestSchema()
                for obligation in obligations:
                    debt_type_id = obligation.get('kindDebtRegId', None)
                    if not obligation:
                        current_app.logger.critical("Obligation doesn't have kindDebtRegId key")
                        current_app.logger.critical(obligation.get('debtinstalmentId'))
                        continue

                    # mark if payment request has already been generated for a obligation
                    reason = str(obligation["taxPeriodYear"]) + "-" + str(obligation["instNo"]) + "-" + str(
                        obligation["partidaNo"])
                    payment = MateusPaymentRequest.find_by_reason(reason, debt_type_id)
                    payment = schema.dump(payment, many=False)
                    if "payment_id" in payment and "status" in payment:
                        obligation["status"] = payment["status"]
                        obligation["hasPaymentRequest"] = True
                    valid_debt_type = VALID_DEBT_REG_ID.get(debt_type_id, None)
                    interest = obligation.get('interest', 0)
                    residual = obligation.get('residual', 0)
                    obligation_cost = round(fsum([interest, residual]), 2)

                    ### Check if it is a debt type we must handle 
                    if valid_debt_type and obligation_cost > 0:
                        ### Check if we already have it
                        if valid_debt_type not in formated_obligations['data']:
                            formated_obligations['data'][valid_debt_type] = {
                                'total': 0,
                                'data': {}
                            }

                        ### Check if  partidaNo is already added
                        batch_id = obligation.get('partidaNo')
                        if batch_id not in formated_obligations['data'][valid_debt_type]['data']:
                            formated_obligations['data'][valid_debt_type]['data'][batch_id] = {
                                'total': 0,
                                'data': []
                            }

                        ### Add item to correct end node
                        formated_obligations['data'][valid_debt_type]['data'][batch_id]['data'].append(obligation)

                        ### Add totals
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

            ### Sort obligations
            for debt_type, debt_type_dict in formated_obligations['data'].items():
                for batch_id in debt_type_dict['data'].keys():
                    formated_obligations['data'][debt_type]['data'][batch_id]['data'] = sorted(
                        formated_obligations['data'][debt_type]['data'][batch_id]['data'],
                        key=lambda d: d['payOrder']
                    )

            response['obligations'] = formated_obligations
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
                                   "message": str(submission_err),
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status


@cors_preflight("POST,OPTIONS")
@API.route("/payment-request", methods=["POST", "OPTIONS"])
class AgentWSPaymentRequestResource(Resource):

    @classmethod
    @auth.require
    @user_context
    # @require_assurance_level("substantial,high")
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
            person_identifier = user.token_info["personIdentifier"]

            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            person_identifier = match[0]
            client = EFormIntegrationsService()
            obligations = request.get_json()
            p_schema = MateusPaymentRequestSchema()
            for p_request in obligations["payment_requests"]:
                reason = str(p_request["taxPeriodYear"]) + "-" + str(p_request["instNo"]) + "-" + str(p_request["partidaNo"])
                payment = MateusPaymentRequest.find_by_reason(reason, p_request["kindDebtRegId"])
                if payment is not None:
                    payment = p_schema.dump(payment, many=False)
                    if payment["status"].capitalize() == 'Pending' or payment["status"].capitalize() == 'Paid':
                        response, status = {
                                               "type": "Bad request error",
                                               "message": "Payment request already created",
                                           }, HTTPStatus.BAD_REQUEST
                        return response, status

            group = MateusPaymentGroup.create_from_dict({
                "person_identifier": person_identifier,
                "status": "New",
                "tax_subject_id": obligations["taxSubjectId"]
            }).to_json()
            for p_request in obligations["payment_requests"]:
                client.create_payment_from_obligation(group, p_request, user)

            return group, HTTPStatus.OK
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


@cors_preflight("GET,OPTIONS")
@API.route("/obligation", methods=["GET", "OPTIONS"])
class AgentWSObligationGroupResource(Resource):

    @classmethod
    @auth.require
    @user_context
    # @require_assurance_level("substantial,high")
    @API.doc(
        params={
            "limit": {
                "in": "query",
                "description": "Number of obligations",
                "default": "10",
            },
            "page": {
                "in": "query",
                "description": "Current page in the list of obligations",
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
            limit = request.args.get('limit', 10)
            page = request.args.get('page', 1)
            person_identifier = user.token_info["personIdentifier"]
            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            person_identifier = match[0]

            obligation_group, page_count, total = MateusPaymentGroup.find_by_personal_identifier(person_identifier,
                                                                                                 limit,
                                                                                                 page)

            schema = MateusPaymentGroupSchema()
            result = {
                'obligations': schema.dump(obligation_group, many=True),
                'pages': page_count,
                'total': total
            }

            return result, HTTPStatus.OK
        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": str(submission_err),
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
        return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/obligation/<int:group_id>", methods=["GET", "OPTIONS"])
class AgentWSObligationPaymentRequestsResource(Resource):

    @classmethod
    @auth.require
    @user_context
    # @require_assurance_level("substantial,high")
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
            client = ObligationService()
            return client.update_status_of_group_payments(group_id), HTTPStatus.OK
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

