"""Tenant clients model."""
from enum import Enum, unique

from sqlalchemy import Column, ForeignKey, Integer, String
from sqlalchemy.dialects.postgresql import ENUM
from sqlalchemy.orm import relationship

from .base_model import BaseModel


@unique
class ClientType(Enum):
    """Client type enum."""

    WEB = 'WEB'
    ANALYTICS = 'ANALYTICS'
    BPM = 'BPM'


class TenantClient(BaseModel):
    """Model class for Tenant Contact."""

    __tablename__ = 'tenant_clients'

    id = Column(Integer, primary_key=True, autoincrement=True)

    tenant_id = Column(ForeignKey('tenants.id'), nullable=False)
    tenant = relationship('Tenant', foreign_keys=[tenant_id], lazy='select')

    client_type = Column(ENUM(ClientType, name="ClientType"), unique=False, nullable=False)
    client_id = Column(String(), unique=False, nullable=True)
    secret = Column(String(), unique=False, nullable=True)
