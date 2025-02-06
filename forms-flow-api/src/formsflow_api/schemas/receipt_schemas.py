from marshmallow import Schema, fields

class ReceiptSchema(Schema):
    _id = fields.Str()
    form = fields.Str()
    name = fields.Str()
