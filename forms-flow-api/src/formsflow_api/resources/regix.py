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

API = Namespace("Regix", description="Regix")


@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class RegixResource(Resource):

    @classmethod
    @auth.require
    @user_context
    @API.doc(
        params={
            "identityDocumentNumber": {
                "in": "query",
                "description": "Identity Document Number",
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
        403,
        "FORBIDDEN - Invalid document returned"
    )
    @API.response(
        404,
        "NOT_FOUND - No user found for provided identity document and personal identifier"
    )
    def get(cls, **kwargs):
        try:
            user: UserContext = kwargs["user"]
            identity_document_number = request.args["identityDocumentNumber"] or None
            current_app.logger.debug("Resource")
            person_identifier = user.token_info["personIdentifier"]
            current_app.logger.debug(person_identifier)

            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            current_app.logger.debug(match)
            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            person_identifier = match[0]
            client = EFormIntegrationsService()
            response = client.get_person_data(person_identifier, identity_document_number)

            return response, HTTPStatus.OK
        except EFormIntegrationException as err:
            response, status = {
                                   "type": "EForm IntegrationException",
                                   "message": err.message
                               }, err.error_code
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except KeyError as err:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
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
        except BusinessException as err:
            # exception from draft service
            current_app.logger.warning(err)
            error, status = err.error, err.status_code
            return error, status


@cors_preflight("POST,OPTIONS")
@API.route("/search", methods=["POST", "OPTIONS"])
class RegixSearchResource(Resource):

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
        403,
        "FORBIDDEN - Invalid document returned"
    )
    @API.response(
        404,
        "NOT_FOUND - No user found for provided identity document and personal identifier"
    )
    def post(cls, **kwargs):
        try:
            client = EFormIntegrationsService()
            response = client.search(request.get_json())

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
