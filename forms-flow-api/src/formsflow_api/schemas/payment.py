from marshmallow import Schema, fields


class PaymentSchema(Schema):
    payment_id = fields.Int(data_key="paymentId", required=True, allow_none=False)
