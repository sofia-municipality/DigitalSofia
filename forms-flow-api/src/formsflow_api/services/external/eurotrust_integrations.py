import requests
from flask import current_app, url_for, request
from formsflow_api.exceptions import EurotrustException
from http import HTTPStatus


class EurotrustIntegrationsService:

    def __init__(self):
        current_app.logger.debug("eForm Service")
        current_app.logger.debug("Init")
        self.base_url = current_app.config.get("EFORM_INTEGRATIONS_URL")

    def check_document_status(self, transaction_id: str, group_signing: bool = False):
        url=f"{self.base_url}/integrations/evrotrust/document/status/"
        url = url + f"{transaction_id}/"
        url = url + str(group_signing)

        response = requests.get(url=url)
        
        current_app.logger.debug(response.status_code)

        if response.ok:
            current_app.logger.debug(response.json())
            return response.json()

        
        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(error_response.get("message"), error_response.get("status"), error_response.get("data"))

    def get_signed_file(self, transaction_id: str, group_signing: bool = False) -> dict:
        url = f"{self.base_url}/integrations/evrotrust/document/download/{transaction_id}/{str(group_signing)}"
        
        current_app.logger.debug(f"Getting signed file for transaction \"{transaction_id}\"")
        response = requests.get(url=url)

        if response.ok:
            return response.json()
        
        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(error_response.get("message"), error_response.get("status"), error_response.get("data"))
    
    def withdraw_document(self, thread_id: str,) -> dict:
        response = requests.post(
            url=f"{self.base_url}/integrations/evrotrust/document/withdraw",
            json={
                "threadID": thread_id
            }
        )

        if response.ok:
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(error_response.get("message"), error_response.get("status"), error_response.get("data"))

    def send_identification_request(self, user_identifier: str, date_expire:str) ->dict:
        current_app.logger.debug(self.base_url)
        url = f"{self.base_url}/integrations/evrotrust/document/doc/identification"
        data = {
            "document": {
                "dateExpire": date_expire
            },
            "includes": {
                "names": True,
                "latinNames": True,
                "phones": False,
                "emails": False,
                "address": True,
                "documentType": True,
                "documentNumber": True,
                "documentIssuerName": True,
                "documentValidDate": True,
                "documentIssueDate": True,
                "documentCountry": True,
                "identificationNumber": True,
                "gender": False,
                "nationality": False,
                "documentPicture": False,
                "documentSignature": False,
                "picFront": False,
                "picBack": False,
                "picIDCombined": False,
                "dateOfBirth": False,
                "placeOfBirth": False
            },
            "BIOrequired": 0,
            "user": {
                "identificationNumber": user_identifier,
                "country": "BG"
            }
        }

        response = requests.post(
            url=url,
            json=data
        )

        current_app.logger.debug(response.status_code)
        current_app.logger.debug(response.content)
        if response.ok:
            current_app.logger.debug(response.json())
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(error_response["message"], error_response["status"], error_response.get("data"))

    def sign_document(
            self, 
            tenant_key: str,
            dateExpire:str, 
            content:str,
            content_type:str,
            file_name: str, 
            user_identifier:str
        ):
        
        callback_url_base = current_app.config.get("FORMSFLOW_API_URL")

        response = requests.post(
            url=f"{self.base_url}/integrations/evrotrust/document/sign",
            json={
                "dateExpire": dateExpire,
                "userIdentifiers": [user_identifier],
                "documents": [
                    {
                        "content": content,
                        "contentType": content_type,
                        "fileName": file_name
                    }
                ],
                "urlCallback": f"{callback_url_base}/eurotrust/"
            }
        )

        current_app.logger.debug(response.status_code)
        if response.ok:
            current_app.logger.debug(response.json())
            current_app.logger.debug(f"{callback_url_base}/eurotrust/")
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(error_response.get("message"), error_response.get("status"), error_response.get("data"))
