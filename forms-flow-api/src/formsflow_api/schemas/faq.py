from marshmallow import EXCLUDE, Schema, fields


class FAQSchema(Schema):
    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    title = fields.Str(required=True)
    content = fields.Str(required=True)
    is_favoured = fields.Bool(data_key="isFavoured",requred=True)


class FAQListSchema(Schema):
    id = fields.Int(data_key="id")
    
    modified = fields.Str()
    page_no = fields.Int(data_key="pageNo", required=False, allow_none=True)
    limit = fields.Int(required=False, allow_none=True)

    sort_order = fields.Str(data_key="sortOrder", required=False)
    order_by = fields.Str(data_key="sortBy", required=False)
    is_favoured = fields.Bool(data_key="isFavoured",required=False)
