import re
import base64
import json
import datetime
from io import BytesIO
from xml.etree import ElementTree
from PyPDF2 import PdfReader
from http import HTTPStatus
from flask import current_app, request, make_response
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    profiletime
)
from formsflow_api.schemas import DocumentsListSchema
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api.exceptions import EurotrustException
from formsflow_api.services import FormioServiceExtended, DocumentsService, KeycloakAdminAPIService
from formsflow_api.services.external import EurotrustIntegrationsService
from formsflow_api.models import DocumentStatus


API = Namespace("Documents", description="Temporary documents EP")

document = API.model(
    "Document",
    {
        "id"
    }
)

VALID_STATUSES = [
    "unsigned",
    "signing",
    "signed",
    "expired",
    "rejected"
]

VALID_TYPES = [
    "eAuth"
    "eIdentity"
    "OKEP"
    "eDelivery"
]

@cors_preflight("GET,OPTIONS")
@API.route("", methods=["GET", "OPTIONS"])
class DocumentResource(Resource):
    """Resource for managing documents."""
    
    @classmethod
    @auth.require
    @profiletime
    @user_context
    @API.doc(
        params={
            "status": {
                "in": "query",
                "description": "Status filter. Valid statuses are: " + ",".join(VALID_STATUSES),
                "default": None
            },
            "createdAfter": {
                "in": "query",
                "description": "Created after filter: ",
                "default": None
            },
            "cursor": {
                "in": "query",
                "description": "Current page",
                "default": None
            },
        }
    )
    def get(cls, **kwargs):
        ### 1. Get user data
        current_app.logger.debug("1. Get user data")
        user: UserContext = kwargs["user"]
        is_verified = getattr(user, 'is_verified', False)
        current_app.logger.debug(f"Is verified - {is_verified}")
        person_identifier = user.token_info["personIdentifier"]
        tenant_key = user.tenant_key

        
        current_app.logger.debug("2. Check person identifier validity")
        match = re.findall(r"PNOBG-(\d{10})$", person_identifier)
        if not match:
            current_app.logger.debug(BusinessException)
            raise BusinessException(
                "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
            )


        current_app.logger.debug("3. See if the user is verified")
        if not is_verified:
            current_app.logger.debug("4. Try and get an identity_request if exists")
            person_identifier = match[0]
            document_client = DocumentsService()
            identity_request = document_client.get_identity_request(
                person_identifier=person_identifier, 
                tenant_key=tenant_key
            )

            if not identity_request:
                identity_request = document_client.create_identity_request(
                    person_identifier=person_identifier, 
                    tenant_key=tenant_key
                )
            
            current_app.logger.debug("5. Spoof response")
            return_dict = {
                "documents": [
                    {
                        "created": identity_request.created.isoformat(),
                        "modified": identity_request.modified.isoformat(),
                        "expired": None,
                        "validUntill": identity_request.valid_untill.isoformat(),
                        "signed": None,
                        "rejected": None,
                        "applicationId": None,
                        "evrotrustThreadId": identity_request.thread_id,
                        "evrotrustTransactionId": identity_request.transaction_id,
                        "fileName": None,
                        "status": "pending",
                        "formioId": None,
                        "referenceNumber": None,
                        "fileUrl": None,
                    }
                ],
                "pagination": {
                    "cursor": None
                }
            }

            return (return_dict, HTTPStatus.OK)

        
        ## Paginate data
        current_app.logger.debug("4. User is verified, get cursor parameters")
        args = DocumentsListSchema().load(request.args) or {}
        new_cursor = args.get('cursor', None)
        if new_cursor:
            pagination_values = base64.urlsafe_b64decode(new_cursor)
            pagination_values = pagination_values.decode()
            pagination_values = json.loads(pagination_values)
        else:
            pagination_values = {
                "skip": 0,
                "limit": args.get("limit", 5)
            }

        skip = pagination_values.get('skip')
        limit = pagination_values.get("limit")
        
        current_app.logger.debug("5. Init formio client")
        formio_client = FormioServiceExtended()
        formio_token = formio_client.get_formio_access_token()
        file_formio_path = user.tenant_key + "-" + current_app.config.get('FORMIO_FILE_RESOURCE_PATH')

        status = args.get("status", [])
        if status:
            status = status.split(",")

        current_app.logger.debug("6. Get submissions, getting one more to see if there are further pages")
        related_file_submissions, formio_status = formio_client.get_submissions(
            file_formio_path, 
            formio_token=formio_token,
            person_identifier=person_identifier,
            select=['_id', 'data', 'created', 'modified'],
            status=status,
            skip=skip,
            limit=limit + 1, ### Get one more to check if we have more pages
            created_after=args.get("created_after")
        )

        current_app.logger.debug("7. Encode new cursor")
        ### If the returned submissions (limit + 1) are more than the limit, there is another page
        if len(related_file_submissions) > limit:
            skip += limit 
            next_cursor = base64.urlsafe_b64encode(
                    json.dumps(
                        {"skip": skip, "limit": limit}
                    ).encode()
                ).decode()
        else:
            next_cursor=None


        current_app.logger.debug("8. Format data")
        returned_submissions = []
        current_index = 0

        api_base = current_app.config.get("FORMSFLOW_API_URL")

        for submission in related_file_submissions:
            if current_index == limit:
                break
            current_index += 1
            submission_data = submission.get("data", {})
            file_list = submission_data.get("file", [])
            if not file_list:
                file_data = {}
            else:
                file_data = file_list[0]

            formio_id = submission.get("_id")
            returned_submissions.append(
                {
                    "created": submission.get("created"),
                    "modified": submission.get("modified"),
                    "expired": submission_data.get("expired"),
                    "validUntill": submission_data.get("validUntill"),
                    "signed": submission_data.get("signed"),
                    "rejected": submission_data.get("rejected"),
                    "applicationId": submission_data.get("applicationId"),
                    "evrotrustThreadId": submission_data.get("evrotrustThreadId"),
                    "evrotrustTransactionId": submission_data.get("evrotrustTransactionId"),
                    "fileName": file_data.get("name"),
                    "status": submission_data.get("status"),
                    "formioId": submission.get("_id"),

                    ### To add
                    "referenceNumber": submission_data.get("referenceId", "TODO"),
                    "fileUrl": f"{api_base}/documents/{formio_id}/serve",
                }
            )

        response, formio_status = {
                "documents": returned_submissions,
                "pagination": {
                    "cursor": next_cursor
                }
            }, formio_status
        return response, formio_status


