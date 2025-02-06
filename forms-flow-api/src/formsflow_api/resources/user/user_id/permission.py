from http import HTTPStatus

from flask import current_app, request
from flask_restx import Resource, fields
from formsflow_api_utils.utils import (
    ADMIN_GROUP,
    auth,
    cors_preflight,
    profiletime,
)

from formsflow_api.schemas import (
    UserPermissionUpdateSchema,
)
from formsflow_api.services.factory import KeycloakFactory
from ..namespace import API

user_permission_update_model = API.model(
    "UserPermission",
    {"userId": fields.String(), "groupId": fields.String(), "name": fields.String()},
)


@cors_preflight("PUT, DELETE, OPTIONS")
@API.route(
    "/<string:user_id>/permission/groups/<string:group_id>",
    methods=["PUT", "DELETE", "OPTIONS"],
)
class UserPermission(Resource):
    """Resource to manage keycloak user permissions."""

    @staticmethod
    @auth.has_one_of_roles([ADMIN_GROUP])
    @profiletime
    @API.doc(body=user_permission_update_model)
    @API.response(204, "NO CONTENT:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def put(user_id, group_id):
        """Add users to role / group."""
        json_payload = request.get_json()
        user_and_group = UserPermissionUpdateSchema().load(json_payload)
        current_app.logger.debug("Initializing admin API service...")
        service = KeycloakFactory.get_instance()
        current_app.logger.debug("Successfully initialized admin API service !")
        response = service.add_user_to_group_role(user_id, group_id, user_and_group)
        if not response:
            current_app.logger.error(f"Failed to add {user_id} to group {group_id}")
            return {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST
        return None, HTTPStatus.NO_CONTENT

    @staticmethod
    @auth.has_one_of_roles([ADMIN_GROUP])
    @profiletime
    @API.doc(body=user_permission_update_model)
    @API.response(204, "NO CONTENT:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def delete(user_id, group_id):
        """Remove users from role / group."""
        json_payload = request.get_json()
        user_and_group = UserPermissionUpdateSchema().load(json_payload)
        current_app.logger.debug("Initializing admin API service...")
        service = KeycloakFactory.get_instance()
        current_app.logger.debug("Successfully initialized admin API service !")
        response = service.remove_user_from_group_role(
            user_id, group_id, user_and_group
        )
        if not response:
            current_app.logger.error(
                f"Failed to remove {user_id} from group {group_id}"
            )
            return {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST
        return None, HTTPStatus.NO_CONTENT
