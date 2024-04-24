from marshmallow import Schema, fields


class MateusPaymentRequestSchema(Schema):
    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    person_identifier = fields.Str(required=True)
    status = fields.Str(required=True)
    payment_id = fields.Str(required=True)
    access_code = fields.Str(required=True)
    group_id = fields.Int(required=True)
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

