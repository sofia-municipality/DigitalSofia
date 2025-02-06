from http import HTTPStatus
from formsflow_api.models import MateusPaymentGroup
from datetime import datetime
from flask import current_app

from formsflow_api.models.valid_debt_reg_id import VALID_DEBT_REG_ID
from formsflow_api.schemas import MateusPaymentRequestSchema
from formsflow_api.services.external import EFormIntegrationsService
import decimal


class ObligationService:
    @staticmethod
    def prepare_data(group: MateusPaymentGroup, payment_requests):
        current_date = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%S3Z')

        # Prepare the instalments
        instalments = []
        for payment in payment_requests:
            instalment = {
                "debtInstalmentId": payment.debt_instalment_id,
                "paidInstalmentSum": payment.residual,
                "paidInterestSum": payment.interest
            }
            instalments.append(instalment)

        return {
            "companyId": current_app.config.get("MATEUS_COMPANY_ID"),
            "operatorId": group.person_identifier,
            "agentTransactionId": group.payment_id, # payment_id created in the beginning of the payment
            "agentTransactionDate": current_date,
            "municipalityId": 1239,
            "subjectsInstalments": [
                {
                    "taxSubjectId": int(group.tax_subject_id),
                    "instalments": instalments
                }
            ]
        }

    # Handle paid status and update the status in mateus
    '''
        COMPLETED (отразено за платено в МДТ),
        При неуспешна транзакция, не се връща JSON, а се връща HTTP error, както следва:
        - Http status 400 и код EXCEPTION_DUPLICATE_TAXSUBJECT_ID (когато групирането по лице не е коректно);
        - Http status 400 и код EXCEPTION_DUPLICATE_DEBTINSTALMENT_ID (когато едно и също задължение е подадено повече от един път);
        - Http status 400 и код EXCEPTION_SUBJECT_NOT_FOUND_REQUEST (когато подадения идентификатор на лице не е валиден - taxsubjectid);
        - Http status 400 и код EXCEPTION_MORE_THAN_ONE_SUBJECT (когато групирането на задълженията не е коректно и е подадено повече от едно лице);
        - Http status 400 и код EXCEPTION_NOT_FOR_THIS_SUBJECT (групираните задължения не са за подаденото лице в taxsubjectid);
        - Http status 400 и код EXCEPTION_ISNTALMENTS_NOT_FOUND (при невалиден идентификатор на задължение/я);
        - Http status 400 и код INCORRECT_PAY_ORDER (не е спазена последователността за погасяване);
        - Http status 400 и код INVALID_DATA_FORMAT (формата на JSON не е валиден);
 
    '''

    @staticmethod
    def update_mateus_for_payments(group, payments, **kwargs):
        try:

            if "test" in kwargs:
                if kwargs["test"] == "success":
                    return {
                            'success': True,
                            'statusCode': 200,
                            'data': {
                                'transactionStatus': 'COMPLETED',
                                'payTransaction': 27652532,
                                'payDocuments': None
                            }
                        }
                elif kwargs["test"] == "400":
                    return {
                            'success': True,
                            'statusCode': 400,
                            'data': {
                                'transactionStatus': 'EXCEPTION_SUBJECT_NOT_FOUND_REQUEST',
                                'payTransaction': 27652532,
                                'payDocuments': None
                            }
                        }

            client = EFormIntegrationsService()
            # Prepare the data to send to mateus
            data = ObligationService.prepare_data(group, payments)
            
            # call mateus and update the status of the group payments
            response = client.update_mateus_status(data)

            current_app.logger.info("Mateus Response")
            current_app.logger.info(response)

            return response
        except Exception as ex: 
            current_app.logger.error(f"Error in update_mateus_for_payments: {ex}")
            return {
                "error": ex
            }

    # Update the group status by payment from ePayment external service
    @staticmethod
    def update_group_by_payment(group):
        result = {
            "real_estate": [],
            "vehicle": [],
            "household_waste": [],
        }

        current_app.logger.info("Update group by payment")
        current_app.logger.info(group)

        # Initialize the external client
        client = EFormIntegrationsService()

        current_app.logger.info("Get payment status from ePayment for group %s", group["id"])
        # Get the status of the group from the external service
        response = client.get_payment_status(group["e_payment_payment_id"])

        if response and response.get("status") is not None:
            payment_status = response["status"].capitalize()

            current_app.logger.info("ePayment payment status: %s", payment_status)

            if payment_status == "Inprocess":
                payment_status = "Pending"
            elif payment_status == "Suspended":
                payment_status = "Cancelled"

            group["status"] = payment_status

            # Update the status of the group in the database 
            current_app.logger.info("Update group status in database for group: %s with payment_id: %s", group["id"],
                                    group["payment_id"])
            MateusPaymentGroup.update_status(group)

            # Serialize the payments
            schema = MateusPaymentRequestSchema()
            payments = schema.dump(group["payments"], many=True)

            for payment in payments:
                payment["status"] = group["status"]
                result[VALID_DEBT_REG_ID[str(payment["kind_debt_reg_id"])]].append(payment)
        else:
            current_app.logger.error("No payment status found for group %s", group["id"])

        return result

    # Check the payment status by person identifier
    @staticmethod
    def verify_payment_status(person_identifier):
        # check for already initiated payment request in database
        # if there is a pending payment request, return an error
        # if there is paid payment request and the payment is not updated in mateus, return an error
        groups = MateusPaymentGroup.check_payment_status_by_person_id(person_identifier)

        current_app.logger.info(groups)

        if not groups:
            return {}, HTTPStatus.OK

        for group in groups:
            if group.status == "Paid" and not group.is_notified:
                return {
                    "key": "mateus_payment_not_updated",
                    "message": "You have obligations with an initiated payment that are still being processed. Please "
                               "try again later.",
                }, HTTPStatus.BAD_REQUEST

            return {
                "key": "pending_payment_request",
                "message": "You already have a registered payment request. Please complete the payment.",
                "accessCode": group.access_code,
            }, HTTPStatus.BAD_REQUEST

    @staticmethod
    def check_user_have_obligation_entities(user_identifier: str) -> bool:
        query = MateusPaymentGroup.find_all_by_personal_identifier_query(user_identifier)
        count = MateusPaymentGroup.select_count_by_query(query)
        return bool(count)
