from marshmallow import Schema, fields


class AddressKADSchema(Schema):
    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    code_nm_grao = fields.Str(required=True)
    code_pa = fields.Str(required=True)
    building_number = fields.Str(required=True)
    entrance = fields.Str()
    region_id = fields.Int(required=True)
    section = fields.Str(required=True)
    division = fields.Str()
    post_code = fields.Str(required=True)
    num_permanent_address = fields.Int()
    num_present_address = fields.Int()
    date_change = fields.Str()
    status = fields.Int(required=True)


class AddressKADListSchema(Schema):
    page_no = fields.Int(data_key="pageNo", required=False, allow_none=True)
    limit = fields.Int(required=False, allow_none=True)

    sort_order = fields.Str(data_key="sortOrder", required=False)
    order_by = fields.Str(data_key="sortBy", required=False)
    name_pa = fields.Str(data_key="name", required=True)
    region_id = fields.Int(data_key="region_id", required=True)
    building_number = fields.Str(data_key="buildingNumber", required=False)
