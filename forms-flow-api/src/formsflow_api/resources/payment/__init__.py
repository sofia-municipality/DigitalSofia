# __init__.py
from .namespace import API
# from .models import payment_status_callback_model, payment_result_model, payment_status_model

from .payment_post import PaymentResource
from .payment_status import PaymentStatusResource
from .payment_cancelled_handler import PaymentCancelledHandlerResource
from .payment_status_callback import PaymentStatusCallbackResource

API.add_resource(PaymentResource)
API.add_resource(PaymentStatusResource)
API.add_resource(PaymentCancelledHandlerResource)
API.add_resource(PaymentStatusCallbackResource)
