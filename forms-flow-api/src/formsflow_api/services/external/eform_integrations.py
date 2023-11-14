import json
import requests
from flask import current_app, jsonify
from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.models import PaymentRequest

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
        # current_app.logger.debug(response.content)

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
                    message=error_response["message"]
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
                message=error_response["message"]
            )

    def sent_to_eDelivery(self, data):
        url = f"{self.base_url}/integrations/eDelivery/send-message-on-behalf-to-person"
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
                message=error_response["message"]
            )
