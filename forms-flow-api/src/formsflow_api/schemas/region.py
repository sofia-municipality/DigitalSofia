from marshmallow import Schema, fields


class RegionSchema(Schema):
    created = fields.Str()
    modified = fields.Str()

    name = fields.Str(required=True)
    code = fields.Str(required=True)
    city_are_code = fields.Int(required=True)
    reference_number_code = fields.Str(required=True)
    ais_code = fields.Str(required=False)
    eik = fields.Str(required=False)
    id = fields.Str(required=False)
    title = fields.Str(required=False)
    client_id = fields.Str(required=False)
    secret_key = fields.Str(required=False)
