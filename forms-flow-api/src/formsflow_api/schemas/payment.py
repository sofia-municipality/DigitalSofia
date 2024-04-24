from marshmallow import (
    Schema, 
    fields, 
    validates, 
    ValidationError
)


class PaymentSchema(Schema):
    payment_id = fields.Int(data_key="paymentId", required=True, allow_none=False)
    application_id = fields.Int(data_key="applicationId", required=False, allow_none=True)

class PaymentCancelledResolveSchema(Schema):
    status = fields.String(data_key="status", required=True, allow_none=False)

    @validates("status")
    def validate_status(self, status_name):
        valid_statuses = ["paid", "canceled"]
        if status_name not in valid_statuses:
            raise ValidationError("Invalid status value. Valid values are:" + ",".join(valid_statuses))
