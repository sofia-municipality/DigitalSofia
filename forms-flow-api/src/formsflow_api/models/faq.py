from __future__ import annotations

from flask import current_app, request
from .audit_mixin import AuditDateTimeMixin
from .base_model import BaseModel
from .db import db
from sqlalchemy import update
from sqlalchemy.ext.mutable import MutableDict
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy_utils import TranslationHybrid, force_auto_coercion
from formsflow_api_utils.exceptions import BusinessException
from sqlalchemy.orm.attributes import flag_modified
from http import HTTPStatus
from formsflow_api.utils import translation_hybrid, get_locale


class FAQ(AuditDateTimeMixin, BaseModel, db.Model):
    id = db.Column(db.Integer, primary_key=True)

    title_translations = db.Column(MutableDict.as_mutable(JSONB))
    content_translations = db.Column(MutableDict.as_mutable(JSONB))
    is_favoured = db.Column(db.Boolean, unique=False, default=False)

    title = translation_hybrid(title_translations)
    content = translation_hybrid(content_translations)

    # def update_translation_field(self, translation_field_key, translation_field_value):
    #     new_dict = getattr(self, translation_field_key)
    #     new_dict[get_locale()] = translation_field_value
    #     setattr(self, translation_field_key, new_dict)


    def update(self, faq_info: dict):
        ### Translation not updating correclty
        # self.update_translation_field("title_translations", faq_info["title"])
        # self.update_translation_field("content_translations", faq_info["content"])
        self.title = faq_info["title"]
        self.content = faq_info["content"]
        self.is_favoured = faq_info["is_favoured"]

        self.save()

    @classmethod
    def create_from_dict(cls, faq_info: dict) -> FAQ:
        if faq_info:
            faq = FAQ()

            faq.title = faq_info["title"]
            faq.content = faq_info["content"]
            faq.is_favoured = faq_info["is_favoured"]

            faq.save()
            return faq
        return None
    
    @classmethod
    def get_all(
        cls,
        page_number=None,
        limit=None,
        sort_by="id",
        sort_order="desc",
        **filters,
    ):

        result = cls.query


        result = result.filter(FAQ.title_translations[get_locale()] != None)

        is_favoured = filters.get("is_favoured")
        if is_favoured is not None and type(is_favoured) is bool:
            result = result.filter_by(is_favoured=is_favoured)

        if sort_by and sort_order:
            try:
                attribute = getattr(FAQ, sort_by)
                order = getattr(attribute, sort_order)
                result.order_by(order())
            except AttributeError:
                raise BusinessException(
                    "Invalid sort or order.", HTTPStatus.BAD_REQUEST
                )

        total_count = result.count()
        result = result.paginate(page=page_number, per_page=limit)
        return result.items, result.pages, total_count

