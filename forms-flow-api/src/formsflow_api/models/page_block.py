import sys

from flask import current_app

from .db import db
from .base_model import BaseModel
from .audit_mixin import AuditDateTimeMixin
from sqlalchemy.ext.mutable import MutableDict
from sqlalchemy.dialects.postgresql import JSONB
from formsflow_api.utils import translation_hybrid


class PageBlock(
    AuditDateTimeMixin,
    BaseModel, 
    db.Model):

    id = db.Column(db.Integer, primary_key=True)
    machine_name = db.Column(db.String, unique=True)
    page = db.Column(db.String)
    attributes_translations = db.Column(MutableDict.as_mutable(JSONB))
    attributes = translation_hybrid(attributes_translations)

    @staticmethod
    def create_from_dict(page_block_info: dict):
        try:
            page_block = PageBlock()

            page_block.machine_name = page_block_info["machine_name"]
            page_block.page = page_block_info["page"]
            page_block.attributes_translations = page_block_info["attributes_translations"]
            page_block.save()
            return page_block
        except:
            current_app.logger.info(sys.exc_info()[0])
            return None

# [
#     {
#         "machine-name": "hero-block",
#         "attributes": {
#             "title": "Новите е-услуги",
#             "content": "Lorem Ipsum",
#             "features": [
#                 "Lorem",
#                 "Ipsum",
#                 "Dolorem"
#             ]
#         }
#     },
#     {
#         "machine-name": "map-action-block",
#         "attributes": {
#             "image-orientation": "left",
#             "image": "BASE64 string",
#             "content": "Новите е-услуги",
#             "cta-items": [
#                 {
#                     "cta-text": "Промяна на адрес",
#                     "cta-class": "",
#                     "cta-href": "href",
#                     "cta-target": "_blank"
#                 }
#             ]
#         }
#     },
#     {
#         "machine-name": "local-taxes-action-block",
#         "attributes": {
#             "image-orientation": "left",
#             "image": "BASE64 string",
#             "content": "Новите е-услуги",
#             "cta-items": [
#                 {
#                     "cta-text": "Промяна на адрес",
#                     "cta-class": "",
#                     "cta-href": "href",
#                     "cta-target": "_blank"
#                 }
#             ]
#         }
#     },
#     {
#         "machine-name": "how-it-works-block",
#         "attributes": {
#             "content": "Как работи услугата",
#             "items": [
#                 {
#                     "image": "BASE64",
#                     "description": "Lorem Ipsum"
#                 }
#             ]
#         }
#     },
#     {
#         "machine-name": "mobile-app-block",
#         "attributes": {
#             "image-orientation": "left",
#             "image": "BASE64 string",
#             "content": "Новите е-услуги",
#             "cta-items": [
#                 {
#                     "cta-text": "Промяна на адрес",
#                     "cta-class": "",
#                     "cta-href": "href",
#                     "cta-target": "_blank"
#                 }
#             ]
#         }
#     }
# ]
