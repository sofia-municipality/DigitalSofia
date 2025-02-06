from http import HTTPStatus

from flask import current_app
from flask_restx import Resource
from formsflow_api_utils.utils import cors_preflight, auth, user_context, UserContext

from formsflow_api.resources.user.namespace import API
from formsflow_api.utils import validate_person_identifier
from formsflow_api.services.login_event_service import LoginEventService


@cors_preflight("PUT, OPTIONS")
@API.route(
    "/login-event",
    methods=["PUT", "OPTIONS"],
)
class LoginEvent(Resource):
    @staticmethod
    @auth.require
    @user_context
    @API.response(200, "OK")
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def put(**kwargs):
        try:
            user: UserContext = kwargs["user"]
            current_app.logger.debug(user.token_info)
            person_identifier = validate_person_identifier(
                user.token_info["personIdentifier"]
            )

            current_app.logger.debug("person_identifier")
            current_app.logger.debug(person_identifier)

            LoginEventService.add_user_login_event(user.token_info["personIdentifier"])
        except BaseException as error:
            response, status = {
                "error": "Something went wrong while adding login event.",
                "status": HTTPStatus.BAD_REQUEST.phrase,
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.debug(response)
            current_app.logger.error(error)

            return response, status

        return "OK"
