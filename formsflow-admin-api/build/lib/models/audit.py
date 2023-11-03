"""Audit base class."""
from datetime import datetime

from sqlalchemy.ext.declarative import declared_attr

from admin_api.utils.user_context import user_context

from .base_model import BaseModel
from .db import db


class Audit(BaseModel):  # pylint: disable=too-few-public-methods
    """This class provides base methods for Auditable Table."""

    __abstract__ = True

    created_on = db.Column('created_on', db.DateTime, nullable=True, default=datetime.now)
    updated_on = db.Column('updated_on', db.DateTime, default=None, onupdate=datetime.now)

    @declared_attr
    def created_by(cls):  # pylint:disable=no-self-argument, # noqa: N805
        """Return created by."""
        return db.Column('created_by', db.String(50), nullable=True, default=cls._get_user_name)

    @declared_attr
    def updated_by(cls):  # pylint:disable=no-self-argument, # noqa: N805
        """Return updated by."""
        return db.Column('updated_by', db.String(50), nullable=True, default=None, onupdate=cls._get_user_name)

    @staticmethod
    @user_context
    def _get_user_name(**kwargs):
        """Return current user user_name."""
        return kwargs['user'].user_name
