import sys

from flask import current_app
from sqlalchemy import asc, and_, or_
from sqlalchemy.orm import joinedload, Query
from sqlalchemy.sql import or_

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
    payment_id = db.Column(db.String, nullable=True)
    e_payment_payment_id = db.Column(db.String, nullable=True)
    access_code = db.Column(db.String, nullable=True)
    payments = db.relationship('MateusPaymentRequest', back_populates='group')
    is_notified = db.Column(db.Boolean, nullable=True, default=False) # Indicates if Mateus was seccessfuly notified
    last_notification_try = db.Column(db.DateTime, nullable=True) # When the last attempt for Mateus notification was made
    first_notification_try = db.Column(db.DateTime, nullable=True) # When the first attempt for Mateus notification was made
    retry_count = db.Column(db.Integer, nullable=False, default=0) # Number of attempts for Mateus notification made so far
    pay_transaction_id = db.Column(db.String, nullable=True, default=0) # Returned property from Mateus
    transaction_time = db.Column(db.DateTime, nullable=True) # Returned property from Mateus
    agent_transaction_id = db.Column(db.String, nullable=True, default=0) # Returned property from Mateus

    def to_json(self):
        return {
            "person_identifier": self.person_identifier,
            "status": self.status,
            "tax_subject_id": self.tax_subject_id,
            "payment_id": self.payment_id,
            "access_code": self.access_code,
            "id": self.id,
            "is_notified": self.is_notified,
            "last_notification_try": self.last_notification_try,
            "first_notification_try": self.first_notification_try,
            "agent_transaction_id": self.agent_transaction_id,
            "transaction_time": self.transaction_time,
            "pay_transaction_id": self.pay_transaction_id,
            "retry_count": self.retry_count
        }
    
    def to_json_with_payments(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
    
    @staticmethod
    def create_from_dict(mateus_payment_group_info: dict):
        mateus_payment_group = MateusPaymentGroup()
        mateus_payment_group.person_identifier = mateus_payment_group_info["person_identifier"]
        mateus_payment_group.status = mateus_payment_group_info["status"]
        mateus_payment_group.tax_subject_id = mateus_payment_group_info["tax_subject_id"]
        mateus_payment_group.payment_id = mateus_payment_group_info["payment_id"]
        
        db.session.add(mateus_payment_group)
        db.session.flush()
        
        return mateus_payment_group

    @staticmethod
    def find_by_personal_identifier(person_identifier: str, limit=10, page=1):
        result = MateusPaymentGroup.query.filter_by(person_identifier=str(person_identifier)).order_by(MateusPaymentGroup.created.desc())
        total_items = result.count()
        result = result.paginate(page=int(page), per_page=int(limit))
        return result.items, result.pages, total_items
    
    @classmethod
    def find_all_by_personal_identifier(cls, filter_by_pi_query: Query):
        result = filter_by_pi_query.all()
        return result
    
    @classmethod
    def delete_by_personal_identifier(cls, filter_by_pi_query: Query) -> int:
        count = filter_by_pi_query.delete()
        db.session.commit()
        return count
    
    @classmethod
    def find_all_by_personal_identifier_query(cls, person_identifier: str) -> Query:
        query = cls.query.filter_by(person_identifier=str(person_identifier))
        return query
    
    @classmethod
    def select_count_by_query(cls, query: Query) -> int:
        count = query.count()
        return count

    @staticmethod
    def get_pending_notification(limit=100, page=1):
        result = MateusPaymentGroup.query.filter(MateusPaymentGroup.is_notified==False, MateusPaymentGroup.status == 'Paid').order_by(MateusPaymentGroup.created.asc())
        total_items = result.count()
        result = result.paginate(page=int(page), per_page=int(limit))
        return result.items, result.pages, total_items

    @staticmethod
    def find_by_id(group_id: int):
        return MateusPaymentGroup.query.options(joinedload(MateusPaymentGroup.payments)).filter_by(id=group_id).first()

    @staticmethod
    def update_status(mateus_payment_group_info: dict):
        try:
            mateus_payment_group = MateusPaymentGroup.query.filter_by(id=mateus_payment_group_info["id"]).first()
            mateus_payment_group.status = mateus_payment_group_info["status"]
            mateus_payment_group.save()
            return mateus_payment_group
        
        except Exception as e:
            # Log the error
            current_app.logger.error(f"Error in bulk_insert: {e}")
            return None

    @staticmethod    
    def update_from_dict(properties: dict):
        mateus_payment_group = MateusPaymentGroup.query.filter_by(id=properties["id"]).first()
        if mateus_payment_group is not None:
            for key, value in properties.items():
                if hasattr(mateus_payment_group, key):
                    setattr(mateus_payment_group, key, value)
            db.session.add(mateus_payment_group)
        return mateus_payment_group
        
    @staticmethod
    def find_by_status(status: str):
        return MateusPaymentGroup.query.options(joinedload(MateusPaymentGroup.payments)).filter_by(status=status).all()
    
    @staticmethod
    def check_payment_status_by_person_id(person_identifier: str):
        current_app.logger.info("Checking payment status for person_identifier: %s", person_identifier)
        return MateusPaymentGroup.query.filter(MateusPaymentGroup.person_identifier == person_identifier,
            or_(
                MateusPaymentGroup.status == 'Pending',
                and_(
                    MateusPaymentGroup.status == 'Paid',
                    MateusPaymentGroup.is_notified == False
                )
            )).all()
    
    @staticmethod
    def delete(group_id: int):
        try:
            # Find the group
            mateus_payment_group = MateusPaymentGroup.query.filter_by(id=group_id).first()

            # If the group doesn't exist, return an error
            if mateus_payment_group is None:
                return "Error: No group found with the given id."

            # Delete the group
            db.session.delete(mateus_payment_group)

            # Commit the changes
            db.session.commit()

            return "Success: Group deleted."

        except:
            current_app.logger.info(sys.exc_info()[0])
            return "Error: Could not delete group."

    @staticmethod
    def get_by_payment_id(payment_id: str):
        return MateusPaymentGroup.query.filter_by(e_payment_payment_id=payment_id).first()
