import pytz

from formsflow_api.models import MateusPaymentGroup, MateusPaymentRequest
from datetime import datetime
from flask import current_app
import os
import hashlib

from formsflow_api.models.valid_debt_reg_id import VALID_DEBT_REG_ID
from formsflow_api.schemas import MateusPaymentRequestSchema
from formsflow_api.services.external import EFormIntegrationsService


class ObligationService:
    def update_status_of_group_payments(self, group_id):
        group = MateusPaymentGroup.find_by_id(group_id).to_json()
        has_pending = False
        has_paid = False
        has_canceled = False

        payment_requests = MateusPaymentRequest.find_by_mateus_payment_group(group_id)
        schema = MateusPaymentRequestSchema()
        payments = schema.dump(payment_requests, many=True)
        result = {
            "real_estate": [],
            "vehicle": [],
            "household_waste": [],
        }
        client = EFormIntegrationsService()

        for payment in payments:
            original_status = payment["status"]
            if payment["status"].capitalize() == "Pending" or payment["status"] == 'Inprocess':
                payment["status"] = client.get_payment_status(payment["payment_id"])["status"].capitalize()
                if payment["status"] == 'Inprocess':
                    payment["status"] = "Pending"
                current_app.logger.info(f"Payment request status: {payment['status']}")
            if payment["status"].capitalize() == "Pending":
                has_pending = True
            elif payment["status"].capitalize() == "Paid" or payment["status"].capitalize() == "Inprocess":
                if original_status != "Paid" and payment["status"].capitalize() == "Paid":
                    current_date = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%S3Z')
                    data = {
                        "companyId": 999,
                        "operatorId": payment['person_identifier'],
                        "agentTransactionId": payment['payment_id'],
                        "agentTransactionDate": current_date,
                        "municipalityId": payment['municipality_id'],
                        "subjectsInstalments": [
                            {
                                "taxSubjectId": int(group["tax_subject_id"]),
                                "instalments": [
                                    {
                                        "debtInstalmentId": payment['debt_instalment_id'],
                                        "paidInstalmentSum": payment['residual'],
                                        "paidInterestSum": payment['interest']
                                    }
                                ]
                            }
                        ]
                    }
                    response = client.update_mateus_status(data)
                    if response:
                        has_paid = True
                    current_app.logger.info("Mateus Response")
                    current_app.logger.info(response)
                else:
                    has_paid = True

            else:
                has_canceled = True
            MateusPaymentRequest.update_from_dict(payment)
            result[VALID_DEBT_REG_ID[str(payment["kind_debt_reg_id"])]].append(payment)

        if has_canceled and not (has_paid and has_pending):
            group["status"] = 'Canceled'
        elif has_paid and not (has_pending and has_canceled):
            group["status"] = 'Paid'
        else:
            group["status"] = 'New'
        MateusPaymentGroup.update_from_dict(group)

        return result

    def update_group_by_payment(self, group_id):
        group = MateusPaymentGroup.find_by_id(group_id).to_json()
        has_pending = False
        has_paid = False
        has_canceled = False

        payment_requests = MateusPaymentRequest.find_by_mateus_payment_group(group_id)
        schema = MateusPaymentRequestSchema()
        payments = schema.dump(payment_requests, many=True)
        result = {
            "real_estate": [],
            "vehicle": [],
            "household_waste": [],
        }
        client = EFormIntegrationsService()

        for payment in payments:
            if payment["status"].capitalize() == "Pending":
                has_pending = True
            elif payment["status"].capitalize() == "Paid":
                current_date = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')
                data = {
                    "companyId": 999,
                    "operatorId": payment['person_identifier'],
                    "agentTransactionId": payment['rnu'],
                    "agentTransactionDate": current_date,
                    "municipalityId": payment['municipality_id'],
                    "subjectsInstalments": [
                        {
                            "taxSubjectId": int(group["tax_subject_id"]),
                            "instalments": [
                                {
                                    "debtInstalmentId": payment['debt_instalment_id'],
                                    "paidInstalmentSum": payment['interest'],
                                    "paidInterestSum": payment['residual']
                                }
                            ]
                        }
                    ]
                }
                response = client.update_mateus_status(data)
                current_app.logger.info(response)
                has_paid = True

            else:
                has_canceled = True
            MateusPaymentRequest.update_from_dict(payment)
            result[VALID_DEBT_REG_ID[str(payment["kind_debt_reg_id"])]].append(payment)

        if has_canceled and not (has_paid and has_pending):
            group["status"] = 'Canceled'
        elif has_paid and not (has_pending and has_canceled):
            group["status"] = 'Paid'
        else:
            group["status"] = 'New'
        MateusPaymentGroup.update_from_dict(group)

        return result

