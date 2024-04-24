"""API endpoints for managing application resource."""

from http import HTTPStatus

import re
import requests
from urllib.parse import urlencode
from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    DESIGNER_GROUP,
    REVIEWER_GROUP,
    auth,
    cors_preflight,
    get_form_and_submission_id_from_form_url,
    profiletime,
)
from marshmallow.exceptions import ValidationError
from formsflow_api_utils.utils.user_context import UserContext, user_context

from formsflow_api.schemas import (
    ApplicationListReqSchema,
    ApplicationListRequestSchema,
    ApplicationSchema,
    ApplicationUpdateSchema,
    ApplicationPermittedSchema
)
from formsflow_api.models import Application, Draft, FormProcessMapper, ApplicationHistory
from formsflow_api.models.db import db
from formsflow_api.services import ApplicationService, DocumentsService
from formsflow_api.services.external import BPMService, KeycloakAdminAPIService, EurotrustIntegrationsService
from formsflow_api.services.overriden import FormioServiceExtended

API = Namespace("Application", description="Application")

application_create_model = API.model(
    "ApplicationCreate",
    {
        "formId": fields.String(),
        "submissionId": fields.String(),
        "formUrl": fields.String(),
        "webFormUrl": fields.String(),
    },
)

application_base_model = API.model(
    "ApplicationCreateResponse",
    {
        "applicationStatus": fields.String(),
        "created": fields.String(),
        "createdBy": fields.String(),
        "formId": fields.String(),
        "formProcessMapperId": fields.String(),
        "id": fields.Integer(),
        "modified": fields.String(),
        "modifiedBy": fields.String(),
        "processInstanceId": fields.String(),
        "submissionId": fields.String(),
    },
)

application_model = API.inherit(
    "Application",
    application_base_model,
    {
        "applicationName": fields.String(),
        "processKey": fields.String(),
        "processName": fields.String(),
        "processTenant": fields.String(),
    },
)

application_list_model = API.model(
    "ApplicationList",
    {
        "applications": fields.List(
            fields.Nested(application_model, description="List of Applications.")
        ),
        "draftCount": fields.Integer(),
        "totalCount": fields.Integer(),
        "limit": fields.Integer(),
        "pageNo": fields.Integer(),
    },
)

application_update_model = API.model(
    "ApplicationUpdate",
    {"applicationStatus": fields.String(), "formUrl": fields.String()},
)

application_status_list_model = API.model(
    "StatusList", {"applicationStatus": fields.List(fields.String())}
)

application_resubmit_model = API.model(
    "ApplicationResubmitModel",
    {
        "processInstanceId": fields.String(),
        "messageName": fields.String(),
        "data": fields.Raw(),
    },
)

ADDITIONAL_FORM_KEY_MATCH_CHECKS = {
    "ownersignform": {
        "match": {
            "email": "propertyOwnerEmail",
            "phone": "propertyOwnerPhone"
        },
        "mismatch": {
            "person_identifier": "personIdentifier"
        }
    },
    "trusteesignform": {
        "match": {
            "email": "trusteeEmail",
            "phone": "trusteePhone",
        },
        "mismatch": {
            "person_identifier": "personIdentifier"
        }
    },
    "signitureform": {
        "match": {
            # "email": "email",
            # "phone": "phone",
            "person_identifier": "personIdentifier"
        }
    }
}


