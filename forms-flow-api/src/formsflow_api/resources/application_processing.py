"""API endpoints for processing applications resource."""

from http import HTTPStatus
import base64
from io import BytesIO
import mimetypes
from datetime import datetime

from flask import current_app, request
from werkzeug.datastructures import FileStorage
from flask_restx import Namespace, Resource
from formsflow_api_utils.utils import (
    cors_preflight,
    profiletime, 
    user_context, 
    auth,
    UserContext
)

from formsflow_api.models import Application
from formsflow_api.resources.application import application_base_model
from formsflow_api.schemas import (
    ApplicationSchema,
    ApplicationProcessingCreateRequest,
    ApplicationDocumentProcessedRequest,
    ApplicationProcessingChangeAssigneesRequest
)
from formsflow_api.services import AcstreService, OtherFileService
from formsflow_api.services.external import KeycloakAdminAPIService, BPMService
from marshmallow import ValidationError

API = Namespace("ApplicationProcessing", description="ApplicationProcessing")


@cors_preflight("POST,OPTIONS")
@API.route("/create", methods=["POST", "OPTIONS"])
class ApplicationResource(Resource):

    @staticmethod
    @auth.require
    @user_context
    @profiletime
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
        ### 1. Get acstre user
        user: UserContext = kwargs["user"]
        tenant_key: str = user.tenant_key

        ### 2. Verify user has needed roles
        realm_access = user.token_info.get("realm_access", {})
        roles = realm_access.get("roles", [])
        if "axterAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an axterAdmin."
                },
                HTTPStatus.UNAUTHORIZED
            )


        request_datetime = datetime.now()
        acstre_keycloak_id = user.token_info.get("sub", None)
        keycloak_client = KeycloakAdminAPIService()

        ### 3. Get account groups
        user_groups = keycloak_client.get_user_groups(acstre_keycloak_id)
        valid_groups = []
        for group in user_groups:
            name = group.get("name") 
            group_id = group.get("id")

            if not name.startswith("formsflow") and not name.startswith("camunda"):
                valid_groups.append(group_id)

        try:
            ### 4. Get application json submission
            application_json = request.get_json()
            application_json['valid_groups'] = valid_groups
            create_application_process_schema = ApplicationProcessingCreateRequest()
            data = create_application_process_schema.load(application_json)

            ### 5. Init Acstre Service
            acstre_service = AcstreService()

            ### 6. Create application
            application_response = acstre_service.create_application_on_submission_data(
                tenant_key=tenant_key,
                submission_json=data["application_json_submission"],
                externalId=data["external_id"],
                hasPayment=data["has_payment"],
                taxAmount=data["tax_amount"],
                paymentTill=data["payment_till"],
                assignees=data["assignees"]
            )

            application_id = application_response["id"]
            
            ### 7. Set assignees
            acstre_service.set_application_assignees(
                application_id=application_id,
                assignees=data["assignees"]
            )

            ### 8. Check if process originated within digitallSofia
            has_digitall_sofia_origin = data.get("has_digitall_sofia_origin")
            current_app.logger.debug(f"Setting origin - {has_digitall_sofia_origin}")
            if has_digitall_sofia_origin:
                acstre_service.set_application_origin_process_instance(
                    application_id=application_id,
                    origin_process_instance_id=data.get("origin_process_instance_id")
                )

            ### 9. Get other files
            other_files = data.get("other_files", [])
            current_app.logger.debug(f"Check if we have other files {len(other_files)}")            

            ### 10. If other files exist, handle them
            if other_files:
                other_file_service = OtherFileService()
                user_id = user.token_info.get("preferred_username")
                list_of_formio_url_files = []
                datetime_folder = request_datetime.strftime("%Y-%m-%d %H-%M-%S")
                additional_path = f"{application_id}/{datetime_folder}/"


                for file in other_files:
                    base64_string = file["file"]
                    binary_data = base64.b64decode(base64_string)

                    stream = BytesIO(binary_data)

                    mime_type, encoding = mimetypes.guess_type(file["name"])

                    file = FileStorage(
                        stream=stream, 
                        filename=file["name"], 
                        content_type=mime_type
                    )
                    other_file = other_file_service.save_file(
                        user_id=user_id,
                        file=file,
                        application_id=application_id,
                        additional_path=additional_path,
                        created_at=request_datetime
                    )

                    list_of_formio_url_files.append(
                        {
                            "url": other_file.file_url,
                            "name": other_file.file_name,
                            "size": other_file.file_size
                        }
                    )

                current_app.logger.debug("Add formio files")
                acstre_service.add_other_files_to_application(
                    application_id,
                    other_files=list_of_formio_url_files
                )

            return (
                {
                    "id": application_id
                }
            ), HTTPStatus.CREATED
        except ValidationError as err:
            return (
                {
                    "type": "Bad request error",
                    "message": "Unprocessable Entity",
                    "data": err.messages
                },
                HTTPStatus.UNPROCESSABLE_ENTITY
            )
        
        
