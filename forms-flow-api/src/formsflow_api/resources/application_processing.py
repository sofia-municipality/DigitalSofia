"""API endpoints for processing applications resource."""

from http import HTTPStatus
import base64
from io import BytesIO
import mimetypes
from datetime import datetime, timedelta

from flask import current_app, request
from werkzeug.datastructures import FileStorage
from flask_restx import Namespace, Resource
from marshmallow import ValidationError

from formsflow_api_utils.utils import (
    cors_preflight,
    profiletime,
    user_context,
    auth,
    UserContext,
)
from formsflow_api.resources.application import application_base_model
from formsflow_api.schemas import (
    ApplicationProcessingCreateRequest,
    ApplicationDocumentProcessedRequest,
    ApplicationProcessingChangeAssigneesRequest,
)
from formsflow_api.services import AcstreService, OtherFileService, DocumentsService
from formsflow_api.services.external import KeycloakAdminAPIService
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.utils.enums import SignatureSourceEnum
from formsflow_api.services.receipts import ReceiptService

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
        if "acstreAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an acstreAdmin.",
                },
                HTTPStatus.UNAUTHORIZED,
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
            application_json["valid_groups"] = valid_groups
            create_application_process_schema = ApplicationProcessingCreateRequest()
            data = create_application_process_schema.load(application_json)

            current_app.logger.debug("data application_json_submission")
            current_app.logger.debug(data["application_json_submission"])

            current_app.logger.debug("data [application_json_submission][data][applicationId]")
            current_app.logger.debug(data["application_json_submission"]["data"]["applicationId"])
            current_app.logger.debug(type(data["application_json_submission"]["data"]["applicationId"]))
            current_app.logger.debug(data["application_json_submission"]["data"]["caseDataSource"]["data"]["applicationId"])
            current_app.logger.debug("data[application_json_submission][data][caseDataSource][data][applicationId]")
            current_app.logger.debug(type(data["application_json_submission"]["data"]["caseDataSource"]["data"]["applicationId"]))
            main_application_id = (
                data["application_json_submission"]["data"]["applicationId"]
                if not isinstance(
                    data["application_json_submission"]["data"]["applicationId"], str
                )
                else data["application_json_submission"]["data"]["caseDataSource"][
                    "data"
                ]["applicationId"]
            )
            current_app.logger.debug("main_application_id")
            current_app.logger.debug(main_application_id)

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
                assignees=data["assignees"],
            )

            application_id = application_response["id"]

            ### 7. Set assignees
            acstre_service.set_application_assignees(
                application_id=application_id, assignees=data["assignees"]
            )

            ### 8. Check if process originated within digitallSofia
            has_digitall_sofia_origin = data.get("has_digitall_sofia_origin")
            current_app.logger.debug(f"Setting origin - {has_digitall_sofia_origin}")
            if has_digitall_sofia_origin:
                acstre_service.set_application_origin_process_instance(
                    application_id=application_id,
                    origin_process_instance_id=data.get("origin_process_instance_id"),
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
                        stream=stream, filename=file["name"], content_type=mime_type
                    )
                    other_file = other_file_service.save_file(
                        user_id=user_id,
                        file=file,
                        application_id=application_id,
                        additional_path=additional_path,
                        created_at=request_datetime,
                    )

                    list_of_formio_url_files.append(
                        {
                            "url": other_file.file_url,
                            "name": other_file.file_name,
                            "size": other_file.file_size,
                        }
                    )

                current_app.logger.debug("Add formio files")
                acstre_service.add_other_files_to_application(
                    application_id, other_files=list_of_formio_url_files
                )

            is_with_payment = data["has_payment"]
            acstre_service.send_is_with_payment_message(
                main_application_id, is_with_payment
            )

            return ({"id": application_id}), HTTPStatus.CREATED
        except ValidationError as err:
            return (
                {
                    "type": "Bad request error",
                    "message": "Unprocessable Entity",
                    "data": err.messages,
                },
                HTTPStatus.UNPROCESSABLE_ENTITY,
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
        if "acstreAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an acstreAdmin.",
                },
                HTTPStatus.UNAUTHORIZED,
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
                    "data": err.messages,
                },
                HTTPStatus.UNPROCESSABLE_ENTITY,
            )

        acstre_service = AcstreService()

        status_response = acstre_service.get_application_status(
            application_id=application_id, tenant_key=user.tenant_key
        )

        if status_response is None:
            return (
                {
                    "type": "Not found",
                    "message": f"Application {application_id} not found",
                },
                HTTPStatus.NOT_FOUND,
            )

        if status_response["status"] not in ["ready", "denied", "needs-agreement"]:
            return (
                {
                    "type": "Not ready",
                    "message": f"Application {application_id} not ready, denied or needs-agreement",
                },
                HTTPStatus.FORBIDDEN,
            )

        response = acstre_service.document_processed_application(
            user_id=user.token_info.get("preferred_username"),
            application_id=application_id,
            status=data.get("status"),
            description=data.get("description"),
            documents=data.get("documents"),
        )

        if not response:
            return (
                {
                    "type": "Not found",
                    "message": f"Application {application_id} not found",
                },
                HTTPStatus.NOT_FOUND,
            )

        if data.get("status") == "completed":
            try:
                main_application_id = response["main_application_id"]

                # Find origin FormIO submission
                formio_service = FormioServiceExtended()
                formio_token = formio_service.get_formio_access_token()
                form_path = user.tenant_key + "-generated-files"

                formio_resp = formio_service.get_submissions(
                    form_path=form_path,
                    formio_token=formio_token,
                    params={
                        "data.applicationId": str(main_application_id),
                        "data.signatureSource": SignatureSourceEnum.DIGITAL_SOFIA.value,
                    },
                )[0]

                # Check if there is a submission
                if formio_resp:
                    origin_submission = formio_resp[0]
                    origin_user_id = origin_submission["data"]["userId"]

                    user_identifier = origin_user_id.replace("PNOBG-", "")

                    current_app.logger.debug(
                        "------------- User_identifier is: -------------"
                    )
                    current_app.logger.debug(user_identifier)

                    document_to_deliver = data["documents"][0]
                    document_name = document_to_deliver["name"]
                    document_base64 = document_to_deliver["file"]
                    thread_id, transaction_id = (
                        DocumentsService.deliver_document_to_eurotrust(
                            document_base64,
                            document_name,
                            user_identifier,
                            "Удостоверение за промяна на адресна регистрация.",
                        )
                    )

                    # Update status in formio
                    now = datetime.utcnow()
                    expire = now + timedelta(hours=4)
                    iso_datetime_expire = expire.isoformat()

                    # Create document submission
                    document_to_deliver = data["documents"][0]
                    document_name = document_to_deliver["name"]
                    document_base64 = document_to_deliver["file"]

                    file_form_id = formio_service.fetch_form_id_by_path(
                        form_path, formio_token
                    )

                    new_submission_data = {
                        "formId": file_form_id,
                        "data": {
                            "applicationId": str(application_id),
                            "file": [
                                {
                                    "name": document_name,
                                    "originalName": document_name,
                                    "size": len(str(document_base64)),
                                    "storage": "base64",
                                    "type": "application/pdf",
                                    "url": "data:application/pdf;base64,"
                                    + document_base64,
                                }
                            ],
                            "status": "delivering",
                            "userId": origin_user_id,
                            "referenceId": "234234",
                            "formPath": form_path,
                            "signatureSource": "digitalSofia",
                            "validUntil": iso_datetime_expire,
                            "evrotrustThreadId": thread_id,
                            "evrotrustTransactionId": transaction_id,
                        },
                    }

                    new_submission_resp = formio_service.post_submission(
                        data=new_submission_data, formio_token=formio_token
                    )

                    current_app.logger.debug("Transaction signature source")
                    document_service = DocumentsService()
                    delivering_status = document_service.get_document_status(
                        "Delivering"
                    )
                    document_transaction = (
                        document_service.create_and_save_document_transaction(
                            transaction_id,
                            thread_id,
                            user.tenant_key,
                            delivering_status.id,
                            new_submission_resp["_id"],
                            origin_user_id,
                            str(application_id),
                            new_submission_resp["form"],
                            "digitalSofia",
                        )
                    )

                    receipt_service = ReceiptService()
                    receipt_service.wait_for_notification_in_separate_process(
                        document_transaction
                    )

            except Exception as e:
                current_app.logger.debug("Error during document delivery logic!")
                current_app.logger.debug(e)

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
        if "acstreAdmin" not in roles:
            return (
                {
                    "type": "Invalid Role Error",
                    "message": "Authorized Account is not an acstreAdmin.",
                },
                HTTPStatus.UNAUTHORIZED,
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
            application_json["valid_groups"] = valid_groups

            change_assignees_request = ApplicationProcessingChangeAssigneesRequest()
            data = change_assignees_request.load(application_json)

            new_assignees = data["assignees"]

            acstre_service = AcstreService()

            status_response = acstre_service.get_application_status(
                application_id=application_id, tenant_key=user.tenant_key
            )

            if (
                status_response is not None
                and status_response["status"].lower() != "not-ready"
            ):
                return {
                    "type": "Operation not allowed",
                    "message": f"Change of application processing assignee denied for application {application_id}.",
                }, HTTPStatus.FORBIDDEN

            response = acstre_service.set_application_assignees(
                application_id=application_id, assignees=new_assignees
            )

            return {}, HTTPStatus.OK
        except ValidationError as err:
            return (
                {
                    "type": "Bad request error",
                    "message": "Unprocessable Entity",
                    "data": err.messages,
                },
                HTTPStatus.UNPROCESSABLE_ENTITY,
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
                    "message": "Authorized Account is not an acstreAdmin.",
                },
                HTTPStatus.UNAUTHORIZED,
            )

        service = AcstreService()
        response = service.get_application_status(application_id, user.tenant_key)

        if not response:
            return (
                {
                    "type": "Not found",
                    "message": f"Application {application_id} not found",
                },
                HTTPStatus.NOT_FOUND,
            )

        if response["status"]:
            return response

        return (
            {"type": "Invalid Status", "message": "Invalid status for application."},
            HTTPStatus.INTERNAL_SERVER_ERROR,
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
                    "message": "Authorized Account is not an acstreAdmin.",
                },
                HTTPStatus.UNAUTHORIZED,
            )

        submission_data = request.get_json()
        application_ids = submission_data.get("applicationIds")

        to_return = []
        acstre_service = AcstreService()
        for application_id in application_ids:
            response = acstre_service.get_application_status(
                application_id, user.tenant_key
            )
            if response:
                response["applicationId"] = application_id
                to_return.append(response)

        return {"data": to_return}, HTTPStatus.OK


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
            if submission_json["data"]["caseDataSource"]["data"]["serviceId"]:

                acstre_service = AcstreService()
                application_response = (
                    acstre_service.create_application_on_submission_data(
                        tenant_key, submission_json, [submission_json["assignees"]]
                    )
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