@cors_preflight("GET,POST,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class ApplicationsResource(Resource):
    """Resource for managing applications."""

    @staticmethod
    @auth.require
    @profiletime
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
            "sortBy": {
                "in": "query",
                "description": "Specify field for sorting the results.",
                "default": "id",
            },
            "sortOrder": {
                "in": "query",
                "description": "Specify sorting  order.",
                "default": "desc",
            },
            "applicationName": {
                "in": "query",
                "description": "Filter resources by application name.",
                "type": "string",
            },
            "id": {
                "in": "query",
                "description": "Filter resources by id.",
                "type": "int",
            },
            "modifiedFrom": {
                "in": "query",
                "description": "Filter resources by modified from.",
                "type": "string",
            },
            "modifiedTo": {
                "in": "query",
                "description": "Filter resources by modified to.",
                "type": "string",
            },
        }
    )
    @API.response(200, "OK:- Successful request.", model=application_list_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get():  # pylint:disable=too-many-locals
        """Get applications."""
        try:
            dict_data = ApplicationListRequestSchema().load(request.args) or {}
            page_no = dict_data.get("page_no")
            limit = dict_data.get("limit")
            order_by = dict_data.get("order_by", "id")
            application_id = dict_data.get("application_id")
            application_name = dict_data.get("application_name")
            application_status = dict_data.get("application_status")
            created_by = dict_data.get("created_by")
            created_from_date = dict_data.get("created_from_date")
            created_to_date = dict_data.get("created_to_date")
            modified_from_date = dict_data.get("modified_from_date")
            modified_to_date = dict_data.get("modified_to_date")
            sort_order = dict_data.get("sort_order", "desc")
            if auth.has_role([REVIEWER_GROUP]):
                (
                    application_schema_dump,
                    application_count,
                    draft_count,
                ) = ApplicationService.get_auth_applications_and_count(
                    created_from=created_from_date,
                    created_to=created_to_date,
                    modified_from=modified_from_date,
                    modified_to=modified_to_date,
                    order_by=order_by,
                    sort_order=sort_order,
                    created_by=created_by,
                    application_id=application_id,
                    application_name=application_name,
                    application_status=application_status,
                    token=request.headers["Authorization"],
                    page_no=page_no,
                    limit=limit,
                )
            else:
                (
                    application_schema_dump,
                    application_count,
                    draft_count,
                ) = ApplicationService.get_all_applications_by_user(
                    page_no=page_no,
                    limit=limit,
                    order_by=order_by,
                    sort_order=sort_order,
                    created_from=created_from_date,
                    created_to=created_to_date,
                    modified_from=modified_from_date,
                    modified_to=modified_to_date,
                    created_by=created_by,
                    application_id=application_id,
                    application_name=application_name,
                    application_status=application_status,
                )
            return (
                (
                    {
                        "applications": application_schema_dump,
                        "totalCount": application_count,
                        "draftCount": draft_count,
                        "limit": limit,
                        "pageNo": page_no,
                    }
                ),
                HTTPStatus.OK,
            )
        except ValidationError as err:
            response, status = (
                {
                    "type": "Invalid Request Object",
                    "message": "Required fields are not passed",
                },
                HTTPStatus.BAD_REQUEST,
            )

            current_app.logger.critical(response)
            current_app.logger.critical(err)
            return response, status

        except KeyError as err:
            response, status = (
                {
                    "type": "Invalid Request Object",
                    "message": "Required fields are not passed",
                },
                HTTPStatus.BAD_REQUEST,
            )
            current_app.logger.critical(response)
            current_app.logger.critical(err)
            return response, status


@cors_preflight("GET,PUT,DELETE,OPTIONS")
@API.route("/<int:application_id>", methods=["GET", "PUT", "DELETE", "OPTIONS"])
class ApplicationResourceById(Resource):
    """Resource for getting application by id."""

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.", model=application_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get(application_id: int):
        """Get application by id."""
        try:
            if auth.has_role([REVIEWER_GROUP]):
                (
                    application_schema_dump,
                    status,
                ) = ApplicationService.get_auth_by_application_id(
                    application_id=application_id,
                    token=request.headers["Authorization"],
                )
                return (
                    application_schema_dump,
                    status,
                )
            application, status = ApplicationService.get_application_by_user(
                application_id=application_id
            )
            return (application, status)
        except PermissionError as err:
            response, status = (
                {
                    "type": "Permission Denied",
                    "message": f"Access to form id - {application_id} is prohibited.",
                },
                HTTPStatus.FORBIDDEN,
            )
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BusinessException as err:
            return err.error, err.status_code

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def put(application_id: int):
        """Update application details."""
        application_json = request.get_json()
        try:
            application_schema = ApplicationUpdateSchema()
            dict_data = application_schema.load(application_json)
            form_url = dict_data.get("form_url", None)
            if form_url:
                (
                    latest_form_id,
                    submission_id,
                ) = get_form_and_submission_id_from_form_url(form_url)
                dict_data["latest_form_id"] = latest_form_id
                dict_data["submission_id"] = submission_id
            ApplicationService.update_application(
                application_id=application_id, data=dict_data
            )
            return "Updated successfully", HTTPStatus.OK
        except PermissionError as err:
            response, status = (
                {
                    "type": "Permission Denied",
                    "message": f"Access to application-{application_id} is prohibited.",
                },
                HTTPStatus.FORBIDDEN,
            )
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status

        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid request data",
                               }, HTTPStatus.BAD_REQUEST

            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)

            return response, status

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def delete(application_id: int, **kwargs):
        try:
            ### 1. Get user data
            current_app.logger.debug("1. Get user data")
            user: UserContext = kwargs["user"]
            user_id: str = user.user_name
            tenant_key: str = user.tenant_key
            owner_form_application_id = None
            trustee_form_application_id = None

            ### 2. Get application by user_id
            current_app.logger.debug("2. Get application by user")
            application = Application.query.filter_by(
                id=application_id,
                created_by=user_id
            ).first()

            ### 3. If application is not found return 404
            current_app.logger.debug("3. Check if application exists")
            if not application:
                return {
                           "type": "Not found",
                           "message": ""
                       }, HTTPStatus.NOT_FOUND

            ### 4. Delete process instance
            bpm_response = None
            current_app.logger.debug(f"4. Delete process instance - {application.process_instance_id}")
            if application.process_instance_id:
                ### 4.1. Get process instance
                process_instance = BPMService.get_process_instance(process_instance_id=application.process_instance_id,
                                                                   token=None)
                if process_instance and process_instance.get("id"):
                    ### 4.2. Get process values
                    process_variables = BPMService.get_process_variables(
                        process_instance_id=application.process_instance_id, token=None)

                    ### 4.3. Get related variables
                    if process_variables.get("ownerFormApplicationId"):
                        owner_form_application_id = process_variables.get("ownerFormApplicationId").get("value")

                    if process_variables.get("trusteeFormApplicationId"):
                        trustee_form_application_id = process_variables.get("trusteeFormApplicationId").get("value")

                    ### 4.4. Delete process instance
                    bpm_response = BPMService.delete_process_instance(
                        process_instance_id=application.process_instance_id,
                        token=None
                    )

            ### 5. Which application should we delete:
            related_application_ids = [owner_form_application_id, trustee_form_application_id, application.id]
            application_ids_to_delete = list(filter(None, related_application_ids))

            current_app.logger.debug(
                f"5. From which applications should we delete " + ' '.join(map(str, application_ids_to_delete)))

            ### 6. Delete formio application resource
            current_app.logger.debug(f"6. Deleting from formio application resource if any")
            formio_response = None
            formio_client = FormioServiceExtended()
            if application.submission_id:
                formio_path = application.form_process_mapper.form_path

                current_app.logger.debug(formio_path)
                formio_response = formio_client.delete_submission_formio(
                    formio_path,
                    formio_submission_id=application.submission_id
                )

            ### 7. Delete generated files for specific application
            file_formio_path = current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
            file_formio_path = f"{tenant_key}-{file_formio_path}"
            current_app.logger.debug(f"7. Deleting from formio file resources - {file_formio_path}")
            if file_formio_path:
                formio_token = formio_client.get_formio_access_token()

                application_ids_to_delete_query_param = ",".join(map(str, application_ids_to_delete))

                related_file_submissions = formio_client.get_all_submissions(
                    file_formio_path,
                    formio_token=formio_token,
                    options=f"data.applicationId__in={application_ids_to_delete_query_param}"
                )

                document_service_client = DocumentsService()
                for file_submission in related_file_submissions:
                    file_id = file_submission.get("_id")

                    if file_id:
                        document_transaction = document_service_client.get_document_by_submission_id(formio_id=file_id)
                        if document_transaction:
                            document_transaction.delete()

                        ### Delete submission in formio
                        current_app.logger.debug(f"Deleting formio file resource with id - {file_id}")
                        formio_client.delete_submission_formio(file_formio_path, file_id)

            ### 8. Delete related formio applications
            ### 8.1. Get related form paths
            ### NOTE: Form paths can change, this is the initial part of the form paths
            related_formio_form_paths = ["ownersignform", "trusteesignform", "signitureform"]
            current_app.logger.debug(f"8. Deleting applications in related formio forms")
            for related_formio_form_path in related_formio_form_paths:
                form_process_mappers = FormProcessMapper.query.filter(
                    FormProcessMapper.form_path.like(related_formio_form_path + "%"),
                    FormProcessMapper.deleted == False
                ).distinct(FormProcessMapper.form_id).all()
                formio_token = formio_client.get_formio_access_token()
                for mapper in form_process_mappers:
                    current_app.logger.debug(f"8.1. Deleting submissions from related form path - {mapper.form_path}")

                    related_submissions = formio_client.get_all_submissions(
                        mapper.form_path,
                        formio_token=formio_token,
                        options=f"data.applicationId={application_id}"
                    )

                    for submission in related_submissions:
                        submission_id = submission.get("_id")
                        current_app.logger.debug(f"Deleting submission - {submission_id}")
                        formio_client.delete_submission_formio(mapper.form_path, submission_id)

            ### 9. Delete application from our db
            drafts_to_delete = application.draft
            if drafts_to_delete:
                for draft in drafts_to_delete:
                    draft.delete()

            application.delete()

            ### 10. Delete application history
            ApplicationHistory.query.filter_by(application_id=application_id).delete()
            db.session.commit()

            return {
                       "application_id": application_id,
                       "bpm_response": bpm_response,
                       "formio_response": formio_response
                   }, HTTPStatus.ACCEPTED
        except BusinessException as err:
            response, status = {
                                   "type": "Bad request error",
                                   "message": (err),
                               }, HTTPStatus.BAD_REQUEST

            current_app.logger.warning(response)
            current_app.logger.warning(err)

            return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/formid/<string:form_id>", methods=["GET", "OPTIONS"])
