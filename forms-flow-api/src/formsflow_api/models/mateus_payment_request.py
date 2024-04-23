import sys
from http import HTTPStatus

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

    person_identifier = db.Column(db.String, nullable=False)
    status = db.Column(db.String, nullable=True)
    payment_id = db.Column(db.String, nullable=False, unique=True)
    payment_request_id = db.Column(db.String, nullable=False, unique=True)
    access_code = db.Column(db.String, nullable=False)
    group_id = db.Column(db.Integer, db.ForeignKey('mateus_payment_group.id'),
                         nullable=False)
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

            mateus_payment_request.person_identifier = payment_info["person_identifier"]
            mateus_payment_request.status = payment_info["status"]
            mateus_payment_request.payment_id = payment_info["payment_id"]
            mateus_payment_request.payment_request_id = payment_info["payment_request_id"]
            mateus_payment_request.access_code = payment_info["access_code"]
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
    def find_by_payment_id(payment_id: str):
        return MateusPaymentRequest.query.filter_by(payment_id=payment_id).order_by(MateusPaymentRequest.created.desc()).first()

    @staticmethod
    def find_by_payment_request_id(payment_request_id: str):
        return MateusPaymentRequest.query.filter_by(payment_request_id=payment_request_id).first()

    @staticmethod
    def find_by_mateus_payment_group(group_id: str):
        result = MateusPaymentRequest.query.filter_by(group_id=int(group_id)).all()
        return result

    @staticmethod
    def find_by_reason(reason: str, kind_debt_reg_id: int):
        return MateusPaymentRequest.query.filter_by(reason=reason).filter_by(
            kind_debt_reg_id=kind_debt_reg_id).order_by(MateusPaymentRequest.created.desc()).first()

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
