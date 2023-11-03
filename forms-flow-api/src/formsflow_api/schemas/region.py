from marshmallow import Schema, fields


class RegionSchema(Schema):
    created = fields.Str()
    modified = fields.Str()

    name = fields.Str(required=True)
    code = fields.Str(required=True)
    city_are_code = fields.Int(required=True)
    reference_number_code = fields.Str(required=True)
