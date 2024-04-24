from .audit_mixin import AuditDateTimeMixin
from .base_model import BaseModel
from .db import db
from formsflow_api_utils.exceptions import BusinessException
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
    origin_form_formio_id = db.Column(db.String(24), nullable=True)
    signature_source = db.Column(db.String, nullable=True)

    status = db.relationship(
        "DocumentStatus", back_populates="documents"
    )

    def update_status(self, status: DocumentStatus):
        self.status_id = status.id

    def update_status_send_notification(self, new_status: DocumentStatus):
        from formsflow_api.services import KeycloakAdminAPIService, FirebaseService
        current_app.logger.info("DocumentTransaction@update_status_send_notification")
        current_app.logger.debug(f"|{new_status.id}| == |{self.status_id}|")
        if new_status.id == self.status_id:
            return
        
        self.status_id = new_status.id
        self.commit()

        current_app.logger.debug(f"Signature Source - {self.signature_source}")
        if not self.signature_source or self.signature_source not in ["digitalSofia"]:
            return

        keycloak_client = KeycloakAdminAPIService()

        url_path = f"users?username={self.user_email}&exact={True}"
        keycloak_users = keycloak_client.get_request(url_path)

        if keycloak_users:
            keycloak_user = keycloak_users[0]    

            keycloak_user_attributes = keycloak_user.get("attributes")
            firebase_user_registration_token = keycloak_user_attributes.get("fcm", None)
            current_app.logger.debug(f"User has fcm - {firebase_user_registration_token}")
            if firebase_user_registration_token:
                current_app.logger.debug("Sending message to")
                firebase_user_registration_token = firebase_user_registration_token[0]
                firebase_client = FirebaseService()
                firebase_client.send_status_change_message(
                    transaction=self, 
                    firebase_user_registration_token=firebase_user_registration_token
                )

        
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
