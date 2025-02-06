from datetime import datetime, timedelta
import pytz
from flask import current_app
from typing import Dict

from formsflow_api_utils.utils import UserContext


class EFormIntegrationsTransformer:
    def __init__(self):
        current_app.logger.debug("EFormIntegrationsTransformer Init")

    @staticmethod
    def payment_request(payment_id, amount, reason, personIdentifier, user_name) -> Dict:
        """
        Transforms the provided arguments into a payment request.

        Returns:
            Dict: A dictionary representing a payment request, with the following properties:
                - group_payment_request_id: A unique identifier for the payment request.
                - request: A dictionary containing the payment request details, including:
                    - paymentRequest: A dictionary containing the payment details, including:
                        - actors: A list of actors involved in the payment.
                        - paymentData: A dictionary containing the payment data, including:
                            - paymentId: A unique identifier for the payment.
                            - currency: The currency of the payment.
                            - amount: The amount of the payment.
                            - referenceNumber: A reference number for the payment.
                            - referenceType: A reference type for the payment.
                            - referenceDate: The date of the payment.
                            - expirationDate: The expiration date of the payment.
                            - reason: The reason for the payment.
                            - administrativeServiceUri: The service URI for the payment.
                            - administrativeServiceSupplierUri: The supplier URI for the payment.
                            - administrativeServiceNotificationURL: The notification URL for the payment.
                            - obligationType: The type of obligation for the payment.
                    - eserviceClientId: The client ID for the eService.
        """
        current_date = datetime.utcnow().replace(tzinfo=pytz.utc)
        current_date_formatted = datetime.isoformat(current_date)
        expiration_date = datetime.isoformat(current_date + timedelta(minutes=15))  # 15 minutes expiration time

        # ASK VALIO ABOUT THIS
        service_notification_url = (f"{current_app.config.get('FORMSFLOW_API_URL')}/payment/payment-status-callback"
                                    f"?message=PaymentStatusMessage&fieldId=ePaymentId")

        return {
            "group_payment_request_id": payment_id,
            "request": {
                "paymentRequest": {
                    "actors": [{
                        "type": "PERSON",
                        "uid": {
                            "type": "EGN",
                            "value": personIdentifier
                        },
                        "name": user_name,
                        "participantType": "APPLICANT"
                    }],
                    "paymentData": {
                        "paymentId": payment_id,
                        "currency": "BGN",
                        "amount": float(round(sum(amount), 2)),
                        "referenceNumber": payment_id,
                        "referenceType": "9",
                        "referenceDate": current_date_formatted,
                        "expirationDate": expiration_date,
                        # "reason": "Плащане на задължения за местни данъци и такси " + ','.join(reason),
                        "reason": "Плащане на задължения за местни данъци и такси",
                        "administrativeServiceUri": 2410,  # this is the service uri for the eService
                        "administrativeServiceSupplierUri": "000696327",  # this is the supplier uri for the eService
                        "administrativeServiceNotificationURL": service_notification_url,
                        "obligationType": 1
                    }
                },
                "eserviceClientId": current_app.config.get("MATEUS_PAYMENT_CLIENT_ID")
                # this is the client id for the eService
            }
        }
