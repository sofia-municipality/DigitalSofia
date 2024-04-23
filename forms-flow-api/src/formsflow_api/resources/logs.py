from flask_restx import Namespace, Resource
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    auth,
    cors_preflight
)
from flask import current_app, request
from http import HTTPStatus
from formsflow_api.models.application import Application
from formsflow_api.models.draft import Draft
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services.external import BPMService
from formsflow_api.schemas.services import ServicesListSchema
import os
import time


API = Namespace("Logs", description="Mobile logs storing")


@cors_preflight("POST,OPTIONS")
@API.route("/mobile/<int:person_identifier>", methods=["POST", "OPTIONS"])
class ApplicationResource(Resource):

    @staticmethod
    @API.response(200, "OK")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def post(person_identifier, **kwargs):
        if request.files:
            directory = os.path.join(current_app.config.get("MOBILE_LOGS_FILE_DESTINATION"), str(person_identifier))
            if not os.path.exists(directory):
                os.makedirs(name=directory, exist_ok=True)

            files = request.files.getlist("files[]")

            for file_upload in files:
                file_upload.save(os.path.join(directory, str(time.time()) + " " + file_upload.filename))
            return None, HTTPStatus.OK
        else:
            response, status = {
                "type": "Bad request error",
                "message": "No files provided.",
            }, HTTPStatus.BAD_REQUEST

            return response, status