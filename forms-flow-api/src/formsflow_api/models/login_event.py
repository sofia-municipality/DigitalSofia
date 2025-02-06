from datetime import datetime
from typing import Optional

from .db import db
from .base_model import BaseModel


class LoginEventModel(BaseModel, db.Model):
    __tablename__ = "login_events"

    id = db.Column(db.Integer, primary_key=True)
    user_identifier = db.Column(db.String(320), nullable=False)
    modified = db.Column(db.DateTime, default=datetime.now)
    is_official = db.Column(db.Boolean, default=False)
    have_service = db.Column(db.Boolean, default=False)

    @classmethod
    def get_by_user_identifier(
        cls, user_identifier: str
    ) -> Optional["LoginEventModel"]:
        login_event = cls.query.filter_by(user_identifier=user_identifier).one_or_none()
        return login_event

    @classmethod
    def insert_default_with_user_identifier(cls, user_identifier: str) -> None:
        login_event = cls(user_identifier=user_identifier)
        db.session.add(login_event)
        db.session.commit()

    @classmethod
    def select_events_for_unactive_users_by_criteria(cls, filter_datetime: datetime) -> list["LoginEventModel"]:
        login_events = cls.query.filter(cls.modified < filter_datetime, cls.is_official == False).all()
        return login_events
    
    @classmethod
    def delete_by_user_identifier(cls, user_identifier: str) -> None:
        login_event = cls.get_by_user_identifier(user_identifier)
        if login_event:
            login_event.delete()

    @classmethod
    def set_have_service_by_identifier(cls,  user_identifier: str) -> None:
        login_event = cls.get_by_user_identifier(user_identifier)
        if login_event:
            login_event.have_service = True
            db.session.add(login_event)
            db.session.commit()

    @classmethod
    def set_is_official_by_identifier(cls,  user_identifier: str) -> None:
        login_event =  cls.get_by_user_identifier(user_identifier)
        if login_event:
            login_event.is_official = True
            db.session.add(login_event)
            db.session.commit()

    def update_login_event(self, new_datetime: datetime) -> None:
        self.modified = new_datetime
        db.session.add(self)
        db.session.commit()
