"""API endpoints for processing applications resource."""

from http import HTTPStatus

from flask import current_app, request
from flask_restx import Namespace, Resource
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth,
)

from formsflow_api.resources.application import application_base_model
from formsflow_api.schemas import (
    ApplicationSchema,
)
from formsflow_api.services import ApplicationService
from formsflow_api.services.external import BPMService
from formsflow_api.services.overriden import FormioServiceExtended

API = Namespace("ApplicationProcessing", description="ApplicationProcessing")


@cors_preflight("POST,OPTIONS")
@API.route("/create", methods=["POST", "OPTIONS"])
class ApplicationProcessingResource(Resource):
    """Resource for application processing creation."""

    @staticmethod
    @profiletime
    @auth.require
    @user_context
    @API.response(201, "CREATED:- Successful request.", model=application_base_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post(**kwargs):
        submission_json = request.get_json()
        if submission_json['data']['caseDataSource']['data']['serviceId']:
            try:
                service_id = submission_json['data']['caseDataSource']['data']['serviceId']
                formio_service = FormioServiceExtended()
                formio_token = formio_service.get_formio_access_token()
                form_id = formio_service.fetch_form_id_by_path(service_id + '/forma/request', formio_token)
                submission_data = submission_json['data']
                submission_data["applicationId"] = ""
                submission_data["applicationStatus"] = ""
                data = {
                    "formId": form_id,
                    "data": submission_data
                }
                current_app.logger.info(form_id)
                submission = formio_service.post_submission(data=data, formio_token=formio_token)
                current_app.logger.info(submission)
                application_schema = ApplicationSchema()
                application_data = application_schema.load({
                    "formId": form_id,
                    "submissionId": submission['_id'],
                    "formUrl": current_app.config.get("FORMIO_WEB_URL") + "/form/" + form_id + "/submission/" +
                               submission[
                                   '_id'],
                    "webFormUrl": current_app.config.get("WEB_BASE_URL") + "/form/" + form_id + "/submission/" +
                                  submission['_id'],

                })
                current_app.logger.info(application_data)
                application, status = ApplicationService.create_application(
                    data=application_data, token=request.headers["Authorization"]
                )
                response = application_schema.dump(application)
                if submission_data['assignee']:
                    bpm_service = BPMService()
                    tasks = bpm_service.get_process_instance_tasks(process_instance_id=response["processInstanceId"],
                                                                   token=request.headers["Authorization"])
                    bpm_service.claim_task(task_id=tasks[0]["id"], data={"userId": submission_data['assignee']},
                                           token=request.headers["Authorization"])
                return response, status
            except BaseException as submission_err:  # pylint: disable=broad-except
                response, status = {
                                       "type": "Bad request error",
                                       "message": "Invalid submission request passed",
                                   }, HTTPStatus.BAD_REQUEST
                current_app.logger.warning(response)
                current_app.logger.warning(submission_err)
                return response, status
        else:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid application request passed",
                               }, HTTPStatus.BAD_REQUEST
            return response, status