class ApplicationResourceByFormId(Resource):
    """Resource for getting applications based on formid."""

    @staticmethod
    @auth.require
    @profiletime
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
        }
    )
    @API.response(200, "OK:- Successful request.", model=application_list_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get(form_id: str):
        """Get applications by formId."""
        if request.args:
            dict_data = ApplicationListReqSchema().load(request.args)
            page_no = dict_data["page_no"]
            limit = dict_data["limit"]
        else:
            page_no = 0
            limit = 0

        if auth.has_role(["formsflow-reviewer"]):
            application_schema = ApplicationService.get_all_applications_form_id(
                form_id=form_id, page_no=page_no, limit=limit
            )
            application_count = ApplicationService.get_all_applications_form_id_count(
                form_id=form_id
            )
        else:
            application_schema = ApplicationService.get_all_applications_form_id_user(
                form_id=form_id,
                page_no=page_no,
                limit=limit,
            )
            application_count = (
                ApplicationService.get_all_applications_form_id_user_count(
                    form_id=form_id
                )
            )

        if page_no == 0:
            return (
                (
                    {
                        "applications": application_schema,
                        "totalCount": application_count,
                    }
                ),
                HTTPStatus.OK,
            )
        return (
            (
                {
                    "applications": application_schema,
                    "totalCount": application_count,
                    "limit": limit,
                    "pageNo": page_no,
                }
            ),
            HTTPStatus.OK,
        )


@cors_preflight("GET,OPTIONS")
@API.route("/formid/<string:form_id>/count", methods=["GET", "OPTIONS"])
class ApplicationResourceCountByFormId(Resource):
    """Resource for getting applications count on formid."""

    @staticmethod
    @auth.has_one_of_roles([DESIGNER_GROUP])
    @profiletime
    def get(form_id: str):
        """Get application count by formId."""
        try:
            application_count = ApplicationService.get_all_applications_form_id_count(
                form_id=form_id
            )
            return (
                (
                    {
                        "message": f"Total Applications found are: {application_count}",
                        "value": application_count,
                    }
                ),
                HTTPStatus.OK,
            )
        except PermissionError as err:
            response, status = (
                {
                    "type": "Permission Denied",
                    "message": f"Access to application count of-{form_id} is prohibited",
                },
                HTTPStatus.FORBIDDEN,
            )
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except KeyError as err:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid application request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BaseException as application_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid application request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(application_err)
            return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/permitted", methods=["GET", "OPTIONS"])
class ApplicationResourceAvailableForUser(Resource):

    @classmethod
    def _extract_values(cls, user_key: str, bpm_key: str, user_values: dict, bpm_values: dict):
        bpm_variable = bpm_values.get(bpm_key)
        if not bpm_variable or not bpm_variable.get("value"):
            raise ValidationError(f"Variable {bpm_key} not set in camunda.")

        user_value = user_values.get(user_key)
        if not user_value:
            raise ValidationError(f"Variable {user_key} not set in token.")

        return user_value, bpm_variable.get("value")

    @classmethod
    @auth.require
    @profiletime
    @user_context
    @API.doc(
        params={
            "formioFormId": {
                "in": "query",
                "description": "The formio form id, we use this to see the relation to the task"
            },
            "taskId": {
                "in": "query",
                "description": "The id of the current task, we get the variables from it"
            },
        }
    )
    @API.response(200, "Can edit submission")
    @API.response(403, "Can not edit submission")
    def get(cls, **kwargs):
        try:
            ### 0. Get query parameters
            schema = ApplicationPermittedSchema()
            data = schema.load(request.args)
            task_id = data.get("task_id")
            formio_form_id = data.get("formio_form_id")
            current_app.logger.debug(task_id)

            user: UserContext = kwargs["user"]
            tenant_key: str = user.tenant_key

            ### 1. Get form path based on submission
            formio_service = FormioServiceExtended()
            formio_token = formio_service.get_formio_access_token()
            formio_form_data = formio_service.get_form(
                data={"form_id": formio_form_id},
                formio_token=formio_token
            )

            form_path = formio_form_data["path"]
            current_app.logger.debug(f"Form path - {form_path}")

            ### 2. Do we handle for specific form_path
            keys_to_check = {}
            for key in ADDITIONAL_FORM_KEY_MATCH_CHECKS.keys():
                relevant_key = f"{tenant_key}-{key.lower()}"
                current_app.logger.debug(relevant_key)
                if form_path.lower().startswith(relevant_key):
                    keys_to_check = ADDITIONAL_FORM_KEY_MATCH_CHECKS[key]

            if not keys_to_check:
                raise BusinessException(
                    "We do not make additional checks for specific form path",
                    HTTPStatus.BAD_REQUEST
                )

            ### 3. Get task variables
            bpm_variables = BPMService.get_task_variables(task_id=task_id, token=request.headers["Authorization"])

            ### 4. Get token variables
            user: UserContext = kwargs["user"]
            email = user.token_info.get("email", None)
            phone = user.token_info.get("phone_number", None)

            person_identifier = user.token_info["personIdentifier"]
            preferred_username = user.token_info["preferred_username"]
            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)
            if not match:
                raise ValidationError(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            token_variables = {
                "email": email,
                "phone": phone,
                "person_identifier": person_identifier
            }

            current_app.logger.debug(token_variables)
            current_app.logger.debug(bpm_variables)

            ### 5. Based on keys_to_check check for matches and mismatches

            ### 5.1. Check fo matches
            to_match = keys_to_check.get("match")
            if to_match:
                for key_in_token_variables, key_in_bpm in to_match.items():
                    user_value, bpm_value = cls._extract_values(
                        user_key=key_in_token_variables,
                        bpm_key=key_in_bpm,
                        user_values=token_variables,
                        bpm_values=bpm_variables
                    )

                    ### Missmatch detected
                    current_app.logger.debug(f"{user_value} != {bpm_value}")
                    if user_value.lower() != bpm_value.lower():
                        return {"canEdit": False}, HTTPStatus.FORBIDDEN

            ### 5.2. Check for mismatches
            to_mismatch = keys_to_check.get("mismatch")
            if to_mismatch:
                for key_in_token_variables, key_in_bpm in to_mismatch.items():
                    user_value, bpm_value = cls._extract_values(
                        user_key=key_in_token_variables,
                        bpm_key=key_in_bpm,
                        user_values=token_variables,
                        bpm_values=bpm_variables
                    )

                    ### Match detected
                    current_app.logger.debug(f"{user_value} == {bpm_value}")
                    if user_value.lower() == bpm_value.lower():
                        return {"canEdit": False}, HTTPStatus.FORBIDDEN

            BPMService.claim_task(task_id=task_id, data={"assignee": preferred_username, "userId": preferred_username},
                                   token=request.headers["Authorization"])
            ### 6. Return true if nothing set off
            return {"canEdit": True}, HTTPStatus.OK

        except ValidationError as err:
            current_app.logger.warning(err)
            response, status = {
                                   "type": "Validation error",
                                   "errors": err.messages,
                               }, HTTPStatus.BAD_REQUEST
            return response, status


@cors_preflight("POST,OPTIONS")
@API.route("/create", methods=["POST", "OPTIONS"])
class ApplicationResourcesByIds(Resource):
    """Resource for application creation."""

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(body=application_create_model)
    @API.response(201, "CREATED:- Successful request.", model=application_base_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post():
        """Post a new application using the request body.

        e.g,
        ```
        {
           "formId":"632208d9fbcab29c2ab1a097",
           "submissionId":"63407583fbcab29c2ab1bed4",
           "formUrl":"https://formsflow-forms/form/632208d9fbcab29c2ab1a097/submission/63407583fbcab29c2ab1bed4",
           "webFormUrl":"https://formsflow-web/form/632208d9fbcab29c2ab1a097/submission/63407583fbcab29c2ab1bed4"
        }
        ```
        """
        application_json = request.get_json()

        try:
            application_schema = ApplicationSchema()
            dict_data = application_schema.load(application_json)
            application, status = ApplicationService.create_application(
                data=dict_data, token=request.headers["Authorization"]
            )
            response = application_schema.dump(application)
            return response, status
        except PermissionError as err:
            response, status = (
                {
                    "type": "Permission Denied",
                    "message": f"Access to formId-{dict_data['form_id']} is prohibited",
                },
                HTTPStatus.FORBIDDEN,
            )
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except KeyError as err:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid application request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BaseException as application_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid application request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(application_err)
            return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/status/list", methods=["GET", "OPTIONS"])
class ApplicationResourceByApplicationStatus(Resource):
    """Get application status list."""

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.", model=application_status_list_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get():
        """Method to get the application status lists."""
        try:
            return (
                ApplicationService.get_all_application_status(),
                HTTPStatus.OK,
            )
        except BusinessException as err:
            return err.error, err.status_code


@cors_preflight("POST,OPTIONS")
@API.route("/<int:application_id>/resubmit", methods=["POST", "OPTIONS"])
class ApplicationResubmitById(Resource):
    """Resource for resubmit application."""

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(body=application_resubmit_model)
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    @API.response(403, "FORBIDDEN:- Permission denied")
    def post(application_id: int):
        """Resubmit application."""
        try:
            resubmit_json = request.get_json()
            ApplicationService.resubmit_application(
                application_id, resubmit_json, token=request.headers["Authorization"]
            )
            return "Message event updated successfully.", HTTPStatus.OK
        except PermissionError as err:
            response, status = (
                {
                    "type": "Permission Denied",
                    "message": f"Access to application id - {application_id} is prohibited.",
                },
                HTTPStatus.FORBIDDEN,
            )
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BusinessException as err:
            return err.error, err.status_code
        except requests.exceptions.ConnectionError as err:
            current_app.logger.warning(err)
            return {
                       "message": "BPM Service Unavailable",
                   }, HTTPStatus.SERVICE_UNAVAILABLE


@cors_preflight("POST,OPTIONS")
@API.route("/<int:application_id>/withdraw/<string:role>", methods=["POST", "OPTIONS"])
class ApplicationWithdraw(Resource):
    """Resource for resubmit application."""

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.doc(body=application_resubmit_model)
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    @API.response(403, "FORBIDDEN:- Permission denied")
    def post(application_id: int, role: str, **kwargs):
        ### 1. Get user data
        current_app.logger.debug("1. Get user data")
        user: UserContext = kwargs["user"]
        current_app.logger.debug(application_id)
        current_app.logger.debug(user.user_name)

        ### 2. Get application
        current_app.logger.debug("2. Get application")
        application = Application.query.filter_by(
            id=application_id,
            created_by=user.user_name
        ).first()

        current_app.logger.debug("3. Check if an application exists")
        current_app.logger.debug(application)
        current_app.logger.debug(application.process_instance_id)
        ### 3. Check if application exists
        if not application or not application.process_instance_id:
            current_app.logger.debug("4. Application doesn't exist return not found")
            return {
                       "type": "Not found",
                       "message": f"No application with id {application_id} found for user"
                   }, HTTPStatus.NOT_FOUND

        ### 4. Application exists get it's current data
        current_app.logger.debug("4. Application exist bpm variables")
        process_variables = BPMService.get_process_variables(application.process_instance_id, token=None)

        ### 5. Generate correct message
        current_app.logger.debug("5. Generate correct message for withdraw in BPM")
        message_name = None
        relevant_form_path = None
        if role == "owner":
            message_name = "owner_invitation_withdrawn"
            relevant_form_path = f"{user.tenant_key}-ownersignform"
        elif role == "trustee":
            message_name = "trustee_invitation_withdrawn"
            relevant_form_path = f"{user.tenant_key}-trusteesignform"

        if not message_name:
            return {
                       "type": "Bad Request Error",
                       "message": f"No message bound for supplied role {role}"
                   }, HTTPStatus.BAD_REQUEST

        message = {
            "messageName": message_name,
            "processInstanceId": application.process_instance_id
        }
        current_app.logger.debug(message)

        ### 6.Send delete message to camunda
        current_app.logger.debug("6. Send withdraw message to BPM")
        ### Message response is either Empty request, with status code 204,
        ### Or an error response with status code 400
        response = BPMService.send_message(data=message, token=None)

        ### 7. Check is response successfull
        current_app.logger.debug("7. Check is BPM response successful")
        current_app.logger.debug(response)
        if response != True:
            ### 8. BPM Response was unssucessfull
            current_app.logger.debug("8. BPM Response was unsuccessful")
            return {
                       "type": "Bad Request Error",
                       "message": f"An error occurred when sending message to camunda"
                   }, HTTPStatus.BAD_REQUEST

        ### 8. Get invited user by email
        current_app.logger.debug("8. Get email of invited user ")
        if role == "owner":
            withdrawn_applicant_email = process_variables.get("propertyOwnerEmail", {}).get("value")
        elif role == "trustee":
            withdrawn_applicant_email = process_variables.get("trusteeEmail", {}).get("value")
        current_app.logger.debug(f"for {role} - email - {withdrawn_applicant_email}")

        ### 9. Get reference number
        current_app.logger.debug("9. Get reference number")
        reference_number = process_variables.get("reference_number", {}).get("value")
        current_app.logger.debug(f"reference_number = {reference_number}")
        if not reference_number:
            return {
                       "type": "Bad Request Error",
                       "message": f"No reference number attached to process instance."
                   }, HTTPStatus.BAD_REQUEST

        ### 10. Check if there is a user in keycloak with withdrawn_applicant_email
        current_app.logger.debug("10. Get user by email")
        keycloak_client = KeycloakAdminAPIService()
        url_path = f"users?email={withdrawn_applicant_email}&exact={True}"
        response = keycloak_client.get_request(url_path=url_path)
        current_app.logger.debug(f"Response - {response}")

        if not response:
            ### 11. No user found to withdraw, everything is okay
            current_app.logger.debug("11. No user found, ergo no application to withdraw")
            return "Withdrawn successfully", HTTPStatus.OK
        
        ### 11. Get user 
        current_app.logger.debug(f"11. Get person identifier")
        withdrawn_user = response[0]
        attributes = withdrawn_user.get("attributes")
        withdrawn_person_identifier_list = attributes.get("personIdentifier", None)
        withdrawn_person_identifier = withdrawn_person_identifier_list[0] if withdrawn_person_identifier_list else None
        current_app.logger.debug(f"personIdentifier - {withdrawn_person_identifier}")

        if withdrawn_person_identifier is None:
            current_app.logger.debug(f"No person identifier, bound to withdrawn_user")
            current_app.logger.error(f"No personIdentifier for withdrawn_user with email - {withdrawn_applicant_email}")
            return "Withdrawn successfully", HTTPStatus.OK

        ### 12. Init formio
        current_app.logger.debug("12. Init formio client")
        formio_client = FormioServiceExtended()

        ### 13. Init access token
        current_app.logger.debug(f"13. Generate access token")
        formio_token = formio_client.get_formio_access_token()

        ### 14. Generated formio resource path
        formio_file_resource_path = user.tenant_key + "-" + current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
        current_app.logger.debug(f"14. Generate file_path - {formio_file_resource_path}")

        ### 15. Fetch submission
        current_app.logger.debug(f"15. Fetch formio submission")
        current_app.logger.debug(f"data.userId - {withdrawn_person_identifier}")
        current_app.logger.debug(f"data.referenceId - {reference_number}")
        options_dict = {
            "data.userId": withdrawn_person_identifier,
            "data.referenceId": reference_number
        }

        if relevant_form_path:
            current_app.logger.debug(f"data.formPath - {relevant_form_path}")
            options_dict["data.formPath"] = relevant_form_path

        formio_resource = formio_client.find_and_fetch_submission(
            form_path=formio_file_resource_path,
            formio_token=formio_token,
            options=urlencode(options_dict)
        )

        ### 15. We have a formio resource to withdraw
        if not formio_resource:
            current_app.logger.debug(f"16. No formio resource, withdrawn successfully")
            return "Withdrawn successfully", HTTPStatus.OK

        ### 16. Getting relevant formio data from resource
        current_app.logger.debug(f"16. Getting relevant formio_data")
        formio_data = formio_resource.get("data", {})
        evrotrust_thread_id = formio_data.get("evrotrustThreadId")
        evrotrust_status = formio_data.get("status")
        withdrawn_application_id = formio_data.get("applicationId", None)
        
        ### 17. This is in the case that the first user has submitted his own email
        if withdrawn_application_id == application_id:
            current_app.logger.debug("17. This is in the case that the first user has submitted his own email")
            return "Withdrawn successfully", HTTPStatus.OK

        current_app.logger.debug(f"evrotrustThreadId - {evrotrust_thread_id}")
        current_app.logger.debug(f"evrotrustStatus - {evrotrust_status}")

        ### 17. If the evrotrust document is currently signing, withdraw it
        current_app.logger.debug(f"17. Withdrawing document from ET")
        if evrotrust_thread_id and evrotrust_status == 'signing':
            client = EurotrustIntegrationsService()
            evrotrust_response = client.withdraw_document(evrotrust_thread_id)
            current_app.logger.debug(evrotrust_response)

        ### 18. Removing formio resource
        current_app.logger.debug("18. Deleting formio resource")
        formio_resource_id = formio_resource.get("_id")
        formio_response = formio_client.delete_submission_formio(
            formio_file_resource_path,
            formio_submission_id=formio_resource_id
        )
        current_app.logger.debug(formio_response)

        
        ### 19. Delete application, application history and draft row within python DB
        current_app.logger.debug("19. Deleting application from Python DB")
        if withdrawn_application_id:
            Draft.query.filter_by(application_id=withdrawn_application_id).delete()
            Application.query.filter_by(id=withdrawn_application_id).delete()
            ApplicationHistory.query.filter_by(application_id=withdrawn_application_id).delete()
        db.session.commit()

        return "Withdrawn successfully", HTTPStatus.OK


@cors_preflight("POST,OPTIONS")
@API.route("/<int:application_id>/reassign", methods=["POST", "OPTIONS"])
class ApplicationResubmitById(Resource):
    """Resource for resubmit application."""

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(body=application_resubmit_model)
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    @API.response(403, "FORBIDDEN:- Permission denied")
    def post(application_id: int):
        """Resubmit application."""
        try:
            submission_data = request.get_json()
            application = ApplicationService.get_application_form_mapper_by_id(
                application_id
            )
            bpm_service = BPMService()
            bpm_service.unclaim_task(task_id=application.get('task_variable'), data={}, token=request.headers["Authorization"])
            bpm_service.claim_task(task_id=application.get('task_variable'),
                                   data={"userId": submission_data['assignee']},
                                   token=request.headers["Authorization"])
            current_app.logger.info(application)
            return "Task updated successfully.", HTTPStatus.OK
        except PermissionError as err:
            response, status = (
                {
                    "type": "Permission Denied",
                    "message": f"Access to application id - {application_id} is prohibited.",
                },
                HTTPStatus.FORBIDDEN,
            )
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BusinessException as err:
            return err.error, err.status_code
        except requests.exceptions.ConnectionError as err:
            current_app.logger.warning(err)
            return {
                       "message": "BPM Service Unavailable",
                   }, HTTPStatus.SERVICE_UNAVAILABLE
