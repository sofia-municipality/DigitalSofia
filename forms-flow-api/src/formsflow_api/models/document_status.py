from .audit_mixin import AuditDateTimeMixin
from .base_model import BaseModel
from .db import db


class DocumentStatus(AuditDateTimeMixin, BaseModel, db.Model):
    id = db.Column(db.Integer, primary_key=True)

    title = db.Column(db.String, nullable=False)
    eurotrust_status = db.Column(db.Integer, nullable=True)
    formio_status = db.Column(db.String, nullable=False)
    documents = db.relationship("DocumentTransaction", back_populates="status")