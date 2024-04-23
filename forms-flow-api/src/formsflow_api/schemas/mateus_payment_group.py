from marshmallow import Schema, fields


class MateusPaymentGroupSchema(Schema):
    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    person_identifier = fields.Str(required=True)
    status = fields.Str(required=False)
    tax_subject_id = fields.Str(required=True)
