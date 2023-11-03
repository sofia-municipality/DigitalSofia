from .audit_mixin import AuditDateTimeMixin
from .base_model import BaseModel
from .db import db
from formsflow_api_utils.exceptions import BusinessException
# from formsflow_api.services import FormioService ##, FormioServiceExtended
from http import HTTPStatus
from flask import current_app
from .document_status import DocumentStatus


class DocumentTransaction(AuditDateTimeMixin, BaseModel, db.Model):
    id = db.Column(db.Integer, primary_key=True)

    transaction_id = db.Column(db.String(12), nullable=False)
    thread_id = db.Column(db.String(12), nullable=False)
    tenant_key = db.Column(db.String, nullable=True)
    status_id = db.Column(db.Integer, db.ForeignKey("document_status.id"), nullable=True)
    application_id = db.Column(db.String(12), nullable=True)
    formio_id = db.Column(db.String(24), nullable=False)
    user_email = db.Column(db.String(320), nullable=False)

    status = db.relationship(
        "DocumentStatus", back_populates="documents"
    )

    def update_status(self, status: DocumentStatus):
        self.status_id = status.id
        
    # def get_formio_file_form_path(self):
    #     client = FormioServiceExtended()
    #     token = client.get_formio_access_token()
    #     form_path = current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
    #     return client.fetch_form_id_by_path(form_path=form_path, token=token)
    
    # def update_formio_item(self, status_value):
    #     client = FormioService()
    #     token = client.get_formio_access_token()

    #     current_app.logger.debug("Partial update form")
    #     file_form_id = self.get_formio_file_form_path()

    #     response = client.partial_update_application(
    #         file_form_id,
    #         self.formio_id,
    #         token,
    #         [
    #             {
    #                 "op": "replace",
    #                 "path": "/data/status",
    #                 "value": status_value
    #             }
    #         ]
    #     )
    #     current_app.logger.debug(response)
