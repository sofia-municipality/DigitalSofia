import sys
import uuid
from flask import current_app

from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db
from formsflow_api.utils import encode_secret


class Region(AuditDateTimeMixin,
             BaseModel,
             db.Model):
    name = db.Column(db.String, nullable=False)
    code = db.Column(db.String, nullable=False)
    city_are_code = db.Column(db.Integer, primary_key=True)
    reference_number_code = db.Column(db.String, nullable=False)
    ais_code = db.Column(db.String, nullable=True)
    eik = db.Column(db.String, nullable=True)
    id = db.Column(db.String, default=uuid.uuid4, nullable=True)
    title = db.Column(db.String, nullable=True)
    client_id = db.Column(db.String, nullable=True)
    secret_key = db.Column(db.String, nullable=True)

    @staticmethod
    def create_from_dict(region_info: dict):
        try:
            region = Region()

            region.name = region_info["name"]
            region.code = region_info["code"]
            region.city_are_code = region_info["city_are_code"]
            region.reference_number_code = region_info["reference_number_code"]
            region.ais_code = region_info["ais_code"]
            region.eik = region_info["eik"]
            region.id = region_info["id"]
            region.title = region_info["title"]
            region.client_id = region_info["client_id"]

            # encode secret key before saving
            secret = current_app.config.get("REGIONS_SECRET_KEY")
            region.secret_key = encode_secret(region_info["secret_key"], secret)

            region.save()
            return region
        except Exception as e:
            current_app.logger.error(f"Exception occurred: {e}")
            current_app.logger.info(sys.exc_info()[0])
            return None

    @staticmethod
    def delete_all():
        try:
            num_rows_deleted = db.session.query(Region).delete()
            db.session.commit()
            return num_rows_deleted
        except:
            db.session.rollback()
            return sys.exc_info()[0]

    @classmethod
    def get_all(cls):
        return cls.query.all()

    @classmethod
    def get_by_client_id(cls, client_id):
        return cls.query.filter_by(client_id=client_id).first()

    @classmethod
    def get_by_city_area_code(cls, city_are_code):
        return cls.query.filter_by(city_are_code=city_are_code).first()

    @classmethod
    def get_by_id(cls, id):
        return cls.query.filter_by(id=id).first()
