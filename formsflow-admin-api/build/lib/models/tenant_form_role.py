"""Tenant admin model."""
from enum import Enum, unique

from sqlalchemy import Column, ForeignKey, Integer, String, and_, or_
from sqlalchemy.dialects.postgresql import ENUM
from sqlalchemy.orm import relationship

from .base_model import BaseModel


@unique
class RoleType(Enum):
    """Admin type enum."""

    CLIENT = 'CLIENT'
    DESIGNER = 'DESIGNER'
    REVIEWER = 'REVIEWER'
    ANONYMOUS = 'ANONYMOUS'
    RESOURCE_ID = 'RESOURCE_ID'


class TenantRole(BaseModel):
    """Model class for Tenant Role."""

    __tablename__ = 'tenant_roles'

    id = Column(Integer, primary_key=True, autoincrement=True)

    tenant_id = Column(ForeignKey('tenants.id'), nullable=True)
    tenant = relationship('Tenant', foreign_keys=[tenant_id], lazy='select')

    role_id = Column(String(), nullable=False)
    type = Column(ENUM(RoleType, name="RoleType"), nullable=False, index=True)

    @classmethod
    def find_by_tenant_id_and_type(cls, tenant_id: int, role_type: RoleType):
        """Return tenant role by tenant id and role type."""
        query = cls.query
        if role_type != RoleType.DESIGNER:
            query = query.filter(or_(and_(TenantRole.tenant_id == tenant_id, TenantRole.type == role_type),
                                     TenantRole.type == RoleType.RESOURCE_ID))
        else:
            query = query.filter(
                or_(TenantRole.tenant_id == tenant_id, TenantRole.type.in_([RoleType.ANONYMOUS, RoleType.RESOURCE_ID])))
        return query.all()

    @classmethod
    def find_anonymous_role(cls):
        """Return anonymous role record."""
        return cls.query.filter(TenantRole.type == RoleType.ANONYMOUS).all()

    @classmethod
    def find_resource_id(cls):
        """Return resource id record."""
        return cls.query.filter(TenantRole.type == RoleType.RESOURCE_ID).all()