@cors_preflight("POST, OPTIONS")
@API.route("/<int:application_id>/documents-processed", methods=["POST", "OPTIONS"])
class ApplicationProcessingDocumentProcessed(Resource):
    
    @staticmethod
    @auth.require
    @user_context
    @profiletime
    def post(application_id, **kwargs):
        user: UserContext = kwargs["user"]

        realm_access = user.token_info.get("realm_access", {})
        roles = realm_access.get("roles", [])
        if "axterAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an axterAdmin."
                },
                HTTPStatus.UNAUTHORIZED
            )
        
        try:

            schema = ApplicationDocumentProcessedRequest()
            data = schema.load(request.get_json())
            
            # return data
        except ValidationError as err:
            return (
                {
                    "type": "Bad request error",
                    "message": "Unprocessable Entity",
                    "data": err.messages
                },
                HTTPStatus.UNPROCESSABLE_ENTITY
            )


        acstre_service = AcstreService()

        status_response = acstre_service.get_application_status(
            application_id=application_id,
            tenant_key=user.tenant_key
        )

        if status_response is None:
            return (
                {
                    "type": "Not found",
                    "message": f"Application {application_id} not found"
                },
                HTTPStatus.NOT_FOUND
            )

        if status_response["status"] not in ["ready", "denied", "needs-agreement"]:
            return (
                {
                    "type": "Not ready",
                    "message": f"Application {application_id} not ready, denied or needs-agreement"
                },
                HTTPStatus.FORBIDDEN
            )
            
        response = acstre_service.document_processed_application(
            user_id=user.token_info.get("preferred_username"),
            application_id=application_id, 
            status=data.get("status"), 
            description=data.get("description"), 
            documents=data.get("documents")
        )

        if not response:
            return (
                {
                    "type": "Not found",
                    "message": f"Application {application_id} not found"
                },
                HTTPStatus.NOT_FOUND
            )

        return response, HTTPStatus.OK


