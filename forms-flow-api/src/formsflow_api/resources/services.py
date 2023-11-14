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


API = Namespace("Services", description="Personal Services")

service = API.model(
    "Service",
    {

    }
)


@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class ServicesResource(Resource):

    @API.doc(
        params={
            "pageNo": {
                "in": "query",
                "description": "Page number for paginated results",
                "default": "1",
            },
            "limit": {
                "in": "query",
                "description": "Limit for paginated results",
                "default": "5",
            },
            "formioFields": {
                "in": "query",
                "description": "List of fields to show on listing",
                "default": ""
            },
            "bpmFields": {
                "in": "query",
                "description": "List of fields to show on listing",
                "default": ""
            }
        }
    )
    @staticmethod
    @auth.require
    @user_context
    def get(**kwargs):
        user: UserContext = kwargs["user"]
        # person_identifier = user.token_info["personIdentifier"]

        try:
            query_params = ServicesListSchema().load(request.args) or {}
            page_number = query_params.get("page_no", 1)
            limit = query_params.get("limit", 5)
            formio_fields = query_params.get("formio_fields", [])
            bpm_fields = query_params.get("bpm_fields", [])
        except BaseException as submission_err:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status

        # specify forms
        # Get Application
        query = Application.query.filter_by(
            created_by=user.user_name
        ).order_by(Application.id.desc())

        total = query.count()
        pagination = query.paginate(page=page_number, per_page=limit)

        formio_client = FormioServiceExtended()
        formio_token = formio_client.get_formio_access_token()

        to_return = []
        for application in pagination.items:
            new_item = {
                "applicationId": application.id,
                "path": application.form_process_mapper.form_path,
                "status": application.application_status,
                "formioName": application.form_process_mapper.form_name,
                "formioSubmissionId": application.submission_id,
                "formioFormId": application.latest_form_id,
                "processInstanceId": application.process_instance_id,
                "camundaProcessKey": application.form_process_mapper.process_key,
                "created": application.created.isoformat(),
                "modified": application.modified.isoformat(),
                "draftId": None,
                "formioData": None,
                "camundaData": None
            }

            ### Getting formio data
            current_app.logger.debug("Getting formio data")
            current_app.logger.debug(application.latest_form_id)
            current_app.logger.debug(application.submission_id)

            data = None
            if application.submission_id and application.form_process_mapper.form_path:
                try:
                    submission = formio_client.debug_get_submission(
                        {
                            'form_id': application.latest_form_id,
                            'sub_id': application.submission_id
                        }, 
                        formio_token=formio_token
                    )
                except BusinessException as err:
                        current_app.logger.error("An error occurred when getting submission from formio")
                        current_app.logger.error(err)
                else:
                    current_app.logger.debug(submission)
                    data = submission.get("data", [])
                
            else:
                draft = Draft.query.filter_by(application_id=application.id).order_by(Draft.modified.desc()).first()
                if draft:
                    current_app.logger.debug(draft.data)
                    data = draft.data
                    new_item["draftId"] = draft.id

            # Filter data                    
            if data is not None and formio_fields:
                data = {key: data.get(key, None) for key in formio_fields}
            new_item["formioData"] = data

            ### Getting camunda process instance data
            current_app.logger.debug("Getting camunda data")
            current_app.logger.debug(application.process_instance_id)
            if application.process_instance_id:
                data = BPMService.get_process_variables(process_instance_id=application.process_instance_id, token=None)
                if data:

                    # Filter keys
                    if bpm_fields:
                        data = {key: data.get(key, None) for key in bpm_fields}
                    new_item["camundaData"] = data

            if application.process_instance_id:
                tasks = BPMService.get_process_instance_tasks(process_instance_id=application.process_instance_id, token=None)
                new_item["camundaTasks"] = tasks

            to_return.append(new_item)
        # select them from form.io + camunda values when needed
        # format them with the needed resource
        
        
        return (
            {
                "items": to_return,
                "total": total,
                "pages": pagination.pages
            }, 
            HTTPStatus.OK
        )