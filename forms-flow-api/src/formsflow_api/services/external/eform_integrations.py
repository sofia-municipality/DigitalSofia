import json
from datetime import datetime
from http import HTTPStatus
from math import fsum
from random import randrange
import re
import base64
import pytz
import requests
from flask import current_app, jsonify
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import UserContext

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.models import PaymentRequest, MateusPaymentRequest
from formsflow_api.models.region import ADDITIONAL_REGION_DATA

VALID_ERRORS_BY_METHOD = {
    'get_obligations': [
        {
            'key': 'message',
            'value': 'Cannot found subject by passed parameters!',
            'return': {
                "taxSubject": {},
                "obligations": [],
                "hasMore": False
            }
        }
    ]
}


class EFormIntegrationsService:

    def __init__(self):
        current_app.logger.debug("eForm Service")
        current_app.logger.debug("Init")
        self.base_url = current_app.config.get("EFORM_INTEGRATIONS_URL")

    def generateOperationObject(self, operation: str, type: str, xmlns: str, parameters: list):
        return {
            "operation": operation,
            "argument": {
                "type": type,
                "xmlns": xmlns,
                "parameters": parameters
            }
        }

    def generateOperationParameter(self, name: str, value: str, type: str):
        return {
            "IdentifierType": {
                "parameterStringValue": name,
                "parameterType": type
            },
            "Identifier": {
                "parameterStringValue": value,
                "parameterType": type
            }
        }

    def get_person_data(self, personal_identifier, identity_document_number):
        current_app.logger.debug("eForm Service")
        current_app.logger.debug(f"{self.base_url}/integrations/regix/grao/person-data-search")
        current_app.logger.debug(personal_identifier)
        current_app.logger.debug(identity_document_number)
        data = self.generateOperationObject(
            operation="TechnoLogica.RegiX.MVRBDSAdapter.APIService.IMVRBDSAPI.GetPersonalIdentityV3",
            type="PersonalIdentityInfoRequest",
            xmlns="http://egov.bg/RegiX/MVR/BDS/PersonalIdentityInfoRequest",
            parameters=[
                self.generateOperationParameter("IdentityDocumentNumber", identity_document_number, "STRING"),
                self.generateOperationParameter("EGN", personal_identifier, "STRING")
            ]
        )

        response = requests.post(
            url=f"{self.base_url}/integrations/regix/search",
            json=data
        )

        current_app.logger.debug(response.status_code)

        ## An error has occurred within the integration error
        if response.status_code == 204:
            current_app.logger.error(response.status_code)
            current_app.logger.error(response.text)
            current_app.logger.error(response.url)
            raise EFormIntegrationException(
                error_code=response.status_code,
                message=f"EForm Integration error when getting person-data-search for EGN:{personal_identifier} and IDN:{identity_document_number}"
            )

        return response.json()["Response"]

    def get_obligations(self, personal_identifier, limit=None):
        url = f"{self.base_url}/integrations/AgentWS/obligations?idn={personal_identifier}"
        if limit:
            url += f"&limit={limit}"

        response = requests.get(
            url=url,
        )

        ## An error has occurred within the integration error
        if response.status_code == 200:
            return response.json()
        else:

            is_valid_error, response = self.check_is_valid_error('get_obligations', response)
            if not is_valid_error:
                return response
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"]
            )

    def check_is_valid_error(self, source, response):
        try:
            # get data
            data = response.json()
            data = data.get('data')
            data = json.loads(data)
            method_errors = VALID_ERRORS_BY_METHOD.get(source)

            current_app.logger.debug(f"Checking for expected errors for {source}")
            for item in method_errors:
                key_to_check = item.get('key')
                value = item.get('value')
                current_app.logger.debug("Data get key")
                current_app.logger.debug(data.get(key_to_check))

                if value is not None and value == data.get(key_to_check):
                    return False, item.get('return')

            return True, response
        except Exception as err:
            current_app.logger.error(str(err))
            current_app.logger.error(response.content)
            return True, response

    def search(self, data):
        current_app.logger.info(data)
        url = f"{self.base_url}/integrations/regix/search"
        response = requests.post(
            url=url,
            data=json.dumps(data)
        )
        if response.ok:
            return response.json()
        else:
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )

    def create_payment(self, application_id, data):

        payment_request = PaymentRequest.get_by_application_id(application_id)
        if payment_request:
            return payment_request.to_json()
        else:
            region_index = list(filter(
                lambda i: ADDITIONAL_REGION_DATA[i]['code'] == data['paymentRequest']['paymentData'][
                    'administrativeServiceSupplierUri'], ADDITIONAL_REGION_DATA))
            if len(region_index):
                region = ADDITIONAL_REGION_DATA[region_index[0]]
                data['eserviceClientId'] = region['eserviceClientId']

            ### Set callbackurl
            if "paymentRequest" in data and "paymentData" in data["paymentRequest"]:
                callback_url = current_app.config.get("FORMSFLOW_API_URL")
                current_app.logger.debug(f"Callback Url - {callback_url}")
                data["paymentRequest"]["paymentData"]["administrativeServiceNotificationURL"] = f"{callback_url}/payment/payment-status-callback"


            url = f"{self.base_url}/integrations/ePayment/register-payment-extended"
            response = requests.post(
                url=url,
                data=json.dumps(data)
            )
            if response.ok:
                result = response.json()
                payment_dict = {
                    "payment_id": result["paymentId"],
                    "application_id": application_id,
                    "access_code": result["accessCode"],
                }
                current_app.logger.info(payment_dict)
                PaymentRequest.create_from_dict(payment_dict)
                return result
            else:
                error_response = response.json()
                current_app.logger.info(error_response)
                raise EFormIntegrationException(
                    error_code=error_response["status"],
                    message=error_response["message"],
                    data=error_response["data"]
                )

    def get_payment_status(self, payment_id):
        url = f"{self.base_url}/integrations/ePayment/payment-status?paymentId={payment_id}"
        response = requests.get(
            url=url
        )
        if response.ok:
            return response.json()
        else:
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )

    def sent_to_eDelivery(self, data):

        url = f"{self.base_url}/integrations/eDelivery/send-message-on-behalf"
        response = requests.post(
            url=url,
            data=json.dumps(data)
        )
        if response.ok:
            return response.json()
        else:
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )

    def create_payment_from_obligation(self, group, obligation, user: UserContext):
        reason = str(obligation["taxPeriodYear"]) + "-" + str(obligation["instNo"]) + "-" + str(
            obligation["partidaNo"])
        payment_request_id = str(obligation["kindDebtRegId"]) + "-" + reason + str(randrange(100000,999999))
        region_index = list(filter(lambda i: ADDITIONAL_REGION_DATA[i]['mateus_code'] == obligation['municipalityId'],
                                   ADDITIONAL_REGION_DATA))
        personIdentifier = user.token_info["personIdentifier"].split("-")[1]
        if len(region_index):
            region = ADDITIONAL_REGION_DATA[region_index[0]]
            eserviceClientId = region['mateus_eserviceClientId']
        else:
            raise BusinessException(
                "Invalid Municipality", HTTPStatus.BAD_REQUEST
            )
        current_date = datetime.utcnow().replace(tzinfo=pytz.utc)
        current_date_formatted = datetime.isoformat(current_date)
        expiration_date = datetime.isoformat(current_date.replace(hour=int(current_date.hour) + 1))
        service_notification_url = f"{current_app.config.get('FORMSFLOW_API_URL')}/payment/payment-status-callback?message=PaymentStatusMessage&fieldId=ePaymentId"

        data = {
            "paymentRequest": {
                "actors": [{
                    "type": "PERSON",
                    "uid": {
                        "type": "EGN",
                        "value": personIdentifier
                    },
                    "name": user.token_info["name"],
                    "participantType": "APPLICANT"
                }],
                "paymentData": {
                    "paymentId": payment_request_id,
                    "currency": "BGN",
                    "amount": round(fsum([obligation["residual"], obligation["interest"]]), 2),
                    "referenceNumber": payment_request_id,
                    "referenceType": "9",
                    "referenceDate": current_date_formatted,
                    "expirationDate": expiration_date,
                    "reason": reason,
                    "additionalInformation": json.dumps({
                        "partidaNo": obligation["partidaNo"],
                        "propertyAddress": obligation["propertyAddress"],
                        "kindDebtRegName": obligation["kindDebtRegName"],
                        "taxPeriodYear": obligation["taxPeriodYear"],
                        "instNo": obligation["instNo"],
                        "debtInstalmentId": obligation["debtInstalmentId"],
                        "paidInstalmentSum": obligation["residual"],
                        "paidInterestSum": obligation["interest"],
                        "taxSubjectId": group["tax_subject_id"],
                        "regionClientId": eserviceClientId,
                        "regionName": obligation["municipalityName"],
                        "registerNo": obligation["registerNo"]
                    }),
                    "administrativeServiceUri": 2410,
                    "administrativeServiceSupplierUri": obligation["municipalityId"],
                    "administrativeServiceNotificationURL": service_notification_url,
                    "obligationType": obligation["kindDebtRegId"]
                }
            },
            "eserviceClientId": eserviceClientId
        }
        current_app.logger.info(data)
        url = f"{self.base_url}/integrations/ePayment/register-payment-extended"
        response = requests.post(
            url=url,
            data=json.dumps(data)
        )
        if response.ok:
            result = response.json()
            if obligation["propertyAddress"] != " ":
                additional_data = obligation["propertyAddress"]
            else:
                additional_data = obligation["registerNo"]
            payment_dict = {
                "payment_id": result["paymentId"],
                "payment_request_id": payment_request_id,
                "reason": reason,
                "access_code": result["accessCode"],
                "person_identifier": personIdentifier,
                "status": "Pending",
                "group_id": group["id"],
                "amount": float(data["paymentRequest"]["paymentData"]["amount"]),
                "tax_period_year": int(obligation["taxPeriodYear"]),
                "partida_no": obligation["partidaNo"],
                "kind_debt_reg_id": int(obligation["kindDebtRegId"]),
                "pay_order": int(obligation["payOrder"]),
                "additional_data": additional_data,
                "rnu": obligation["rnu"],
                "municipalityId": obligation["municipalityId"],
                "residual": obligation["residual"],
                "interest": obligation["interest"],
                "debtInstalmentId": obligation["debtInstalmentId"],

            }

            current_app.logger.info(payment_dict)
            MateusPaymentRequest.create_from_dict(payment_dict)
            return result
        else:
            error_response = response.json()
            current_app.logger.info(error_response)
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )

    def eDelivery_search_profile(self, identifier: str, targetGroupId: int):
        url = f"{self.base_url}/integrations/eDelivery/search-profile?identifier={identifier}&targetGroupId={targetGroupId}"
        response = requests.get(
            url=url,
        )
        if response.ok:
            return response.json()
        else:
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )

    def eDelivery_upload_blob(self, representedPersonID: str, file):
        url = f"{self.base_url}/integrations/eDelivery/upload/obo/blobs?type=Storage"
        response = requests.post(
            url=url,
            files=file,
            headers={
                "representedPersonID": representedPersonID
            }
        )
        if response.ok:
            return response.json()
        else:
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )
    def eDelivery_upload_blob_from_base64(self, file_path, token):

        file = requests.get(url=file_path, headers={
            "Authorization": token,
            "User-Agent": "curl/7.61.0"
        }).json()

        if file["data"] is None or len(file["data"]["file"]) < 1:
            raise BusinessException(
                error_code=HTTPStatus.BAD_REQUEST,
                message="No file found.",
            )

        # Upload file to eDelivery
        files = re.split(";|,", file["data"]["file"][0]["url"])
        image_binary = base64.b64decode(files[2])
        current_app.logger.info(file["data"]["file"][0]["name"])
        file_response = self.eDelivery_upload_blob("000696327000001",
                                                          {'file': (file["data"]["file"][0]["name"],
                                                                    image_binary)})
        return file_response["blobId"]
    def update_mateus_status(self, data):
        current_app.logger.info(data)
        url = f"{self.base_url}/integrations/AgentWS/payment/pay"
        response = requests.post(
            url=url,
            data=json.dumps(data)
        )
        if response.ok:
            return response.json()
        else:
            return False