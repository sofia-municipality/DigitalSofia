import zipfile
import io
import base64

from flask import current_app

from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services.external import EurotrustIntegrationsService
from formsflow_api.utils.enums import FormIOPathEnum, ReceiptTypeEnum
from formsflow_api.models.document_transaction import DocumentTransaction


class ReceiptService:
    _form_path = FormIOPathEnum.RECEIPTS.value

    def __init__(self) -> None:
        self._formio_service = FormioServiceExtended()

    def _get_formio_token(self):
        return self._formio_service.get_formio_access_token()

    def _get_form_id(self, formio_token, form_path_with_tenant):
        receipts_form_id = self._formio_service.fetch_form_id_by_path(
            form_path_with_tenant, formio_token
        )

        current_app.logger.debug(receipts_form_id)

        if isinstance(receipts_form_id, tuple):
            raise Exception("Form id not found!")

        return receipts_form_id

    def add_receipt_to_form_io(
        self,
        application_id,
        receipt_type,
        document_name,
        document_base64,
        formio_token,
        form_id,
    ):
        submission_data = {
            "formId": form_id,
            "data": {
                "applicationId": application_id,
                "receiptType": receipt_type,
                "file": [
                    {
                        "name": document_name,
                        "originalName": document_name,
                        "size": len(str(document_base64)),
                        "storage": "base64",
                        "type": "application/pdf",
                        "url": "data:application/pdf;base64," + document_base64,
                    }
                ],
            },
        }

        self._formio_service.post_submission(
            data=submission_data, formio_token=formio_token
        )

    def _determine_receipt_type(self, file_name: str) -> str:
        filename_lower = file_name.lower()

        for receipt_type in ReceiptTypeEnum:
            if receipt_type.value.lower() in filename_lower:  # Case-insensitive check
                return receipt_type.value

        current_app.logger.critical("Receipt type not found!")
        return "No type"

    def download_and_save_receipts(self, transaction_id, tenant_key, application_id):
        eurotrust_service = EurotrustIntegrationsService()
        resp_content = eurotrust_service.download_receipts(transaction_id)

        with zipfile.ZipFile(io.BytesIO(resp_content)) as zip_ref:
            current_app.logger.debug("Files in the ZIP archive:")
            current_app.logger.debug(zip_ref.namelist())

            formio_token = self._get_formio_token()
            form_path_with_tenant = f"{tenant_key}-{self._form_path}"
            form_id = self._get_form_id(formio_token, form_path_with_tenant)

            for file_name in zip_ref.namelist():
                with zip_ref.open(file_name) as file:
                    file_content = file.read()
                    file_base64 = base64.b64encode(file_content).decode(
                        "utf-8"
                    )  # Encoding to base64

                    receipt_type = self._determine_receipt_type(file_name)

                    self.add_receipt_to_form_io(
                        application_id,
                        receipt_type,
                        file_name,
                        file_base64,
                        formio_token,
                        form_id,
                    )

    def get_application_receipts(self, application_id: str, tenant_key: str):
        form_path_with_tenant = f"{tenant_key}-{self._form_path}"
        formio_token = self._get_formio_token()

        receipts = self._formio_service.get_submissions(
            form_path_with_tenant,
            formio_token,
            params={'data.applicationId': application_id},
        )[0]

        return receipts
    
    def get_receipts_evidence_in_eurotrust(self, thread_id: str) -> dict:
        eurotrust_service = EurotrustIntegrationsService()
        eurotrust_resp = eurotrust_service.check_receipt_status(thread_id)

        evidence = eurotrust_resp["threads"][0]["statuses"][0]["evidences"][0]
        return evidence
    
    def wait_for_notification(self, document_transaction: DocumentTransaction) -> None:
        import time

        thread_id = document_transaction.thread_id

        for _ in range(720):
            try:
                current_app.logger.debug("Get receipts evidence from Eurotrust:")
                evidence = self.get_receipts_evidence_in_eurotrust(thread_id)
                current_app.logger.debug("Evidence from Eurotrust:")
                current_app.logger.debug(evidence)

                submission_acceptance = next(filter(lambda r: r.get("type") == 1, evidence), None)
                consignment_acceptance = next(filter(lambda r: r.get("type") == 3, evidence), None)

                if submission_acceptance and consignment_acceptance:
                    if submission_acceptance.get("status") == 3 and consignment_acceptance.get("status") == 3:
                        current_app.logger.debug("Condition for sending notification is reached!")
                        document_transaction.notify_user_for_document_delivering()
                        return
            except Exception as e:
                current_app.logger.warning("Handled exception in wait for notification:")
                current_app.logger.warning(e)

            time.sleep(5)

        current_app.logger.warning("Error during sending notification!")

    def wait_for_notification_in_separate_process(self, document_transaction: DocumentTransaction) -> None:
        from multiprocessing import Process
        process = Process(target=self.wait_for_notification, args=(document_transaction,))
        process.start()
