import re
import base64
import json
import datetime
import time
from io import BytesIO
from xml.etree import ElementTree

import jwt
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
from formsflow_api_utils.utils.user_context import UserContext, user_context, _get_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api.exceptions import EurotrustException
from formsflow_api.services import FormioServiceExtended, DocumentsService, KeycloakAdminAPIService, ApplicationService
from formsflow_api.services.external import EurotrustIntegrationsService, FirebaseService
from formsflow_api.models import DocumentStatus, DocumentTransaction
from formsflow_api.models.db import db

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
        is_verified = user.token_info.get("isVerified")
        if is_verified == 'true':
            is_verified = True
        else:
            is_verified = False
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
            select=['_id', 'data', 'created', 'modified'],
            status=status,
            params={'data.userId': person_identifier, "data.signatureSource": "digitalSofia"},
            skip=skip,
            limit=limit + 1,  ### Get one more to check if we have more pages
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
            next_cursor = None

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
                    "referenceNumber": submission_data.get("referenceId", None),
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
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(400, "BAD_REQUEST:- Invalid request.")
    @API.response(404, "NOT_FOUND:- No document found to check status.")
    def get(submission_formio_id: str, **kwargs):
        try:
            if request.args.get('authToken') is not None:
                token = request.args.get('authToken')
            else:
                if request.headers.get('Authorization') is not None:
                    token = request.headers.get('Authorization')
                else:
                    raise BusinessException(
                        "Access to formsflow.ai API Denied. Check if the bearer token is passed for Authorization or has expired.",
                        HTTPStatus.UNAUTHORIZED
                    )

            user = _get_context(token)
            if user.token_info['exp'] < time.time():
                raise BusinessException(
                    "Access to formsflow.ai API Denied. Check if the bearer token is passed for Authorization or has expired.",
                    HTTPStatus.UNAUTHORIZED
                )

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
                if err.status_code == 401:
                    response, status = {
                                           "type": "Invalid Token Error",
                                           "message": "Access to formsflow.ai API Denied. Check if the bearer token is passed for Authorization or has expired.",
                                       }, HTTPStatus.UNAUTHORIZED
                else:
                    response, status = {
                                           "type": "Bad request error",
                                           "message": "Internal server error",
                                       }, HTTPStatus.BAD_REQUEST

            return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/<string:transaction_id>/status", methods=["GET", "OPTIONS"])
class DocumentCheckStatus(Resource):

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(400, "BAD_REQUEST:- Invalid request.")
    @API.response(404, "NOT_FOUND:- No document found to check status.")
    def get(transaction_id: str, **kwargs):
        try:
            user: UserContext = kwargs["user"]
            tenant_key = user.tenant_key

            # Get document entry if exists
            transaction = DocumentTransaction.query.filter(
                DocumentTransaction.transaction_id == transaction_id,
                DocumentTransaction.tenant_key == tenant_key
            ).first()

            if not transaction:
                raise BusinessException("Can't find transaction with specified transaction id", HTTPStatus.NOT_FOUND)

            document_service = DocumentsService()
            status = document_service.get_document_transaction_status_in_eurotrust(transaction_id=transaction_id)

            if not status:
                raise BusinessException(
                    f"No status found {response['status']}", 
                    HTTPStatus.NOT_FOUND
                )

            # If the transaciton exists, update it 
            if transaction:
                transaction.update_status_send_notification(new_status=status)
                document_service.update_document_status_in_formio(
                    formio_id=transaction.formio_id,
                    tenant_key=tenant_key, 
                    status=status
                )

                if status.title == "Signed":
                    client = DocumentsService()
                    client.set_signed_file_from_eurotrust(transaction=transaction)

                ### Check status
                is_status_pending = status.title == "Pending"
                current_app.logger.debug(f"Should we generate a message for camunda - {is_status_pending}")
                if not is_status_pending:
                    ApplicationService.update_message_for_application_by_status(
                        status=status,
                        transaction=transaction
                    )

            return (
                {
                    "status": status.formio_status
                }, 
                HTTPStatus.OK
            )
        except BusinessException as err:
            current_app.logger.warning(err.error)
            if err.error == "ERROR.INTEGRATIONS.EVROTRUST":
                return (
                    {
                        "status": "File is currently in signing, please try again later"
                    },
                    443
                )

            response, status = {
                "type": "Bad request error",
                "message": err.error,
            }, err.status_code

            return response, status


