import sys

from flask import current_app
from formsflow_api_utils.exceptions import BusinessException
from sqlalchemy import asc

from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db


class MateusPaymentRequest(AuditDateTimeMixin,
                           BaseModel,
                           db.Model):
    __tablename__ = 'mateus_payment_request'

    id = db.Column(db.Integer, primary_key=True)

    group_id = db.Column(db.Integer, db.ForeignKey('mateus_payment_group.id'),
                         nullable=False)
    group = db.relationship("MateusPaymentGroup", back_populates="payments")
    amount = db.Column(db.Numeric(10, 2), nullable=False, default=0)
    tax_period_year = db.Column(db.Integer, nullable=False, default=0)
    partida_no = db.Column(db.String, nullable=False, default="")
    kind_debt_reg_id = db.Column(db.Integer, nullable=False, default=0)
    pay_order = db.Column(db.Integer, nullable=False, default=0)
    additional_data = db.Column(db.String, nullable=False, default="")
    reason = db.Column(db.String)

    rnu = db.Column(db.String)
    municipality_id = db.Column(db.Integer)
    debt_instalment_id = db.Column(db.Integer)
    residual = db.Column(db.Numeric(10, 2))
    interest = db.Column(db.Numeric(10, 2))

    @staticmethod
    def create_from_dict(payment_info: dict):
        try:
            mateus_payment_request = MateusPaymentRequest()

            mateus_payment_request.group_id = payment_info["group_id"]
            mateus_payment_request.amount = payment_info["amount"]
            mateus_payment_request.tax_period_year = payment_info["tax_period_year"]
            mateus_payment_request.partida_no = payment_info["partida_no"]
            mateus_payment_request.kind_debt_reg_id = payment_info["kind_debt_reg_id"]
            mateus_payment_request.additional_data = payment_info["additional_data"]
            mateus_payment_request.pay_order = payment_info["pay_order"]
            mateus_payment_request.reason = payment_info["reason"]

            mateus_payment_request.rnu = payment_info["rnu"]
            mateus_payment_request.municipality_id = payment_info["municipalityId"]
            mateus_payment_request.residual = payment_info["residual"]
            mateus_payment_request.interest = payment_info["interest"]
            mateus_payment_request.debt_instalment_id = payment_info["debtInstalmentId"]

            mateus_payment_request.save()
            return mateus_payment_request
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None
        
    @staticmethod
    def bulk_insert(payment_infos):
        try:
            # Convert dictionaries to MateusPaymentRequest instances
            payment_requests = [MateusPaymentRequest(**info) for info in payment_infos]
            # Use bulk_save_objects to insert all payment_requests
            db.session.bulk_save_objects(payment_requests)
        except Exception as e:
            # Log the error
            current_app.logger.error(f"Error in bulk_insert: {e}")
            # Rethrow the error
            raise

    def to_json(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}

    @staticmethod
    def find_by_mateus_payment_group(group_id: str):
        result = MateusPaymentRequest.query.filter_by(group_id=int(group_id)).all().to_json()
        return result
    
    @staticmethod
    def select_by_mateus_payment_group(group_id: str):
        result = MateusPaymentRequest.query.filter_by(group_id=int(group_id)).all()
        return result
    
    @staticmethod
    def delete_by_payment_groups_ids(groups_ids: list[str]) -> int:
        delete_query = MateusPaymentRequest.query.filter(MateusPaymentRequest.group_id.in_(groups_ids))
        count = delete_query.delete()
        db.session.commit()
        return count

    @staticmethod
    def get_by_mateus_payment_group(group_id: int):
        return MateusPaymentRequest.query.filter(MateusPaymentRequest.group_id==group_id).all()

    @staticmethod
    def find_by_reason(reason: str, kind_debt_reg_id: int, amount: float):
        return MateusPaymentRequest.query.filter_by(reason=reason).filter_by(
            kind_debt_reg_id=kind_debt_reg_id).filter_by(amount=amount).order_by(MateusPaymentRequest.created.desc()).first()
    
    @staticmethod
    def update_from_dict(payment_info: dict):
        try:
            current_app.logger.info(payment_info)
            mateus_payment_request = MateusPaymentRequest.query.filter_by(payment_id=payment_info["payment_id"]).first()
            mateus_payment_request.status = payment_info["status"]
            mateus_payment_request.save()
            db.session.commit()
            return mateus_payment_request
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None
