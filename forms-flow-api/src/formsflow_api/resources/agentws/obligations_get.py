from http import HTTPStatus

import re
from flask_restx import Resource
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    profiletime
)
from flask import current_app, request
from formsflow_api.resources.agentws.namespace import API

from formsflow_api.models.mateus_payment_group import MateusPaymentGroup
from formsflow_api.resources.assurance_level_decorator import require_assurance_level
from formsflow_api.schemas import MateusPaymentGroupSchema


@cors_preflight("GET,OPTIONS")
@API.route("/obligation", methods=["GET", "OPTIONS"])
class AgentWSObligationGroupResource(Resource):

    @classmethod
    @auth.require
    @user_context
    @require_assurance_level("substantial,high")
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