@cors_preflight("POST,OPTIONS")
@API.route("/authenticate/<string:tenant_key>/<string:transaction_id>", methods=["POST", "OPTIONS"])
class AuthenticateResource(Resource):

    @staticmethod
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(400, "BAD_REQUEST:- Invalid request.")
    @API.response(404, "NOT_FOUND:- No document found to check status.")
    def post(tenant_key: str, transaction_id: str, **kwargs):
        try:
            document_client = DocumentsService()
            identity_request = document_client.get_identity_request_by_transaction_id(transaction_id=transaction_id,
                                                                    tenant_key=tenant_key)
            current_app.logger.debug(identity_request)

            if identity_request is None:
                return {"Error": "No such transaction"}, HTTPStatus.BAD_REQUEST

            keycloak_client = KeycloakAdminAPIService()
            url_path = f"users?username=pnobg-{identity_request.person_identifier}&exact={True}"
            response = keycloak_client.get_request(url_path=url_path)
            current_app.logger.debug(f"Response - {response}")

            if not response:
                ### 11. No user found
                current_app.logger.debug("No user found")
                return {"Error": "No such person identifier"}, HTTPStatus.BAD_REQUEST

            current_app.logger.debug("1. Get user data")
            user = response[0]
            attributes = user.get("attributes")

            current_app.logger.debug(f"2. Check if person identifier is valid {attributes['personIdentifier'][0]}")
            match = re.findall(r"PNOBG-(\d{10})$", attributes['personIdentifier'][0])
            if not match:
                current_app.logger.debug(BusinessException)
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            #document_client = DocumentsService()
            #identity_request = document_client.get_identity_request(
            #    person_identifier=person_identifier,
            #    tenant_key=tenant_key
            #) !!!!!!!!!!!!

            person_identifier = match[0]
            current_app.logger.debug(f"3. Person identifier is {person_identifier}")

            current_app.logger.debug(f"4. Check document status for transaction {transaction_id}")
            eurotrust_client = EurotrustIntegrationsService()
            response = eurotrust_client.check_document_status(transaction_id=transaction_id)

            eurotrust_status = response.get("status")
            current_app.logger.debug(f"5. Eurotrust status {eurotrust_status}")

            current_app.logger.debug(f"6. Query document status within our DB")
            status = DocumentStatus.query.filter_by(eurotrust_status=eurotrust_status).first()
            current_app.logger.debug(f"{status}")

            if not status:
                current_app.logger.debug("7. No status valid status found within our system")
                return {
                           "type": "Invalid Status",
                           "message": "Invalid status returned from EuroTrust",
                       }, HTTPStatus.BAD_REQUEST

            current_app.logger.debug(f"7. Get Identity Request")

            current_app.logger.debug(f"8. Check status")
            current_app.logger.debug(f"Status = {status.formio_status}")
            if status.formio_status in ["rejected", "expired", "withdrawn", "failed"]:
                current_app.logger.debug(f"9. Deleting identity request")
                document_client.delete_identity_request(identity_request=identity_request)
                return (
                    {
                        "status": status.formio_status
                    },
                    200
                )

            current_app.logger.debug(f"8. Check is status signed")
            current_app.logger.debug(f"Status = {status.formio_status}")
            if status.formio_status != "signed":
                current_app.logger.debug(f"9. File is not signed return a bad request")
                return {
                           "type": "Not signed",
                           "message": "Status for transaction is not signed",
                       }, HTTPStatus.BAD_REQUEST

            ### TODO: Update user attributes with new keycloak changes
            current_app.logger.debug(f"9. Get signed file from eurotrust")

            ### Identity Request timeout
            timeout_time = int(current_app.config.get("EVROTRUST_IDENTITY_AUTHENTICATE_TIMEOUT"))
            timeout_max_retries = int(current_app.config.get("EVROTRUST_IDENTITY_REQUESTS_MAX_RETRIES"))
            file_list = None

            for i in range(timeout_max_retries):
                try:
                    file_list = eurotrust_client.get_signed_file(transaction_id=transaction_id)

                    if file_list:
                        break
                    else:
                        return {
                            "type": "Bad request error",
                            "message": "Internal server error",
                        }, HTTPStatus.BAD_REQUEST
                except EurotrustException as err:
                    current_app.logger.debug("EurotrustException found")
                    current_app.logger.debug(f"Error error - {err.error}")
                    current_app.logger.debug(f"Error code - {err.status_code}")
                    current_app.logger.debug(f"Error data - {err.data}")
                    if err.data == "443 unknown status: [Document not found]" or err.data == "Error in decrypting content":
                        current_app.logger.debug(f"Retry number {i}")
                        time.sleep(timeout_time)
                        continue
                    return (
                        {
                            "status": err.error
                        },
                        err.status_code
                    )

            if not file_list:
                return (
                    {
                        "status": "File is currently in signing, please try again later"
                    },
                    443
                )

            current_app.logger.debug(f"10. Get file content")
            signed_file_dict = file_list[0]
            signed_file_content = signed_file_dict["content"]

            #### Signed_file_content

            base64_bytes = BytesIO(base64.b64decode(signed_file_content))

            current_app.logger.debug(f"11. Read Keywords")
            reader = PdfReader(base64_bytes)
            meta = reader.metadata
            keywords_string = meta.get("/Keywords")

            current_app.logger.debug(f"12. Init Keycloak API service")
            base64_bytes.close()
            keycloak_client = KeycloakAdminAPIService()

            user_keycloak_id = user.get('id')
            current_app.logger.debug(f"13. Check if we have a valid keywords string and a valid user_keycloak_id")
            current_app.logger.debug(f"keywords_string - {bool(keywords_string)}")
            current_app.logger.debug(f"user_keycloak_id - {bool(user_keycloak_id)}")
            if keywords_string and user_keycloak_id:
                initial_client = keycloak_client.get_request(url_path=f"users/{user_keycloak_id}")

                xml_string = base64.b64decode(keywords_string)
                xml_tree = ElementTree.ElementTree(ElementTree.fromstring(xml_string))
                root = xml_tree.getroot()

                payload = {}
                attributes = initial_client.get("attributes")

                current_app.logger.debug("14. Set user isVerified attribute to True")
                attributes["isVerified"] = True

                current_app.logger.debug("15. Set other attributes")
                for child in root:
                    if not child.tag.startswith("pic"):
                        current_app.logger.debug(child.tag.capitalize())
                        attributes["evroTrust" + child.tag.capitalize()] = child.text
                        if "Firstnamelatin" == child.tag.capitalize():
                            payload['firstName'] = child.text
                        if "Lastnamelatin" == child.tag.capitalize():
                            payload['lastName'] = child.text

                current_app.logger.debug("16. Update keycloak user model")
                request_data = request.json

                if "pin" in request_data:
                    attributes["pin"] = request_data.get('pin')
                if "email" in request_data:
                    payload["email"] = request_data.get('email')
                else:
                    payload["email"] = user.get('email')
                if "phoneNumber" in request_data:
                    attributes["phoneNumber"] = request_data.get('phoneNumber')
                if "fcm" in request_data:
                    attributes["fcm"] = request_data.get('fcm')
                payload['attributes'] = attributes
                current_app.logger.debug(payload)
                response = keycloak_client.update_request(
                    url_path=f"users/{user_keycloak_id}",
                    data=payload
                )


            formio_service = FormioServiceExtended()
            formio_token = formio_service.get_formio_access_token()
            file_form_id = formio_service.fetch_form_id_by_path(tenant_key + '-generated-files', formio_token)

            #current_app.logger.debug(signed_file_dict.keys())

            signing_date = datetime.datetime.now().isoformat()

            data = {
                "formId": file_form_id,
                "data": {
                    "applicationId": None,
                    "file": [
                        {
                            "name": signed_file_dict["fileName"],
                            "originalName": signed_file_dict["fileName"],
                            "size": len(str(signed_file_content)),
                            "storage": "base64",
                            "type": "application/pdf",
                            "url": "data:application/pdf;base64," + signed_file_content
                        }
                    ],
                    "evrotrustTransactionId": transaction_id,
                    "signatureSource": "digitalSofia",
                    "status": 'signed',
                    "userId": "PNOBG-" + person_identifier,
                    "signed": signing_date,
                    "referenceId": None,
                    "formPath": tenant_key + "-identity-requests"
                }
            }

            formio_service.post_submission(data=data, formio_token=formio_token)


            ## 6. Remove identity request
            current_app.logger.debug("Delete identity request")
            document_client.delete_identity_request(identity_request=identity_request)

            # ### 7. Generate a refresh token
            # current_app.logger.debug("Generate new refresh token")
            # client_id = user.token_info.get("azp", None)
            # refresh_token = request.args.get("refreshToken", None)
            # if client_id and refresh_token:
            #     response = keycloak_client.refresh_token(client_id=client_id, refresh_token=refresh_token)
            #     return (
            #         response,
            #         HTTPStatus.OK
            #     )
            # else:
            #     return (
            #         {
            #             "status": status.formio_status
            #         },
            #         HTTPStatus.OK
            #     )

        except EurotrustException as err:
            current_app.logger.debug("EurotrustException found")
            current_app.logger.debug(f"Error error - {err.error}")
            current_app.logger.debug(f"Error code - {err.status_code}")
            current_app.logger.debug(f"Error data - {err.data}")
            if err.data == "443 unknown status: [Document not found]" or err.data == "Error in decrypting content":
                return (
                    {
                        "status": "File is currently in signing, please try again later"
                    },
                    443
                )
            return (
                {
                    "status": err.error
                },
                err.status_code
            )