@cors_preflight("GET,OPTIONS")
@API.route("/<string:submission_formio_id>/serve", methods=["GET", "OPTIONS"])
class DocumentServeBase64(Resource):

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response( 400, "BAD_REQUEST:- Invalid request.")
    @API.response( 404, "NOT_FOUND:- No document found to check status.")
    def get(submission_formio_id: str, **kwargs):
        try:
            user: UserContext = kwargs["user"]
            person_identifier = user.token_info["personIdentifier"]
            formio_client = FormioServiceExtended()
            
            form_path = user.tenant_key + "-" + current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
            formio_token = formio_client.get_formio_access_token()
            file_form_id = formio_client.fetch_form_id_by_path(form_path=form_path, formio_token=formio_token)

            response = formio_client.debug_get_submission(
                data={
                    "form_id": file_form_id,
                    "sub_id": submission_formio_id
                },
                formio_token=formio_token
            )

            submission_data = response.get("data")
            file_data = submission_data.get("file", [])[-1]
            user_id = submission_data.get("userId")

            if (person_identifier != user_id) or (not file_data):
                return {
                    "type": "Not found",
                    "message": f"No document with supplied formioId {submission_formio_id} found"
                }, HTTPStatus.NOT_FOUND
            
            file = file_data['url']
            file = re.split(";|,", file)

            ### If the string is as following - data:application/pdf;base64,JVBERi0xLjQKJdPr6eEKMSAwIG9ia...
            ### The above split will make a file corresponding to:
            ### 0 - data:application/pdf - file data type
            ### 1 - base64 - encoding type
            ### 2 - JVBERi0xLjQKJdPr6eEKMSAw... - 

            image_binary = base64.b64decode(file[2])
            response = make_response(image_binary)
            current_app.logger.debug(file_data['name'])
            response.headers.set('Content-Type', file_data['type'])
            response.headers.set(
                'Content-Disposition', 'inline', filename=file_data['name'].encode("utf-8"))
            return response
    
        except BusinessException as err:
            current_app.logger.error(err.error)
            current_app.logger.error(err.status_code)
            if err.status_code == 400:
                response, status = {
                    "type": "Not found",
                    "message": f"No document with supplied formioId {submission_formio_id} found",
                }, HTTPStatus.BAD_REQUEST
            else:
                response, status = {
                    "type": "Bad request error",
                    "message": "Internal server error",
                }, HTTPStatus.BAD_REQUEST

            return response, status

