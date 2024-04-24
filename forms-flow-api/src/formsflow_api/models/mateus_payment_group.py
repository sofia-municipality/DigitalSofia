import sys
from http import HTTPStatus

from flask import current_app
from formsflow_api_utils.exceptions import BusinessException
from sqlalchemy import asc

from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db


class MateusPaymentGroup(AuditDateTimeMixin,
                         BaseModel,
                         db.Model):
    __tablename__ = 'mateus_payment_group'

    id = db.Column(db.Integer, primary_key=True)

    person_identifier = db.Column(db.String, nullable=False)
    status = db.Column(db.String, nullable=True)
    tax_subject_id = db.Column(db.String, nullable=False)
    payments = db.relationship('MateusPaymentRequest', backref='mateus_payment_group', lazy=True)

    @staticmethod
    def create_from_dict(mateus_payment_group_info: dict):
        try:
            mateus_payment_group = MateusPaymentGroup()
            mateus_payment_group.person_identifier = mateus_payment_group_info["person_identifier"]
            mateus_payment_group.status = mateus_payment_group_info["status"]
            mateus_payment_group.tax_subject_id = mateus_payment_group_info["tax_subject_id"]
            mateus_payment_group.save()
            return mateus_payment_group
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None

    @staticmethod
    def find_by_personal_identifier(person_identifier: str, limit=10, page=1):
        result = MateusPaymentGroup.query.filter_by(person_identifier=str(person_identifier)).order_by(MateusPaymentGroup.created.desc())
        total_items = result.count()
        result = result.paginate(page=int(page), per_page=int(limit))
        return result.items, result.pages, total_items

    @staticmethod
    def find_by_id(group_id: int):
        return MateusPaymentGroup.query.filter_by(id=group_id).first()

    def to_json(self):
        return {
            "person_identifier": self.person_identifier,
            "status": self.status,
            "tax_subject_id": self.tax_subject_id,
            "id": self.id
        }

    @staticmethod
    def update_from_dict(mateus_payment_group_info: dict):
        try:
            mateus_payment_group = MateusPaymentGroup.query.filter_by(id=mateus_payment_group_info["id"]).first()
            mateus_payment_group.status = mateus_payment_group_info["status"]
            mateus_payment_group.save()
            db.session.commit()
            return mateus_payment_group

        except:
            current_app.logger.info(sys.exc_info()[0])
            return None

    @staticmethod
    def find_by_status(status: str):
        result = MateusPaymentGroup.query.filter_by(status=status).all()
        return result
