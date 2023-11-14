"""Tenant model."""
from datetime import datetime
from enum import Enum, unique

from sqlalchemy import JSON, Column, DateTime, Integer, String, func, or_
from sqlalchemy.dialects.postgresql import ENUM

from .audit import Audit


@unique
class Status(Enum):
    """Status enum."""

    PENDING = 'PENDING'
    ACTIVE = 'ACTIVE'
    INACTIVE = 'INACTIVE'
    EXPIRED = 'EXPIRED'


class Tenant(Audit):
    """Model class for Tenants."""

    __tablename__ = 'tenants'

    id = Column(Integer, primary_key=True, autoincrement=True)
    key = Column(String(), nullable=False, unique=True)
    name = Column(String(), nullable=False, unique=True)
    details = Column(JSON, nullable=False)
    status = Column(ENUM(Status, name="Status"), unique=False, nullable=False, default=Status.PENDING)
    expiry_dt = Column(DateTime, nullable=True)

    @classmethod
    def find_tenants(cls, limit: int, page: int, key: str, name: str,  # pylint: disable=too-many-arguments
                     user_name: str = None):
        """Return tenants."""
        from .tenant_admin import AdminType, TenantAdmin  # pylint: disable=import-outside-toplevel

        query = cls.query
        if user_name:
            query = query.join(TenantAdmin, TenantAdmin.tenant_id == Tenant.id) \
                .filter(func.lower(TenantAdmin.user_name) == user_name.lower()) \
                .filter(TenantAdmin.type == AdminType.BPM)
        if key:
            query = query.filter(Tenant.key == key)
        if name:
            query = query.filter(Tenant.name.ilike(f'%{name}%'))

        pagination = query.paginate(per_page=limit, page=page)
        return pagination.items, pagination.total

    @classmethod
    def find_by_key(cls, key: str):
        """Return tenant by key."""
        return cls.query\
            .filter(Tenant.key == key)\
            .filter(Tenant.status == Status.ACTIVE)\
            .filter((Tenant.expiry_dt.is_(None)) | (Tenant.expiry_dt >= datetime.now()))\
            .one_or_none()

    @classmethod
    def find_by_key_or_name(cls, key: str, name: str):
        """Return tenant by key."""
        return cls.query.filter(or_(Tenant.key == key, Tenant.name == name)).one_or_none()
