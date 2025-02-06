import json
import decimal
from datetime import datetime
from http import HTTPStatus
from math import fsum
from enum import Enum
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
from formsflow_api.models.mateus_payment_group import MateusPaymentGroup
from formsflow_api.models.region import Region
from formsflow_api.transformers import EFormIntegrationsTransformer
from formsflow_api.schemas import RegionSchema

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


class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            return str(o)
        return super().default(o)


class EDeliveryUploadType(Enum):
    ON_BEHALF = "upload/obo/blobs"
    COMMON = "upload/blobs"


class EFormIntegrationsService:
    transformer: EFormIntegrationsTransformer

    def __init__(self):
        current_app.logger.debug("eForm Service")
        current_app.logger.debug("Init")
        self.base_url = current_app.config.get("EFORM_INTEGRATIONS_URL")
        self.transformer = EFormIntegrationsTransformer()
        self.eDelivery_file_upload_type = current_app.config.get("EDELIVERY_FILE_UPLOAD_ADDRESS")
        self.eDelivery_profile_id = current_app.config.get("EDELIVERY_FILE_UPLOAD_PROFILE_ID")

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

        current_app.logger.debug(f"Response status code: {response.status_code}")
        current_app.logger.debug(f"Response body: {response.json()}")

        # An error has occurred within the integration error
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

            current_app.logger.warning(f"Data:: {data}")

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
            # region_index = list(filter(
            #     lambda i: ADDITIONAL_REGION_DATA[i]['code'] == data['paymentRequest']['paymentData'][
            #         'administrativeServiceSupplierUri'], ADDITIONAL_REGION_DATA))
            # if len(region_index):
            #     region = ADDITIONAL_REGION_DATA[region_index[0]]
            #     data['eserviceClientId'] = region['eserviceClientId']

            region_entity = Region.get_by_id(data['paymentRequest']['paymentData']['administrativeServiceSupplierUri'])

            if region_entity:
                region_schema = RegionSchema()
                region = region_schema.dump(region_entity, many=False)
                current_app.logger.info("Region data: %s", region)
                data['eserviceClientId'] = region.get("client_id")

            # Set callback url
            if "paymentRequest" in data and "paymentData" in data["paymentRequest"]:
                callback_url = current_app.config.get("FORMSFLOW_API_URL")
                current_app.logger.debug(f"Callback Url - {callback_url}")
                data["paymentRequest"]["paymentData"][
                    "administrativeServiceNotificationURL"] = f"{callback_url}/payment/payment-status-callback"

            url = f"{self.base_url}/integrations/ePayment/register-payment-extended"
            current_app.logger.info("Request to ePayment for region:: %s", data)
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

        current_app.logger.info("Request to ePayment")
        current_app.logger.info(url)

        response = requests.get(
            url=url
        )

        current_app.logger.info("Response from ePayment")

        if response.ok:
            response_data = response.json()
            current_app.logger.info("Response JSON data from ePayment: %s", response_data)
            return response.json()
        else:
            error_response = response.json()
            raise EFormIntegrationException(
                error_code=error_response["status"],
                message=error_response["message"],
                data=error_response["data"]
            )

    def sent_to_eDelivery(self, recipient_profile_ids, subject, file_ids):
        url = f"{self.base_url}/integrations/eDelivery/send-message"

        # Prepare data to be sent to eDelivery
        data = {
            "recipientProfileIds": recipient_profile_ids,
            "subject": subject,
            "templateId": "1",
            "fields": {
                "e2135802-5e34-4c60-b36e-c86d910a571a": file_ids
            }
        }

        # Check if we are using onBehalf upload mechanism
        if self.eDelivery_file_upload_type == EDeliveryUploadType.ON_BEHALF.value:
            url = f"{self.base_url}/integrations/eDelivery/send-message-on-behalf"
            data["senderProfileId"] = self.eDelivery_profile_id

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

    def create_payment_from_obligations(self, transformed_obligations, amount, reason, group, payment_id,
                                        person_identifier, user: UserContext):
        user_name = user.token_info["name"]

        # prepare data for payment request
        data = self.transformer.payment_request(payment_id, amount, reason, person_identifier, user_name)

        current_app.logger.info('Sending payment request to ePayment')
        current_app.logger.info(data)

        # create payment request
        url = f"{self.base_url}/integrations/ePayment/register-payment-extended"
        response = requests.post(
            url=url,
            data=json.dumps(data.get("request"))
        )

        # if payment request is created successfully then store the obligations in the database
        if response.ok:
            current_app.logger.info('Payment request created successfully')
            result = response.json()

            group = {
                "id": group["id"],
                "access_code": result["accessCode"],
                "e_payment_payment_id": result["paymentId"],
                "status": "Pending"
            }

            current_app.logger.info('Updating payment group in the database')
            MateusPaymentGroup.update_from_dict(group)
            current_app.logger.info('Storing obligations in the database')
            current_app.logger.info(transformed_obligations)
            MateusPaymentRequest.bulk_insert(transformed_obligations)

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

    def eDelivery_file_upload(self, file):
        headers = {}
        url = f"{self.base_url}/integrations/eDelivery/{self.eDelivery_file_upload_type}"

        # Check if we are using onBehalf upload mechanism
        # Set representedPersonID for eDelivery if we are using onBehalf upload mechanism
        if self.eDelivery_file_upload_type == EDeliveryUploadType.ON_BEHALF.value:
            headers["representedPersonID"] = self.eDelivery_profile_id

        current_app.logger.debug(f"Uploading file to eDelivery: {url}")
        current_app.logger.debug(f"Headers: {headers}")

        response = requests.post(
            url=url,
            files=file,
            headers=headers
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
        file_response = self.eDelivery_file_upload({'file': (file["data"]["file"][0]["name"],
                                                             image_binary)})
        return file_response["blobId"]

    def update_mateus_status(self, data):
        current_app.logger.info(data)

        url = f"{self.base_url}/integrations/AgentWS/payment/pay"

        response = requests.post(
            url=url,
            data=json.dumps(data, cls=DecimalEncoder)
        )

        current_app.logger.info(response.json())

        if response.ok:
            return {
                "success": response.ok,
                "statusCode": response.status_code,
                "data": response.json()
            }
        else:
            return {
                "success": response.ok,
                "statusCode": response.status_code,
                "data": response.json()
            }
