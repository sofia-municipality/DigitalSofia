import os
from datetime import datetime
from flask import Blueprint, current_app
from formsflow_api.models import DocumentTransaction, DocumentStatus
from formsflow_api.models.db import db
from formsflow_api.services.external import EurotrustIntegrationsService
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services import DocumentsService
from formsflow_api_utils.exceptions import BusinessException


CronBlueprint = Blueprint('cron', __name__)

@CronBlueprint.cli.command('expire-documents')
def cron_expire_documents():
    current_time = datetime.datetime.utcnow()
    minutes = current_app.config.get("CRON_DOCUMENT_TRANSACTION_TIMEOUT")
    before = current_time - datetime.timedelta(minutes=int(minutes))

    current_app.logger.info(f"{current_time.isoformat()} - Cron Job - Expire documents")
    current_app.logger.info(f"All documents before {before.isoformat()}")
    transactions = DocumentTransaction.query.filter(
        DocumentTransaction.modified < before
    ).all()

    document_service = DocumentsService()
    formio_client = FormioServiceExtended()

    for transaction in transactions:
        print(f"{transaction.transaction_id} - {transaction.formio_id}")
        if transaction.status_id:
            # If pending, check status
            if transaction.status.title == 'Pending' or transaction.status.title == "On hold":
                current_app.logger.debug(f"Checking status for transaction {transaction.transaction_id}")
                status = document_service.get_document_transaction_status_in_eurotrust(transaction.transaction_id)
                if status and status.title != 'Pending':
                    # A valid status is returned
                    # Try and update status in formio
                    try:
                        document_service.update_document_status_in_formio(
                            transaction.formio_id,
                            tenant_key=transaction.tenant_key,
                            status=status
                        )
                        
                        if status.title == "Signed":
                            document_service.set_signed_file_from_eurotrust(transaction)
                        db.session.delete(transaction)
                    except BusinessException as err:
                        current_app.logger.error("An error occurred when updating")
                        current_app.logger.error(err)
                        continue
                elif status.title == 'Pending':
                    current_app.logger.debug(f"Document status is still pending")
                else:
                    # If a status is not found
                    current_app.logger.critical(
                        f"While checking status for transaction '{transaction.stransaction_id}' an invalid status was returned from eurotrust: {response}"
                    ) 
                    # Don't delete it and continue
                    continue
            else:
                db.session.delete(transaction)

            # Delete transaction
        
    db.session.commit()
                