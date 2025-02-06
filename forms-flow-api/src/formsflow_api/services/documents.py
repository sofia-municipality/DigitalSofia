from datetime import datetime, timedelta
from typing import Optional
from flask import current_app

from formsflow_api.services.external import EurotrustIntegrationsService
from formsflow_api.models import DocumentTransaction, DocumentStatus, IdentityRequest
from formsflow_api.models.db import db
from formsflow_api.services import FormioServiceExtended
from formsflow_api.utils.enums import DocumentStatusesEnum


class DocumentsService:
    def get_document_status(self, status_title) -> Optional[DocumentStatus]:
        return DocumentStatus.query.filter_by(title=status_title).first()

    def create_document_transaction(
        self,
        transaction_id,
        thread_id,
        tenant_key,
        status_id,
        formio_id,
        user_email,
        application_id=None,
        origin_form_formio_id=None,
        signature_source=None,
    ) -> DocumentTransaction:
        transaction = DocumentTransaction(
            transaction_id=transaction_id,
            thread_id=thread_id,
            tenant_key=tenant_key,
            status_id=status_id,
            application_id=application_id,
            formio_id=formio_id,
            user_email=user_email,
            origin_form_formio_id=origin_form_formio_id,
            signature_source=signature_source,
        )

        return transaction

    def create_and_save_document_transaction(
        self,
        transaction_id,
        thread_id,
        tenant_key,
        status_id,
        formio_id,
        user_email,
        application_id=None,
        origin_form_formio_id=None,
        signature_source=None,
    ) -> DocumentTransaction:
        transaction = DocumentTransaction(
            transaction_id=transaction_id,
            thread_id=thread_id,
            tenant_key=tenant_key,
            status_id=status_id,
            application_id=application_id,
            formio_id=formio_id,
            user_email=user_email,
            origin_form_formio_id=origin_form_formio_id,
            signature_source=signature_source,
        )

        db.session.add(transaction)
        db.session.commit()

        return transaction

    def get_document_by_submission_id(self, formio_id: str):
        return DocumentTransaction.query.filter_by(formio_id=formio_id).first()

    def update_document_status_in_formio(
        self,
        formio_id: int,
        tenant_key: str,
        status: DocumentStatus,
    ):
        formio_client = FormioServiceExtended()
        data = [
            formio_client.generate_rfc6902_object("/data/status", status.formio_status)
        ]

        if status.title == DocumentStatusesEnum.EXPIRED:
            data.append(
                formio_client.generate_rfc6902_object(
                    "/data/expired", datetime.now().isoformat()
                )
            )
        elif status.title == DocumentStatusesEnum.REJECTED:
            data.append(
                formio_client.generate_rfc6902_object(
                    "/data/rejected", datetime.now().isoformat()
                )
            )
        elif status.title == DocumentStatusesEnum.SIGNED:
            data.append(
                formio_client.generate_rfc6902_object(
                    "/data/signed", datetime.now().isoformat()
                )
            )
        elif status.title == DocumentStatusesEnum.GENERATED:
            data.append(
                formio_client.generate_rfc6902_object(
                    "/data/generated", datetime.now().isoformat()
                )
            )

        return self.update_document_in_formio(
            tenant_key=tenant_key, resource_id=formio_id, data=data
        )

    def update_document_in_formio(self, tenant_key, resource_id, data):
        ### 1. Init client and token
        formio_client = FormioServiceExtended()
        formio_token = formio_client.get_formio_access_token()

        ### 2. Form_id for path
        form_path = current_app.config.get("FORMIO_FILE_RESOURCE_PATH")
        form_id = formio_client.fetch_form_id_by_path(
            form_path=f"{tenant_key}-{form_path}", formio_token=formio_token
        )

        ### 3. Update resource
        response = formio_client.update_formio_resource(
            form_id=form_id, resource_id=resource_id, data=data
        )

        return response

    #################################
    ####### Eurotrust methods #######
    #################################
    def send_document_to_sign_eurotrust(
        self,
        tenant_key: str,
        content: str,
        content_type: str,
        filename: str,
        user_identifier: str,
        expire_at: datetime,
    ):
        eurotrust_client = EurotrustIntegrationsService()

        response = eurotrust_client.sign_document(
            tenant_key=tenant_key,
            dateExpire=expire_at.isoformat(),
            content=content,
            content_type=content_type,
            file_name=filename,
            user_identifier=user_identifier,
        )

        return response

    def get_document_transaction_status_in_eurotrust(
        self, transaction_id
    ) -> DocumentStatus:
        current_app.logger.info(
            f"Checking status for transaction_id in Eurotrust: {transaction_id}"
        )

        eurotrust_client = EurotrustIntegrationsService()
        response = eurotrust_client.check_document_status(transaction_id=transaction_id)

        # IMPORTANT: Eurotrust status codes
        # {'status': 2, 'isProcessing': True }
        # When document in eurotrust is signed but is still in processing we need to return
        # pending status to check again
        if response["status"] == 2 and response["isProcessing"]:
            return self.get_document_status("Pending")

        status = DocumentStatus.query.filter_by(
            eurotrust_status=response["status"]
        ).first()

        return status

    def set_signed_file_from_eurotrust(self, transaction: DocumentTransaction) -> str:

        # Get signed file from integration service
        eurotrust_client = EurotrustIntegrationsService()
        file_list = eurotrust_client.get_signed_file(
            transaction_id=transaction.transaction_id
        )
        # TODO: TO make work with multiple files
        # Example response
        # [
        #   {
        #     "fileName": "Filename",
        #     "contentType": application/pdf
        #     "content": <base64>
        #   }
        # ]

        signed_file_dict = file_list[0]

        formio_client = FormioServiceExtended()

        form_path = current_app.config.get("FORMIO_FILE_RESOURCE_PATH")
        if transaction.tenant_key:
            form_path = f"{transaction.tenant_key}-{form_path}"

        response = formio_client.update_resource_formio_file(
            form_path=form_path,
            formio_resource_id=transaction.formio_id,
            type=signed_file_dict.get("contentType"),
            name=signed_file_dict.get("fileName"),
            content=signed_file_dict.get("content"),
        )

        # current_app.logger.debug(response)

        return response

    def create_identity_request(self, person_identifier: str, tenant_key: str, language: str):
        ### HERE
        client = EurotrustIntegrationsService()
        current_time = datetime.utcnow()
        eurotrust_identity_timeout = current_app.config.get(
            "EUROTRUST_IDENTITY_TIMEOUT"
        )
        valid_untill = current_time + timedelta(minutes=int(eurotrust_identity_timeout))
        date_expire_isoformat = valid_untill.isoformat()
        response = client.send_identification_request(
            user_identifier=person_identifier, date_expire=date_expire_isoformat, language=language
        )
        # "evrotrustThreadId": response.get("threadID"),
        # "evrotrustTransactionId": response.get("transactionID"),

        identity_request = IdentityRequest(
            transaction_id=response.get("transactionID"),
            thread_id=response.get("threadID"),
            tenant_key=tenant_key,
            person_identifier=person_identifier,
            valid_untill=valid_untill,
        )
        db.session.add(identity_request)
        db.session.commit()
        return identity_request

    def get_identity_request(self, person_identifier: str, tenant_key: str):
        return IdentityRequest.query.filter_by(
            person_identifier=person_identifier, tenant_key=tenant_key
        ).first()

    def get_identity_request_by_transaction_id(
        self, transaction_id: str, tenant_key: str
    ):
        return IdentityRequest.query.filter_by(
            transaction_id=transaction_id, tenant_key=tenant_key
        ).first()

    def delete_identity_request(self, identity_request: IdentityRequest):
        ### HERE
        if identity_request:
            db.session.delete(identity_request)
            db.session.commit()
            return True

        return False

    @staticmethod
    def get_application_sign_transaction(
        application_id: str,
    ) -> Optional[DocumentTransaction]:
        doc_transaction = DocumentTransaction.query.filter_by(
            application_id=application_id, status_id=3
        ).first()
        return doc_transaction

    @staticmethod
    def deliver_document_to_eurotrust(document_base64: str, document_name: str, user_id: str, description: str) -> tuple[str, str]:
        eurotrust_client = EurotrustIntegrationsService()
        now = datetime.utcnow()
        expire = now + timedelta(hours=4)
        iso_datetime_expire = expire.isoformat()
        resp = eurotrust_client.send_file_for_delivery(
            document_base64,
            document_name,
            user_id,
            description,
            iso_datetime_expire,
        )
        thread_id = resp["threadID"]
        transaction_id = resp["transactions"][0]["transactionID"]
        return thread_id, transaction_id

    def get_document_status_transaction_in_eurotrust(
        self, thread_id: str
    ) -> DocumentStatus:
        current_app.logger.info(
            f"Checking receipt status for thread in Eurotrust: {thread_id}"
        )

        eurotrust_client = EurotrustIntegrationsService()
        eurotrust_resp = eurotrust_client.check_receipt_status(thread_id)

        evidence = eurotrust_resp["threads"][0]["statuses"][0]["evidences"][0]

        contend_handover = next(filter(lambda r: r.get("type") == 5, evidence), None)
        contend_handover_failure = next(filter(lambda r: r.get("type") == 6, evidence), None)

        if contend_handover:
            if contend_handover.get("status") == 3:
                return self.get_document_status(DocumentStatusesEnum.GENERATED.value)

        if contend_handover_failure:
            if contend_handover_failure.get("status") == 3:
                return self.get_document_status(DocumentStatusesEnum.EXPIRED.value)

        return self.get_document_status(DocumentStatusesEnum.DELIVERING.value)
