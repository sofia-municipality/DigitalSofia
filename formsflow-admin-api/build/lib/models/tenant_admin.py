"""Tenant admin model."""
from enum import Enum, unique

from sqlalchemy import Column, ForeignKey, Integer, String
from sqlalchemy.dialects.postgresql import ENUM
from sqlalchemy.orm import relationship

from .base_model import BaseModel


@unique
class AdminType(Enum):
    """Admin type enum."""

    WEB = 'WEB'
    ANALYTICS = 'ANALYTICS'
    BPM = 'BPM'


class TenantAdmin(BaseModel):
    """Model class for Tenant Contact."""

    __tablename__ = 'tenant_admins'

    id = Column(Integer, primary_key=True, autoincrement=True)

    tenant_id = Column(ForeignKey('tenants.id'), nullable=False)
    tenant = relationship('Tenant', foreign_keys=[tenant_id], lazy='select')

    user_name = Column(String(), nullable=False, unique=True)
    type = Column(ENUM(AdminType, name="AdminType"), nullable=False, default=AdminType.BPM)
