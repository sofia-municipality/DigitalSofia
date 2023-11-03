import requests
from flask import current_app
from formsflow_api.exceptions import EurotrustException, KEPException

from formsflow_api.schemas.kep_signature import KEPSignatureRequest


class KEPSignatureData(object):
    pass


class SignatureServicesIntegrationService:
    def __init__(self):
        self.base_url = current_app.config.get("SIGN_SERVICE_API_URL")

    def get_document_data(self, data: KEPSignatureRequest):
        url = f"{self.base_url}/signature/document/data"
        response = requests.post(
            url=url,
            json=data
        )
        if response.ok:
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise KEPException(error_response["message"], error_response["status"])

    def sign_document(self, data: KEPSignatureRequest):
        url = f"{self.base_url}/signature/document/sign"
        response = requests.post(
            url=url,
            json=data
        )
        if response.ok:
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise KEPException(error_response["message"], error_response["status"])
