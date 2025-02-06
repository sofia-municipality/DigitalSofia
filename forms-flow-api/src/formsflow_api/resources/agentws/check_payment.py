from http import HTTPStatus

import re

from flask_restx import Resource
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    cors_preflight,
    auth
)
from flask import current_app
from formsflow_api.resources.agentws.namespace import API
from formsflow_api.services import ObligationService
from formsflow_api.exceptions import EFormIntegrationException


@cors_preflight("GET,OPTIONS")
@API.route("/check-for-payment", methods=["GET", "OPTIONS"])
class AgentWSPaymentCheckResource(Resource):

    @classmethod
    @auth.require
    @user_context
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
            person_identifier = user.token_info["personIdentifier"]

            # validate person identifier
            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            # get person identifier
            person_identifier = match[0]

            obligationService = ObligationService()

            return obligationService.verify_payment_status(person_identifier)
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
