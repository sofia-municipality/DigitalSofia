from marshmallow import Schema, fields

from formsflow_api.schemas import AddressKADSchema


class AddressKRASchema(Schema):

    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    code_nm_grao = fields.Str(required=True)
    code_pa = fields.Str(required=True)
    name_pa = fields.Str()
    vid_pa = fields.Int(required=True)
    data_change = fields.Str()
    region_id = fields.Int()
    region_name = fields.Str()
    status = fields.Int(required=True)
    address_kra = AddressKADSchema


class AddressKRAListSchema(Schema):

    modified = fields.Str()
    page_no = fields.Int(data_key="pageNo", required=False, allow_none=True)
    limit = fields.Int(required=False, allow_none=True)

    sort_order = fields.Str(data_key="sortOrder", required=False)
    order_by = fields.Str(data_key="sortBy", required=False)
    name_pa = fields.Str(data_key="name", required=False)
