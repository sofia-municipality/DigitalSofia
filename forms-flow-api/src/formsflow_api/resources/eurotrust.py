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
from formsflow_api.services.external import EurotrustIntegrationsService
from formsflow_api.services import FormioServiceExtended, DocumentsService
from formsflow_api.schemas import DocumentSignRequest, DocumentSignCallback
from formsflow_api.models import DocumentTransaction, DocumentStatus
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
        "fileName": fields.String()
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
            
            document_json = request.get_json()
            document_schema = DocumentSignRequest()

            data = document_schema.load(document_json)
            current_app.logger.debug(data)

            document_service_client = DocumentsService()

            document_timeout = current_app.config.get("CRON_DOCUMENT_TRANSACTION_TIMEOUT")
            valid_untill = (datetime.now() + timedelta(minutes=int(document_timeout)))
            response = document_service_client.send_document_to_sign_eurotrust(
                tenant_key=tenant_key,
                content=data["content"],
                content_type=data["content_type"],
                filename=data["file_name"],
                user_identifier=match[0],
                expire_at=valid_untill
            )

            eurotrust_response = response.get('response') 
            transactions = eurotrust_response.get('transactions')
            current_app.logger.debug(eurotrust_response)

            # Get pending status
            pending_status = document_service_client.get_document_status("Pending")

            # create Transactions in DB
            for transaction in transactions:
                document_transaction = document_service_client.create_document_transaction(
                    transaction_id=transaction['transactionID'],
                    thread_id=eurotrust_response["threadID"],
                    tenant_key=tenant_key,
                    status_id=pending_status.id,
                    application_id=data.get('application_id', None),
                    formio_id=data['formio_id'],
                    user_email=user.user_name
                )
                db.session.add(document_transaction)

            # Update status in formio
            formio_client = FormioServiceExtended()
            update_data = [
                formio_client.generate_rfc6902_object("/data/status",pending_status.formio_status),
                formio_client.generate_rfc6902_object("/data/evrotrustTransactionId", transactions[0]['transactionID']),
                formio_client.generate_rfc6902_object("/data/evrotrustThreadId", eurotrust_response["threadID"]),
                formio_client.generate_rfc6902_object("/data/validUntill", valid_untill.isoformat())
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
            status = document_service.get_document_transaction_status_in_eurotrust(transaction_id=transaction_id)

            if not status:
                raise BusinessException(
                    f"No status found {response['status']}", 
                    HTTPStatus.NOT_FOUND
                )

            # If the transaciton exists, update it 
            if transaction:
                transaction.update_status(status)
                db.session.commit()
                document_service.update_document_status_in_formio(
                    formio_id=transaction.formio_id,
                    tenant_key=tenant_key, 
                    status=status
                )

                if status.title == "Signed":
                    client = DocumentsService()
                    client.set_signed_file_from_eurotrust(transaction=transaction)

            return (
                {
                    "status": status.formio_status
                }, 
                HTTPStatus.OK
            )
        except BusinessException as err:
            current_app.logger.warning(err.error)            
            response, status = {
                "type": "Bad request error",
                "message": err.error,
            }, err.status_code

            return response, status


@cors_preflight("POST,OPTIONS")
@API.route("/document/ready", methods=["POST", "OPTIONS"])
class EurotrustCallbackResource(Resource):


    @staticmethod
    @API.response(200, "OK:- Successful request.")
    def post(**kwargs):
        try:
            callback_json = request.get_json()

            current_app.logger.debug("Showing callback request")
            current_app.logger.debug(request.json)

            document_callback_schema = DocumentSignCallback()
            data = document_callback_schema.load(callback_json)

            transaction = DocumentTransaction.query.filter(
                DocumentTransaction.transaction_id == data["transaction_id"]
            ).first()

            # Get transaction if not found throw an exception to be handled
            if not transaction:
                raise BusinessException(
                    f"No transaction found with specified transaction id {data['transaction_id']}", 
                    HTTPStatus.NOT_FOUND
                )
            

            # Get status throw exception if not found
            status = DocumentStatus.query.filter_by(eurotrust_status=data["status"]).first()
            if not status:
                raise BusinessException(
                    f"No status found {data['status']}", 
                    HTTPStatus.NOT_FOUND
                )

            # Update transaction status
            transaction.update_status(status)
            db.session.commit()

            # Update formio status
            document_service = DocumentsService()
            document_service.update_document_status_in_formio(
                    transaction.formio_id, 
                    tenant_key=transaction.tenant_key,
                    status=status
                )
            
            if status.title == "Signed":
                client = DocumentsService()
                client.set_signed_file_from_eurotrust(transaction=transaction)

            return ({"response": "Callback received"}, HTTPStatus.OK)
        except BusinessException as err:
            current_app.logger.warning(err)            
            response, status = {
                "type": "Bad request error",
                "message": err.error,
            }, err.status_code

            return response, status