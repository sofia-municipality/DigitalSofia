from typing import Optional

from .db import db
from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin


class UserStatusTransaction(BaseModel, db.Model, AuditDateTimeMixin):
    __tablename__ = "user_status_transaction"

    id = db.Column(db.Integer, primary_key=True)
    user_identifier = db.Column(db.String(320), nullable=False)

    @classmethod
    def insert(cls, identifier: str) -> "UserStatusTransaction":
        user_status_transaction = cls(user_identifier=identifier)
        db.session.add(user_status_transaction)
        db.session.commit()
        return user_status_transaction
    
    @classmethod
    def select_by_user_identifier(cls, identifier: str) -> Optional["UserStatusTransaction"]:
        return cls.query.filter_by(user_identifier=identifier).first() 
    
    @classmethod
    def select_all(cls) -> list["UserStatusTransaction"]:
        user_status_transactions = cls.query.all()
        return user_status_transactions
