from .db import db
import datetime
from flask import current_app
from .base_model import BaseModel


class OtherFile(BaseModel, db.Model):
    
    id = db.Column(db.Integer, primary_key=True)
    file_hash = db.Column(db.String, unique=True, nullable=False)
    file_name = db.Column(db.String, nullable=False)
    file_mimetype = db.Column(db.String, nullable=False)
    file_size = db.Column(db.Integer, nullable=False)
    path = db.Column(db.String, nullable=True)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.datetime.utcnow)
    client_id = db.Column(db.String, nullable=False)
    application_id = db.Column(db.String, nullable=True)
    # pass

    @property
    def file_url(self):
        api_base = current_app.config.get("FORMSFLOW_API_URL")
        return f"{api_base}/external-services/documents/{self.file_hash}"

    @property
    def file_path(self):
        path = self.path + "/" if self.path else ""
        path += self.file_hash
        return path