@cors_preflight("POST, OPTIONS")
@API.route("/<int:application_id>/changeAssignees", methods=["POST", "OPTIONS"])
class ApplicationProcessingChangeAssignees(Resource):

    @staticmethod
    @auth.require
    @user_context
    @profiletime
    def post(application_id, **kwargs):
        ### 1. Get acstre user
        user: UserContext = kwargs["user"]
        tenant_key: str = user.tenant_key


        ### 2. Verify user has needed roles
        realm_access = user.token_info.get("realm_access", {})
        roles = realm_access.get("roles", [])
        if "axterAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an axterAdmin."
                },
                HTTPStatus.UNAUTHORIZED
            )

        request_datetime = datetime.now()
        acstre_keycloak_id = user.token_info.get("sub", None)
        current_app.logger.debug(acstre_keycloak_id)
        keycloak_client = KeycloakAdminAPIService()

        user_groups = keycloak_client.get_user_groups(acstre_keycloak_id)
        current_app.logger.debug(user_groups)
        valid_groups = []
        for group in user_groups:
            name = group.get("name") 

            if not name.startswith("formsflow"):
                valid_groups.append(group.get("id"))

        try:
            application_json = request.get_json()
            application_json['valid_groups'] = valid_groups

            change_assignees_request = ApplicationProcessingChangeAssigneesRequest()
            data = change_assignees_request.load(application_json)


            new_assignees = data["assignees"]
            
            acstre_service = AcstreService()

            status_response = acstre_service.get_application_status(
                application_id=application_id,
                tenant_key=user.tenant_key)
            
            if status_response is not None and status_response["status"].lower() != "not-ready":
                return {
                    "type": "Operation not allowed",
                    "message": f"Change of application processing assignee denied for application {application_id}."
                }, HTTPStatus.FORBIDDEN

            response = acstre_service.set_application_assignees(
                application_id=application_id,
                assignees=new_assignees
            )

            return {}, HTTPStatus.OK
        except ValidationError as err:
            return (
                {
                    "type": "Bad request error",
                    "message": "Unprocessable Entity",
                    "data": err.messages
                },
                HTTPStatus.UNPROCESSABLE_ENTITY
            )


@cors_preflight("GET,OPTIONS")
@API.route("/<int:application_id>/status", methods=["GET", "OPTIONS"])
class ApplicationProcessingSingleStatus(Resource):

    @staticmethod
    @auth.require
    @user_context
    @profiletime
    def get(application_id, **kwargs):
        user: UserContext = kwargs["user"]

        realm_access = user.token_info.get("realm_access", {})
        roles = realm_access.get("roles", [])
        if "acstreAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an acstreAdmin."
                },
                HTTPStatus.UNAUTHORIZED
            )
        
        service = AcstreService()
        response = service.get_application_status(application_id, user.tenant_key)
        
        if not response:
            return (
            {
                "type": "Not found",
                "message": f"Application {application_id} not found"
            },
            HTTPStatus.NOT_FOUND
        )

        if response["status"]:
            return response
        
        return (
            {
                "type": "Invalid Status",
                "message": "Invalid status for application."
            },
            HTTPStatus.INTERNAL_SERVER_ERROR
        )

@cors_preflight("POST,OPTIONS")
@API.route("/multiple-status", methods=["POST", "OPTIONS"])
class ApplicationProcessingSingleStatus(Resource):

    @staticmethod
    @auth.require
    @user_context
    @profiletime
    def post(**kwargs):
        user: UserContext = kwargs["user"]

        realm_access = user.token_info.get("realm_access", {})
        roles = realm_access.get("roles", [])
        if "acstreAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an acstreAdmin."
                },
                HTTPStatus.UNAUTHORIZED
            )
        
        submission_data = request.get_json()
        application_ids = submission_data.get("applicationIds")

        to_return = []
        acstre_service = AcstreService()
        for application_id in application_ids:
            response = acstre_service.get_application_status(application_id, user.tenant_key)
            if response:
                response["applicationId"] = application_id
                to_return.append(response)

        return {
            "data": to_return
        }, HTTPStatus.OK
    


@cors_preflight("POST,OPTIONS")
@API.route("/create/demo", methods=["POST", "OPTIONS"])
class ApplicationProcessingDemo(Resource):
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
        user: UserContext = kwargs["user"]
        tenant_key: str = user.tenant_key

        try:
            submission_json = request.get_json()
            if submission_json['data']['caseDataSource']['data']['serviceId']:

                
                acstre_service = AcstreService()
                application_response = acstre_service.create_application_on_submission_data(
                    tenant_key,
                    submission_json, 
                    [submission_json["assignees"]]
                )

                return application_response, HTTPStatus.CREATED
            else:
                response, status = {
                                    "type": "Bad request error",
                                    "message": "Invalid application request passed",
                                }, HTTPStatus.BAD_REQUEST
                return response, status
        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                    "type": "Bad request error",
                                    "message": "Invalid submission request passed",
                                }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status
