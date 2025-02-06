from http import HTTPStatus

import requests
from flask import current_app, request
from flask_restx import Resource, fields
from formsflow_api_utils.utils import (
    auth,
    cors_preflight,
    profiletime,
)

from formsflow_api.schemas import (
    UsersListSchema,
)
from formsflow_api.services import UserService
from formsflow_api.services.factory import KeycloakFactory
from .namespace import API

user_list_count_model = API.model(
    "List",
    {
        "data": fields.List(
            fields.Nested(
                API.model(
                    "UserList",
                    {
                        "id": fields.String(),
                        "email": fields.String(),
                        "firstName": fields.String(),
                        "lastName": fields.String(),
                        "username": fields.String(),
                    },
                )
            )
        ),
        "count": fields.Integer(),
    },
)


@cors_preflight("GET, OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class KeycloakUsersList(Resource):
    """Resource to fetch keycloak users."""

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(
        params={
            "memberOfGroup": {
                "in": "query",
                "description": "Group/Role  name for fetching users.",
                "default": "",
            },
            "search": {
                "in": "query",
                "description": "A String contained in username, first or last name, or email.",
                "default": "",
            },
            "pageNo": {
                "in": "query",
                "description": "Page number.",
                "default": 1,
            },
            "limit": {
                "in": "query",
                "description": "Max result size.",
                "default": 5,
            },
            "role": {
                "in": "query",
                "description": "Boolean which defines whether roles are returned.",
                "default": "false",
            },
            "count": {
                "in": "query",
                "description": "Boolean which defines whether count is returned.",
                "default": "false",
            },
        }
    )
    @API.response(200, "OK:- Successful request.", model=user_list_count_model)
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get():  # pylint: disable=too-many-locals
        """Get users list."""
        try:
            group_name = request.args.get("memberOfGroup")
            search = request.args.get("search")
            page_no = int(request.args.get("pageNo", 0))
            limit = int(request.args.get("limit", 0))
            role = request.args.get("role") == "true"
            count = request.args.get("count") == "true"
            kc_admin = KeycloakFactory.get_instance()
            if group_name:
                (users_list, users_count) = kc_admin.get_users(
                    page_no, limit, role, group_name, count
                )
                user_service = UserService()
                response = {
                    "data": user_service.get_users(request.args, users_list),
                    "count": users_count,
                }
            else:
                (user_list, user_count) = kc_admin.search_realm_users(
                    search, page_no, limit, role, count
                )
                user_list_response = []
                for user in user_list:
                    user = UsersListSchema().dump(user)
                    user_list_response.append(user)
                response = {"data": user_list_response, "count": user_count}
            return response, HTTPStatus.OK
        except requests.exceptions.RequestException as err:
            current_app.logger.warning(err)
            return {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST
        except Exception as unexpected_error:
            current_app.logger.warning(unexpected_error)
            raise unexpected_error