@cors_preflight("GET,OPTIONS")
@API.route("/request-identity/<string:tenant_key>/<string:person_identifier>", methods=["GET", "OPTIONS"])
class DocumentVerifyIdentity(Resource):
    """Resource for verifying documents."""

    @classmethod
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
    def get(cls, tenant_key: str, person_identifier: str, **kwargs):
        ### 1. Get user data
        keycloak_client = KeycloakAdminAPIService()
        url_path = f"users?username=pnobg-{person_identifier}&exact={True}"
        response = keycloak_client.get_request(url_path=url_path)
        current_app.logger.debug(f"Response - {response}")

        if not response:
            ### 11. No user found
            current_app.logger.debug("No user found")
            return ("No such person identifier", HTTPStatus.OK)

        current_app.logger.debug("1. Get user data")
        user = response[0]
        attributes = user.get("attributes")

        is_verified = attributes.get("isVerified")
        if is_verified == 'true':
            is_verified = True
        else:
            is_verified = False

        current_app.logger.debug(f"Is verified - {is_verified}")
        person_identifier = attributes['personIdentifier'][0]

        current_app.logger.debug("2. Check person identifier validity")
        match = re.findall(r"PNOBG-(\d{10})$", person_identifier)
        if not match:
            current_app.logger.debug(BusinessException)
            raise BusinessException(
                "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
            )

        current_app.logger.debug("4. Try and get an identity_request if exists")
        person_identifier = match[0]
        document_client = DocumentsService()
        identity_request = document_client.get_identity_request(
            person_identifier=person_identifier,
            tenant_key=tenant_key
        )

        if identity_request:
            eurotrust_client = EurotrustIntegrationsService()
            eurotrust_client.withdraw_document(identity_request.thread_id)
            document_client.delete_identity_request(identity_request=identity_request)

        identity_request = document_client.create_identity_request(
            person_identifier=person_identifier,
            tenant_key=tenant_key
        )

        identity_request_data = {
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

        return (identity_request_data, HTTPStatus.OK)