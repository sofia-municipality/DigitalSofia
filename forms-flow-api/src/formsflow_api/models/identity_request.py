from .audit_mixin import AuditDateTimeMixin
from .base_model import BaseModel
from .db import db
import datetime


class IdentityRequest(AuditDateTimeMixin, BaseModel, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    
    transaction_id = db.Column(db.String(12), nullable=False)
    thread_id = db.Column(db.String(12), nullable=False)
    tenant_key = db.Column(db.String, nullable=True)

    person_identifier = db.Column(db.String(16), nullable=False)

    valid_untill = db.Column(db.DateTime, nullable=False, default=datetime.datetime.utcnow)
    # validated = db.Column(db.Boolean, unique=False, default=False)
