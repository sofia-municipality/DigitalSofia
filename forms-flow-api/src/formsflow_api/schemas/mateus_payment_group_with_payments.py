from marshmallow import Schema, fields
from .mateus_payment_request import MateusPaymentRequestSchema

class MateusPaymentGroupWithPaymentsSchema(Schema):
    id = fields.Int(data_key="id")
    created = fields.Str()
    modified = fields.Str()

    person_identifier = fields.Str(required=True)
    status = fields.Str(required=False)
    tax_subject_id = fields.Str(required=True)
    payment_id = fields.Str(required=False)
    e_payment_payment_id = fields.Str(required=False)
    access_code = fields.Str(required=False)
    payments = fields.Nested(MateusPaymentRequestSchema, many=True)
    is_notified = fields.Bool(required=False)
    last_notification_try = fields.DateTime(required=False)
    first_notification_try = fields.DateTime(required=False)
    retry_count = fields.Integer(required=False)
