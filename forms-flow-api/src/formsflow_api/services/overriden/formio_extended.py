import requests
from flask import current_app
from http import HTTPStatus
import json
from formsflow_api_utils.services.external import FormioService
from formsflow_api_utils.exceptions import BusinessException


class FormioServiceExtended(FormioService):

    def get_forms_starting_with_path(self, form_path: str, formio_token:str):
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        url = (
                f"{self.base_url}/form?" +
                f"name__regex=/^{form_path}/i" +
                "&select=title,name,path" +
                "&limit=1"
        )

        current_app.logger.info(url)
        response = requests.get(
            url, 
            # params={
            #     ,,
            #     "limit": "1"
            # },
            headers=headers
        )
        current_app.logger.debug(response.url)
        if response.ok:
            return response.json()
        
        current_app.logger.debug(response.content)
        raise BusinessException(response.content, response.status_code)


    def get_all_submissions(self, form_path: str, formio_token, options=''):
        """Get request to formio API to get submission details."""
        try:
            form_id = self.fetch_form_id_by_path(form_path, formio_token)
        except:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            return response, status
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        skip = 0
        url = (
                f"{self.base_url}/form/" + form_id + "/submission?select=data&limit=100&skip="
        )
        current_app.logger.info(url + str(skip) + "&" + options)
        full_response = []
        while True:
            response = requests.get(url + str(skip) + "&" + options, headers=headers)
            skip += 100
            if len(response.json()) > 0:
                full_response += response.json()
            else:
                break
        return full_response
    
    def get_submissions(self, 
                        form_path:str, 
                        formio_token:str,
                        limit:int = 10,
                        skip:int = 0,
                        status:list = None,
                        params: list = [],
                        select: list = [],
                        created_after: str = None
                        ):
        try:
            form_id = self.fetch_form_id_by_path(form_path, formio_token)
        except:
            response, status = {
                                   "type": "Formio Path Not Found",
                                   "message": f"Formio Path {form_path} not found",
                               }, HTTPStatus.NOT_FOUND
            return response, status
        
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        url = f"{self.base_url}/form/{form_id}/submission/"
        base_params = {
            'limit': limit,
            'skip': skip,
        }
        params = params | base_params

        if status:
            params['data.status__in'] = ','.join(status)

        if select:
            params['select'] = ','.join(map(str,select))

        if created_after:
            params['created__gte'] = created_after

        params['sort'] = '-created'

        current_app.logger.debug("FormioExtended@get_submissions")
        current_app.logger.debug(url)
        current_app.logger.debug(params)
        response = requests.get(
            url, 
            params=params,
            headers=headers
        )

        if response.ok:
            return response.json(), response.status_code
        
        return response.content, response.status_code


    def fetch_form_id_by_path(self, form_path: str, formio_token):
        """Get request to formio API to get submission details."""
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        url = (
                f"{self.base_url}/" + form_path + "?select=_id"
        )
        current_app.logger.debug(f"Fetching form id by path - {form_path}")
        current_app.logger.debug(f"URL - {url}")
        form = requests.get(url, headers=headers).json()

        if '_id' in form:
            return form['_id']

        response, status = {
                               "type": "Bad request error",
                               "message": "Invalid submission request passed",
                           }, HTTPStatus.BAD_REQUEST
        return response, status

    def find_and_fetch_submission(self, form_path: str, formio_token, options=''):
        form_id = self.fetch_form_id_by_path(form_path, formio_token)
        submissions = self.get_all_submissions(form_path, formio_token, options)
        if len(submissions) >= 1:
            return self.get_submission({'sub_id': submissions[-1]['_id'], 'form_id': form_id}, formio_token)
        else:
            return []

    def update_resource_formio_status(
            self, 
            formio_form_path:str,
            formio_resource_id:str = None, 
            formio_status:str = None, 
            transaction_id:str = None, 
            thread_id:str = None
        ):
        formio_token = self.get_formio_access_token()
        
        file_form_id = self.fetch_form_id_by_path(form_path=formio_form_path, formio_token=formio_token)
        updated_values_list = [
                {
                    "op": "replace",
                    "path": "/data/status",
                    "value": formio_status
                }
            ]

        if transaction_id:
            updated_values_list.append(
                {
                    "op": "replace",
                    "path": "/data/evrotrustTransactionId",
                    "value": transaction_id
                }
            )

        if thread_id:
            updated_values_list.append(
                {
                    "op": "replace",
                    "path": "/data/evrotrustThreadId",
                    "value": thread_id
                }
            )

        return self.partial_update_application(
            file_form_id,
            formio_resource_id,
            formio_token,
            data=updated_values_list
        )
    
    def generate_rfc6902_object(self, path:str, value, operation: str = "add"):
        ### https://datatracker.ietf.org/doc/html/rfc6902
        return {
            "op": operation,
            "path": path,
            "value": value
        }

    def delete_submission_formio(self, form_path, formio_submission_id):
        formio_token = self.get_formio_access_token()
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        url = (
            f"{self.base_url}/{form_path}/submission/{formio_submission_id}"
        )
        
        response = requests.delete(url, headers=headers)
        current_app.logger.debug(f"Response - {response.url}")
        if response.ok:
            return response.json()
        elif response.status_code == 404:
            return 
        
        response, status = {
                               "type": "Bad request error",
                               "message": response.content,
                           }, HTTPStatus.BAD_REQUEST
        return response, status

    def update_formio_resource(self, form_id:str, resource_id:str, data:list):
        formio_token = self.get_formio_access_token()

        return self.partial_update_application(
            form_id,
            resource_id,
            formio_token,
            data
        )

    def update_resource_formio_file(
        self, 
        form_path, 
        formio_resource_id, 
        type, 
        name, 
        content, 
        signature_source = None
    ):
        formio_token = self.get_formio_access_token()
        formio_form_id = self.fetch_form_id_by_path(form_path=form_path, formio_token=formio_token)

        typed_content = f"data:{type};base64," + content

        data = [
            {
                    "op": "replace",
                    "path": "/data/file",
                    "value": [
                        {
                            "storage": "base64",
                            "type": type,
                            "name": name,
                            "originalName": name,
                            "url": typed_content,
                            "size": (len(typed_content) * 3) / 4 - typed_content.count('=', -2)
                        }
                    ]
            }
        ]

        if signature_source:
            data.append(
                {
                    "op": "add",
                    "path": "/data/signatureSource",
                    "value": signature_source
                }
            )


        return self.partial_update_application(
            formio_form_id,
            formio_resource_id,
            formio_token,
            data
        )
    
    def check_if_submission_exists(self, form_path: str, data: dict):
        formio_token = self.get_formio_acess_token()
        file_form_id = self.fetch_form_id_by_path(form_path=form_path, formio_token=formio_token)

        url = (
            f"{self.base_url}/{form_path}/exists"
        )

        for key in data.keys():
            data[f'data.{key}'] = data.pop(key)
            
        response = requests.get(url, params=data)

        if response.ok:
            return response.json()
        
        current_app.logger.debug(response.content)
        raise BusinessException(response.content, HTTPStatus.BAD_REQUEST)

    def debug_get_submission(self, data, formio_token):
        """Get request to formio API to get submission details."""
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        url = (
            f"{self.base_url}/form/" + data["form_id"] + "/submission/" + data["sub_id"]
        )
        response = requests.get(url, headers=headers, data=json.dumps(data))
        if response.ok:
            return response.json()
        current_app.logger.debug(response.content)
        raise BusinessException(response.content, response.status_code)
    
    def get_user_id_by_email(self, email, formio_token):
        headers = {"Content-Type": "application/json", "x-jwt-token": formio_token}
        url = f"{self.base_url}/user/exists"
        payload = {
            "data.email": email
        }

        response = requests.get(url, headers=headers, params=payload)
        current_app.logger.debug(response.url)

        if response.ok:
            return response.json().get("_id")

        current_app.logger.debug(response.content)
        raise BusinessException(response.content, HTTPStatus.BAD_REQUEST)

    def get_user_submissions(self):
        url = f"{self.base_url}/user/submission"
        current_app.logger.info("Fetching user resource ids...")
        response = requests.get(url)
        if response.ok:
            current_app.logger.info("User resource ids collected successfully.")
            return response.json()
        current_app.logger.error("Failed to fetch user resource ids!")
        raise BusinessException(response.content, HTTPStatus.SERVICE_UNAVAILABLE)