@cors_preflight("GET,OPTIONS")
@API.route("/<string:submission_formio_id>/status", methods=["GET", "OPTIONS"])
class DocumentCheckStatus(Resource):

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response( 400, "BAD_REQUEST:- Invalid request.")
    @API.response( 404, "NOT_FOUND:- No document found to check status.")
    def get(submission_formio_id: str, **kwargs):
        try:
            ### 0. Get user data
            user: UserContext = kwargs["user"]
            person_identifier = user.token_info["personIdentifier"]
            current_app.logger.debug(f"Person Identifier - {person_identifier}")

            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)
            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            ### 1. Get document resource by submission_formio_id and pnobg
            formio_client = FormioServiceExtended()
            formio_file_resource_path = user.tenant_key + "-" + current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
            formio_token = formio_client.get_formio_access_token()
            generated_files_formio_form_id = formio_client.fetch_form_id_by_path(
                form_path=formio_file_resource_path,
                formio_token=formio_token
            )

            generated_file_resource = formio_client.debug_get_submission(
                data={
                    'form_id': generated_files_formio_form_id,
                    'sub_id': submission_formio_id
                },
                formio_token=formio_token
            )

            ### 2. Check document status in eurotrust
            generated_file_resource_data = generated_file_resource.get("data")
            generated_file_resource_transaction_id = generated_file_resource_data.get("evrotrustTransactionId")

            eurotrust_client = EurotrustIntegrationsService()
            response = eurotrust_client.check_document_status(transaction_id=generated_file_resource_transaction_id)
            eurotrust_status = response["status"]
            status = DocumentStatus.query.filter_by(eurotrust_status=eurotrust_status).first()

            ### 3. Update status
            if not status:
                raise BusinessException(
                    f"No status found {eurotrust_status} in our system", 
                    HTTPStatus.NOT_FOUND
                )

            document_service_client = DocumentsService()
            update_response = document_service_client.update_document_status_in_formio(
                formio_id=submission_formio_id,
                status=status
            )

            return (update_response, HTTPStatus.OK)
        except BusinessException as err:
            response, status = {
                "type": "Bad request error",
                "message": (err),
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.warning(response)
            current_app.logger.warning(err)

            return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/authenticate/<string:transaction_id>", methods=["GET", "OPTIONS"])
class AuthenticateResource(Resource):

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response( 400, "BAD_REQUEST:- Invalid request.")
    @API.response( 404, "NOT_FOUND:- No document found to check status.")
    def get(transaction_id: str,**kwargs):
        try:
            user: UserContext = kwargs["user"]
            user_keycloak_id = user.token_info["sub"]

            eurotrust_client = EurotrustIntegrationsService()
            response = eurotrust_client.check_document_status(transaction_id=transaction_id)
            current_app.logger.debug(response.get("status"))
            status = DocumentStatus.query.filter_by(eurotrust_status=response.get("status")).first()
            current_app.logger.debug(status)
            
            if not status:
                return {
                    "type": "Invalid Status",
                    "message": "Invalid status returned from EuroTrust",
                }, HTTPStatus.BAD_REQUEST
            
            if status.formio_status != "signed":
                return {
                    "type": "Not signed",
                    "message": "Status for transaction is not signed",
                }, HTTPStatus.BAD_REQUEST

            file_list = eurotrust_client.get_signed_file(transaction_id=transaction_id)
            
            signed_file_dict = file_list[0]
            signed_file_content = signed_file_dict["content"]
            base64_bytes = BytesIO(base64.b64decode(signed_file_content))

            
            reader = PdfReader(base64_bytes)
            meta = reader.metadata
            keywords_string = meta.get("/Keywords")
            
            base64_bytes.close()
            if keywords_string and user_keycloak_id:
                keycloak_client = KeycloakAdminAPIService()
                initial_client = keycloak_client.get_request(url_path=f"users/{user_keycloak_id}")

                xml_string = base64.b64decode(keywords_string)
                xml_tree = ElementTree.ElementTree(ElementTree.fromstring(xml_string))
                root = xml_tree.getroot()
                
                attributes = initial_client.get("attributes")
                attributes["isVerified"] = True

                for child in root:
                    if not child.tag.startswith("pic"):
                        attributes["evroTrust" + child.tag.capitalize()] = child.text

                response = keycloak_client.update_request(
                    url_path=f"users/{user_keycloak_id}", 
                    data={
                        "attributes": attributes
                    }
                )
                

            return (
                {
                    "status": status.formio_status
                }, 
                HTTPStatus.OK
            )
    
        except EurotrustException as err:
            current_app.logger.warning(err)
            return err.error, err.status_code
    
