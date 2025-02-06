import sys
from http import HTTPStatus

from flask import current_app
from formsflow_api_utils.exceptions import BusinessException
from sqlalchemy.orm import aliased
from sqlalchemy import select, distinct

from . import AddressKRA, Region
from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from .db import db
from ..utils import SOFIA_CITY_CODES


class AddressKAD(AuditDateTimeMixin,
                 BaseModel,
                 db.Model):
    __tablename__ = 'addresses_kad'

    id = db.Column(db.Integer, primary_key=True)
    code_nm_grao = db.Column(db.String, nullable=False)
    code_pa = db.Column(db.String, nullable=False, )
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
        result = cls.query \
            .join(AddressKRA,
                  AddressKAD.code_pa == AddressKRA.code_pa and AddressKAD.code_nm_grao == AddressKRA.code_nm_grao) \
            .filter(AddressKRA.name_pa == name_pa, AddressKAD.region_id == region_id) \
            .with_entities(AddressKAD.building_number, AddressKAD.region_id) \
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
    def get_regions_for_streets(cls, streets, page_number=None,
                                limit=None,
                                **filters):

        kra_alias = aliased(AddressKRA)
        kad_alias = aliased(AddressKAD)

        filter_values = [street["name_pa"] for street in streets]

        current_app.logger.info('Filter values: %s', filter_values)

        query = db.session.query(
            distinct(kra_alias.name_pa),  # Select distinct name_pa from kra
            kra_alias.name_pa,
            kad_alias.region_id,
            Region.name.label("region_name")
        ).join(
            kra_alias,
            (kad_alias.code_nm_grao == kra_alias.code_nm_grao) &
            (kad_alias.code_pa == kra_alias.code_pa)
        ).join(
            Region,
            kad_alias.region_id == Region.city_are_code
        ).filter(
            kra_alias.name_pa.in_(filter_values),
            kad_alias.code_nm_grao.in_(SOFIA_CITY_CODES)
        )

        total_count = query.count()
        result = query.paginate(page=page_number, per_page=limit)

        return result.items, result.pages, total_count
