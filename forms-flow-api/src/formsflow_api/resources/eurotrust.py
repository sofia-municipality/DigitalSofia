import re
from datetime import datetime, timedelta
from http import HTTPStatus
from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from marshmallow.exceptions import ValidationError
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    cache,
    profiletime
)
from formsflow_api.services.external import EurotrustIntegrationsService, FirebaseService, KeycloakAdminAPIService
from formsflow_api.services import FormioServiceExtended, DocumentsService, ApplicationService
from formsflow_api.schemas import DocumentSignRequest, DocumentSignCallback
from formsflow_api.models import DocumentTransaction, DocumentStatus, Application
from formsflow_api.models.db import db
from formsflow_api.exceptions import EurotrustException


API = Namespace("Eurotrust", description="Integration with eurotrust")

sign_request = API.model(
    "SignRequest",
    {
        "applicationId": fields.String(),
        "formioId": fields.String(),
        "content": fields.String(),
        "contentType": fields.String(),
        "fileName": fields.String(),
        "originFormFormioId": fields.String()
    },
)

transaction = API.model(
    "EurotrustTransaction",
    {
        "transactionID": fields.String(),
        "identificationNumber": fields.String()
    },
)

transaction_dict = API.model(
    "TransactionDict",
    {
        "threadID": fields.String(),
        "transactions": fields.List(
            fields.Nested(transaction, description="List of transactions.")
        )
    }    
)


sign_response = API.model(
    "SignResponse",
    {
        "response": fields.Nested(transaction_dict),
        "groupSigning": fields.Boolean(),
    },
)


@cors_preflight("POST,OPTIONS")
@API.route("/sign", methods=["POST", "OPTIONS"])
class EurotrustSignResource(Resource):

    @staticmethod
    @auth.require
    @user_context
    @API.doc(body=sign_request)
    @API.response(200, "OK:- Successful request.", model=sign_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def post(**kwargs):
        try:
            user: UserContext = kwargs["user"]
            tenant_key = user.tenant_key
            person_identifier = user.token_info["personIdentifier"]
            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )
            
            person_identifier = match[0]
            document_json = request.get_json()
            document_schema = DocumentSignRequest()

            data = document_schema.load(document_json)
            current_app.logger.debug("-------------- Document json sing --------------")
            current_app.logger.debug(document_json.get("originFormFormioId"))
            current_app.logger.debug(data.get("origin_form_formio_id"))

            document_service_client = DocumentsService()

            document_timeout = current_app.config.get("EUROTRUST_EXPIRE_TIMEOUT")
            valid_untill = (datetime.now() + timedelta(minutes=int(document_timeout)))
            response = document_service_client.send_document_to_sign_eurotrust(
                tenant_key=tenant_key,
                content=data["content"],
                content_type=data["content_type"],
                filename=data["file_name"],
                user_identifier=person_identifier,
                expire_at=valid_untill
            )

            eurotrust_response = response.get('response') 
            transactions = eurotrust_response.get('transactions')
            current_app.logger.debug(transactions)

            # Get pending status
            pending_status = document_service_client.get_document_status("Pending")

            
            keycloak_client = KeycloakAdminAPIService()
            url_path = f"users?username=pnobg-{person_identifier}&exact={True}"
            keycloak_users = keycloak_client.get_request(url_path)

            firebase_user_registration_token = None
            if keycloak_users:
                keycloak_user = keycloak_users[0]    

                keycloak_user_attributes = keycloak_user.get("attributes")
                firebase_user_registration_attribute = keycloak_user_attributes.get("fcm", None)
                if firebase_user_registration_attribute:
                    firebase_user_registration_token = firebase_user_registration_attribute[0]
                    current_app.logger.debug(f"User has fcm - {firebase_user_registration_token}")    
                
                firebase_client = FirebaseService()

            signature_source = data.get("signature_source", None)
            # create Transactions in DB
            for transaction in transactions:
                current_app.logger.debug(f"Creating transaction with {transaction['transactionID']} - {data.get('origin_form_formio_id', None)}")
                document_transaction = document_service_client.create_document_transaction(
                    transaction_id=transaction['transactionID'],
                    thread_id=eurotrust_response["threadID"],
                    tenant_key=tenant_key,
                    status_id=pending_status.id,
                    application_id=data.get('application_id', None),
                    formio_id=data['formio_id'],
                    user_email=user.user_name,
                    origin_form_formio_id=data.get("origin_form_formio_id", None),
                    signature_source=signature_source
                )
                

                current_app.logger.debug(f"Signature Source - |{signature_source}|")
                current_app.logger.debug(f"FCM TOken - |{firebase_user_registration_token}|")
                if firebase_user_registration_token and signature_source in ["digitalSofia"]:
                    firebase_client.send_status_change_message(
                        transaction=document_transaction, 
                        firebase_user_registration_token=firebase_user_registration_token
                    )

                db.session.add(document_transaction)

            # Update status in formio
            formio_client = FormioServiceExtended()
            update_data = [
                formio_client.generate_rfc6902_object("/data/status",pending_status.formio_status),
                formio_client.generate_rfc6902_object("/data/evrotrustTransactionId", transactions[0]['transactionID']),
                formio_client.generate_rfc6902_object("/data/evrotrustThreadId", eurotrust_response["threadID"]),
                formio_client.generate_rfc6902_object("/data/validUntill", valid_untill.isoformat()),
                formio_client.generate_rfc6902_object("/data/signatureSource", data.get("signature_source"))
            ]
            document_service_client.update_document_in_formio(
                tenant_key=tenant_key, 
                resource_id=data['formio_id'], 
                data=update_data 
            )

            db.session.commit()
            return (response, HTTPStatus.OK)
        except ValidationError as err:
            current_app.logger.warning(err)
            response, status = {
                                   "type": "Bad request error",
                                   "message": err.messages
                               }, HTTPStatus.BAD_REQUEST
            return response, status
        except EurotrustException as err:
            current_app.logger.warning(err)
            response, status = err.error, err.status_code
            return response, status
        except BusinessException as err:
            current_app.logger.warning(err)
            response, status = {
                "type": "Bad request error",
                "message": (err),
            }, err.status_code
            return response, status


