"""Enum definitions."""
from enum import Enum


class Role(str, Enum):
    """Authorization header types."""

    ADMIN = 'formsflow-admin'
    TENANT_ADMIN = 'forms-flow-tenant-admin'

    DESIGNER = 'formsflow-designer'
    REVIEWER = 'formsflow-reviewer'
    CLIENT = 'formsflow-client'
    PAGE_ADMIN = 'formsflow-page-admin'
    ANALYTICS_VIEWER = 'formsflow-analytics-viewer'


HTTP_TIMEOUT = 1200
