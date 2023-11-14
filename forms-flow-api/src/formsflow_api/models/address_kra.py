import sys
from http import HTTPStatus

from flask import current_app
from formsflow_api_utils.exceptions import BusinessException

from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db


class AddressKRA(AuditDateTimeMixin,
                 BaseModel,
                 db.Model):
    __tablename__ = 'addresses_kra'

    id = db.Column(db.Integer, primary_key=True)
    code_nm_grao = db.Column(db.String, nullable=False)
    code_pa = db.Column(db.String, nullable=False)
    name_pa = db.Column(db.String)
    vid_pa = db.Column(db.Integer, nullable=False)
    data_change = db.Column(db.String)
    status = db.Column(db.Integer, nullable=False)

    @staticmethod
    def create_from_dict(address_info: dict):
        try:
            address = AddressKRA()

            address.code_nm_grao = address_info["code_nm_grao"]
            address.code_pa = address_info["code_pa"]
            address.name_pa = address_info["name_pa"]
            address.vid_pa = address_info["vid_pa"]
            address.data_change = address_info["data_change"]
            address.status = address_info["status"]
            address.save()
            return address
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None

    @staticmethod
    def delete_all():
        try:
            num_rows_deleted = db.session.query(AddressKRA).delete()
            db.session.commit()
            return num_rows_deleted
        except:
            db.session.rollback()
            return sys.exc_info()[0]

    @classmethod
    def get_all(
            cls,
            page_number=None,
            limit=None,
            sort_by="name_pa",
            sort_order="asc",
            **filters,
    ):

        result = cls.query.with_entities(AddressKRA.name_pa).distinct()
        name_pa = filters.get("name_pa")
        if name_pa is not None:
            search = "%{}%".format(name_pa.upper())
            result = result.filter(AddressKRA.name_pa.like(search))

        if sort_by and sort_order:
            try:
                attribute = getattr(AddressKRA, sort_by)
                order = getattr(attribute, sort_order)
                result.order_by(order())
            except AttributeError:
                raise BusinessException(
                    "Invalid sort or order.", HTTPStatus.BAD_REQUEST
                )
        total_count = result.count()
        result = result.paginate(page=page_number, per_page=limit)

        return result.items, result.pages, total_count