@cors_preflight("GET,OPTIONS")
@API.route("/document/<string:transaction_id>/status/", methods=["GET", "OPTIONS"])
class EurotrustDocumentStatusResource(Resource):

    
    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK: - Successful request.")
    def get(transaction_id:str, **kwargs):
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
            new_status = document_service.get_document_transaction_status_in_eurotrust(transaction_id=transaction_id)

            if not new_status:
                raise BusinessException(
                    f"No status found {response['status']}", 
                    HTTPStatus.NOT_FOUND
                )
            
            
            if new_status.title == "Signed":
                client = DocumentsService()
                response = client.set_signed_file_from_eurotrust(transaction=transaction)

            # If the transaciton exists, update it 
            if transaction:
                transaction.update_status_send_notification(new_status=new_status)

                ### Updating status for application in camunda
                is_status_pending = new_status.title == "Pending"
                current_app.logger.debug(f"Should we generate a message for camunda - {is_status_pending}")
                if not is_status_pending:
                    ApplicationService.update_message_for_application_by_status(
                        status=new_status,
                        transaction=transaction
                    )

            return (
                {
                    "status": new_status.formio_status
                }, 
                HTTPStatus.OK
            )
        except EurotrustException as err:
            current_app.logger.debug("EurotrustException found")
            current_app.logger.debug(f"Error error - {err.error}")
            current_app.logger.debug(f"Error code - {err.status_code}")
            current_app.logger.debug(f"Error data - {err.data}")

            if err.data == "443 unknown status: [Document not found]":
                return (
                    {
                        "status": "signing"
                    },
                    200
                )

            return {
                "type": "Bad request error",
                "message": err.error,
            }, err.status_code
        except BusinessException as err:
            current_app.logger.warning(err.error)            
            response, status = {
                "type": "Bad request error",
                "message": err.error,
            }, err.status_code

            return response, new_status


@cors_preflight("POST,OPTIONS")
@API.route("/document/ready", methods=["POST", "OPTIONS"])
class EurotrustCallbackResource(Resource):


    @staticmethod
    @API.response(200, "OK:- Successful request.")
    def post(**kwargs):
        try:
            ### 1. Receive callback
            callback_json = request.get_json()

            current_app.logger.debug("Showing callback request")
            current_app.logger.debug(request.json)

            ### 2. Verify callback data
            document_callback_schema = DocumentSignCallback()
            data = document_callback_schema.load(callback_json)

            ### 3. Get transaction
            transaction = DocumentTransaction.query.filter(
                DocumentTransaction.transaction_id == data["transaction_id"]
            ).first()

            # Get transaction if not found throw an exception to be handled
            if not transaction:
                raise BusinessException(
                    f"No transaction found with specified transaction id {data['transaction_id']}", 
                    HTTPStatus.NOT_FOUND
                )
            

            ### 4. Is it a valid status
            ### Get status throw exception if not found
            new_status = DocumentStatus.query.filter_by(eurotrust_status=data["status"]).first()
            if not new_status:
                raise BusinessException(
                    f"No status found {data['status']}", 
                    HTTPStatus.NOT_FOUND
                )

            ### 5. Update formio status
            document_service = DocumentsService()
            document_service.update_document_status_in_formio(
                    transaction.formio_id, 
                    tenant_key=transaction.tenant_key,
                    status=new_status
                )
            
            ### 6. The new status is signed, update 
            if new_status.title == "Signed":
                client = DocumentsService()
                client.set_signed_file_from_eurotrust(transaction=transaction)
            
            ### 7. Update transaction 
            transaction.update_status_send_notification(new_status=new_status)
            
            ### 8. Updating status for application in camunda
            is_status_pending = new_status.title == "Pending"
            current_app.logger.debug(f"Should we generate a message for camunda - {is_status_pending}")
            if not is_status_pending:
                ApplicationService.update_message_for_application_by_status(
                    status=new_status,
                    transaction=transaction
                )

            return ({"response": "Callback received"}, HTTPStatus.OK)
        except BusinessException as err:
            current_app.logger.warning(err)            
            response, status = {
                "type": "Bad request error",
                "message": err.error,
            }, err.status_code

            return response, status