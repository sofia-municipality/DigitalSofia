"""Enum definitions."""
from enum import Enum


class Role(str, Enum):
    """Authorization header types."""

    ADMIN = 'forms-flow-admin'
    TENANT_ADMIN = 'forms-flow-tenant-admin'

    DESIGNER = 'formsflow-designer'
    REVIEWER = 'formsflow-reviewer'
    CLIENT = 'formsflow-client'


HTTP_TIMEOUT = 300
