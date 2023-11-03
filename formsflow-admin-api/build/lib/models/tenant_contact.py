"""Tenant contact model."""
from sqlalchemy import JSON, Column, ForeignKey, Integer
from sqlalchemy.orm import relationship

from .db import db


class TenantContact(db.Model):  # pylint:disable=too-few-public-methods
    """Model class for Tenant Contact."""

    __tablename__ = 'tenant_contacts'

    id = Column(Integer, primary_key=True, autoincrement=True)

    tenant_id = Column(ForeignKey('tenants.id'), nullable=False)
    tenant = relationship('Tenant', foreign_keys=[tenant_id], lazy='select')

    contact = Column(JSON, nullable=False)
