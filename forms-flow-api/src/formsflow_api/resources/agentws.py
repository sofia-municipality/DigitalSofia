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
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.exceptions import EFormIntegrationException

API = Namespace("AgentWS", description="AgentWS")


VALID_DEBT_REG_ID = {
    '2': 'real_estate',
    '4': 'vehicle',
    '5': 'household_waste'
}

@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class AgentWSResource(Resource):

    @classmethod
    @auth.require
    @user_context
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
                for obligation in obligations:
                    debt_type_id = obligation.get('kindDebtRegId', None)
                    if not obligation:
                        current_app.logger.critical("Obligation doesn't have kindDebtRegId key")
                        current_app.logger.critical(obligation.get('debtinstalmentId'))
                        continue
                
                    valid_debt_type = VALID_DEBT_REG_ID.get(debt_type_id, None)
                    interest = obligation.get('interest', 0)
                    residual = obligation.get('residual', 0)
                    obligation_cost = interest + residual

                    ### Check if it is a debt type we must handle 
                    if valid_debt_type and obligation_cost > 0:
                        ### Check if we already have it
                        if valid_debt_type not in  formated_obligations['data']:
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
                        formated_obligations['total'] += obligation_cost
                        formated_obligations['data'][valid_debt_type]['total'] += obligation_cost
                        formated_obligations['data'][valid_debt_type]['data'][batch_id]['total'] += obligation_cost

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
