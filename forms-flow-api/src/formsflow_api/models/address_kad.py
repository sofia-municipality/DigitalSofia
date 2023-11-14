import sys
from http import HTTPStatus

from flask import current_app
from formsflow_api_utils.exceptions import BusinessException
from sqlalchemy import asc

from . import AddressKRA, Region
from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db


class AddressKAD(AuditDateTimeMixin,
                 BaseModel,
                 db.Model):
    __tablename__ = 'addresses_kad'

    id = db.Column(db.Integer, primary_key=True)
    code_nm_grao = db.Column(db.String, nullable=False)
    code_pa = db.Column(db.String, nullable=False,)
    building_number = db.Column(db.String, nullable=False)
    entrance = db.Column(db.String)
    region_id = db.Column(db.Integer, db.ForeignKey("region.city_are_code"), nullable=False)
    section = db.Column(db.String, nullable=False)
    division = db.Column(db.String)
    post_code = db.Column(db.String, nullable=False)
    num_permanent_address = db.Column(db.Integer)
    num_present_address = db.Column(db.Integer)
    date_change = db.Column(db.String)
    status = db.Column(db.Integer, nullable=False)

    @staticmethod
    def create_from_dict(address_info: dict):
        try:
            address = AddressKAD()

            address.code_nm_grao = address_info["code_nm_grao"]
            address.code_pa = address_info["code_pa"]
            address.building_number = address_info["building_number"]
            address.entrance = address_info["entrance"]
            address.region_id = address_info["region_id"]
            address.section = address_info["section"]
            address.division = address_info["division"]
            address.post_code = address_info["post_code"]
            address.num_permanent_address = address_info["num_permanent_address"]
            address.num_present_address = address_info["num_present_address"]
            address.date_change = address_info["date_change"]
            address.status = address_info["status"]
            address.save()
            return address
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None

    @staticmethod
    def delete_all():
        try:
            num_rows_deleted = db.session.query(AddressKAD).delete()
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
            sort_by="building_number",
            sort_order="desc",
            **filters,
    ):

        name_pa = filters.get("name_pa")
        building_number = filters.get("building_number")
        region_id = filters.get("region_id")
        result = cls.query\
            .join(AddressKRA, AddressKAD.code_pa == AddressKRA.code_pa and AddressKAD.code_nm_grao == AddressKRA.code_nm_grao)\
            .filter(AddressKRA.name_pa == name_pa,AddressKAD.region_id == region_id)\
            .with_entities(AddressKAD.building_number, AddressKAD.region_id)\
            .distinct(AddressKAD.building_number)

        if building_number is not None:
            search_b = "%{}%".format(building_number.upper())
            result = result.filter(AddressKAD.building_number.like(search_b))

        if sort_by and sort_order:
            try:
                attribute = getattr(AddressKAD, sort_by)
                order = getattr(attribute, sort_order)
                result.order_by(order())
            except AttributeError:
                raise BusinessException(
                    "Invalid sort or order.", HTTPStatus.BAD_REQUEST
                )

        total_count = result.count()
        result = result.paginate(page=page_number, per_page=limit)
        return result.items, result.pages, total_count

    @classmethod
    def get_regions_for_streets(cls, streets):
        filter_values = []
        for street in streets:
            filter_values.append(street["name_pa"])
        result = cls.query \
            .join(AddressKRA,
                  AddressKAD.code_pa == AddressKRA.code_pa and AddressKAD.code_nm_grao == AddressKRA.code_nm_grao) \
            .join(Region, Region.city_are_code == AddressKAD.region_id) \
            .filter(AddressKRA.name_pa.in_(filter_values)) \
            .with_entities(AddressKRA.name_pa, AddressKAD.region_id, Region.name.label('region_name')) \
            .distinct(AddressKRA.name_pa, AddressKAD.region_id ).all()
        return result
