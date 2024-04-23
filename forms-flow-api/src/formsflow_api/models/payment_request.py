import sys
from flask import current_app
from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db


class PaymentRequest(AuditDateTimeMixin,
                     BaseModel,
                     db.Model):
    id = db.Column(db.Integer, primary_key=True)
    payment_id = db.Column(db.String, nullable=False)
    application_id = db.Column(db.Integer, db.ForeignKey("application.id"), nullable=False)
    access_code = db.Column(db.String, nullable=False)

    @classmethod
    def create_from_dict(cls, payment_info: dict):
        try:
            payment_request = PaymentRequest()
            payment_request.payment_id = payment_info["payment_id"]
            payment_request.application_id = payment_info["application_id"]
            payment_request.access_code = payment_info["access_code"]
            payment_request.save()
            return payment_request
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None

    @staticmethod
    def get_by_application_id(application_id: int):
        return PaymentRequest.query.filter_by(application_id=application_id).first()
    @staticmethod
    def get_by_payment_id(payment_id: str):
        return PaymentRequest.query.filter_by(payment_id=payment_id).first()

    def to_json(self):
        return {
            "paymentId": self.payment_id,
            "accessCode": self.access_code
        }
