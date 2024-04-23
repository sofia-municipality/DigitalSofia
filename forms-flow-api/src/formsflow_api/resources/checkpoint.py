"""API endpoints for managing healthcheckpoint API resource."""

from http import HTTPStatus
import functools
from flask import current_app
from flask_restx import Namespace, Resource
from formsflow_api_utils.utils import cors_preflight, auth, profiletime, user_context, UserContext
from formsflow_api.resources.assurance_level_decorator import require_assurance_level

API = Namespace("Checkpoint", description="Checkpoint")

@cors_preflight("GET")
@API.route("", methods=["GET"])
class HealthCheckpointResource(Resource):
    """Resource for managing healthcheckpoint."""

    @staticmethod
    def get():
        """Get the status of API."""

        current_app.logger.debug("*** ENTER METHOD ***")

        return (
            ({"message": "Welcome to formsflow.ai API"}),
            HTTPStatus.OK,
        )
