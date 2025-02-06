import requests
import json
from tempfile import TemporaryDirectory

from flask import current_app

from formsflow_api.exceptions import EurotrustException


class EurotrustIntegrationsService:

    def __init__(self):
        current_app.logger.debug("eForm Service")
        current_app.logger.debug("Init")
        self.base_url = current_app.config.get("EFORM_INTEGRATIONS_URL")

    def check_document_status(self, transaction_id: str, group_signing: bool = False):
        url = f"{self.base_url}/integrations/evrotrust/document/status/"
        url = url + f"{transaction_id}/"
        url = url + str(group_signing)

        response = requests.get(url=url)

        current_app.logger.debug(response.status_code)

        if response.ok:
            current_app.logger.debug(response.json())
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def check_receipt_status(self, thread_id: str):
        url = f"{self.base_url}/integrations/evrotrust/delivery/receipts/status"

        post_body = {"threadIDs": [thread_id]}

        resp = requests.post(url, json=post_body)

        current_app.logger.debug(resp.status_code)

        if resp.ok:
            resp_data = resp.json()
            current_app.logger.debug(resp_data)
            return resp_data

        error_response = resp.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def download_receipts(self, transaction_id: str):
        url = f"{self.base_url}/integrations/evrotrust/delivery/receipts/download"

        post_body = {"transactionID": transaction_id}

        resp = requests.post(url, json=post_body)

        current_app.logger.debug(resp.status_code)

        if resp.ok:
            return resp.content

        error_response = resp.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def get_signed_file(self, transaction_id: str, group_signing: bool = False) -> dict:
        url = f"{self.base_url}/integrations/evrotrust/document/download/{transaction_id}/{str(group_signing)}"

        current_app.logger.debug(
            f'Getting signed file for transaction "{transaction_id}"'
        )
        response = requests.get(url=url)

        if response.ok:
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def withdraw_document(
        self,
        thread_id: str,
    ) -> dict:
        response = requests.post(
            url=f"{self.base_url}/integrations/evrotrust/document/withdraw",
            json={"threadID": thread_id},
        )

        if response.ok:
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def send_identification_request(
        self, user_identifier: str, date_expire: str, language: str
    ) -> dict:
        current_app.logger.debug(self.base_url)
        url = f"{self.base_url}/integrations/evrotrust/document/doc/identification"
        data = {
            "document": {"dateExpire": date_expire, "language": language},
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
                "placeOfBirth": False,
            },
            "BIOrequired": 0,
            "user": {"identificationNumber": user_identifier, "country": "BG"},
        }

        current_app.logger.debug(url)
        current_app.logger.debug(data)

        response = requests.post(url=url, json=data)

        current_app.logger.debug(response.status_code)
        current_app.logger.debug(response.content)
        if response.ok:
            current_app.logger.debug(response.json())
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response["message"],
            error_response["status"],
            error_response.get("data"),
        )

    def sign_document(
        self,
        tenant_key: str,
        dateExpire: str,
        content: str,
        content_type: str,
        file_name: str,
        user_identifier: str,
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
                        "fileName": file_name,
                    }
                ],
                "urlCallback": f"{callback_url_base}/eurotrust/",
            },
        )

        current_app.logger.debug(response.status_code)
        if response.ok:
            current_app.logger.debug(response.json())
            current_app.logger.debug(f"{callback_url_base}/eurotrust/")
            return response.json()

        error_response = response.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def send_file_for_delivery(
        self,
        document_base64: str,
        document_name: str,
        user_id_number: str,
        description: str,
        date_expire: str,
    ):
        url = f"{self.base_url}/integrations/evrotrust/document/delivery"
        current_app.logger.debug("Base url")
        current_app.logger.debug(self.base_url)

        post_body = {
            "document": {"description": description, "dateExpire": date_expire},
            "user": {"identificationNumber": user_id_number},
            "documentToDeliver": {
                "content": document_base64,
                "contentType": "application/pdf",
                "name": document_name,
            },
        }

        resp = requests.post(url, json=post_body)
        current_app.logger.debug(resp.status_code)
        current_app.logger.debug(resp.headers)

        if resp.ok:
            resp_data = resp.json()
            current_app.logger.debug(resp_data)
            return resp_data

        error_response = resp.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )

    def check_user_extented(self, user_identifier: str):
        url = f"{self.base_url}/integrations/evrotrust/user/check"
        body = {"identificationNumber": user_identifier}
        resp = requests.post(url, json=body)
        if resp.ok:
            resp_data = resp.json()
            current_app.logger.debug(resp_data)
            return resp_data

        error_response = resp.json()
        current_app.logger.debug(error_response)
        raise EurotrustException(
            error_response.get("message"),
            error_response.get("status"),
            error_response.get("data"),
        )
