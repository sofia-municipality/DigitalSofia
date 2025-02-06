from flask_restx import fields
from formsflow_api.resources.payment.namespace import API

payment_result_model = API.model(
    "PaymentResult",
    {
        "paymentId": fields.String(),
        "registrationTime": fields.Integer(),
        "accessCode": fields.String(),
    }
)
payment_status_model = API.model(
    "PaymentStatusResult",
    {
        "paymentId": fields.String(),
        "status": fields.String(),
        "changeTime": fields.Integer(),
    }
)

payment_status_callback_model = API.model(
    "PaymentStatusCallback",
    {
        "clientId": fields.String(),
        "hmac": fields.String(),
        "data": fields.String(),
    },
)