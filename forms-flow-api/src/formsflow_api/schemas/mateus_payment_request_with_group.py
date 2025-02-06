from marshmallow import Schema, fields
from .mateus_payment_group import MateusPaymentGroupSchema

class MateusPaymentRequestWithGroupSchema(Schema):
    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    group_id = fields.Int(required=True)
    group = fields.Nested(MateusPaymentGroupSchema)
    amount = fields.Number(required=True)
    tax_period_year = fields.Int(required=True)
    partida_no = fields.Str(required=True)
    kind_debt_reg_id = fields.Int(required=True)
    pay_order = fields.Int(required=True)
    additional_data = fields.Str(required=True)
    reason = fields.Str(required=True)
    rnu = fields.Str(required=True)
    municipality_id = fields.Int(required=True)
    residual = fields.Number(required=True)
    interest = fields.Number(required=True)
    debt_instalment_id = fields.Int(required=True)

