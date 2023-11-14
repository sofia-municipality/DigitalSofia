"""Helper module for PDF export."""
import json
import os
import urllib.parse
import uuid
from typing import Any, Tuple, Union

import requests
from http import HTTPStatus
from flask import current_app
from formsflow_api_utils.services import FormioService
from formsflow_api_utils.utils import HTTP_TIMEOUT
from formsflow_api_utils.utils.pdf import get_pdf_from_html, pdf_response
from nested_lookup import get_occurrences_and_values

from formsflow_documents.utils import DocUtils


class PDFService:
    """Helper class for pdf generation."""

    __slots__ = (
        "__is_form_adaptor",
        "__custom_submission_url",
        "__form_io_url",
        "__host_name",
        "__chrome_driver_path",
        "__api_url",
        "form_id",
        "submission_id",
        "formio",
        "draft_id",
        "language"
    )

    def __init__(self, form_id: str, submission_id='', draft_id='', language='bg') -> None:
        """
        Initializes PDFService.

        form_id: formid corresponding to the PDF
        submission_id: submissionid corresponding to the PDF.
        """
        self.__is_form_adaptor = current_app.config.get("CUSTOM_SUBMISSION_ENABLED")
        self.__custom_submission_url = current_app.config.get("CUSTOM_SUBMISSION_URL")
        self.__form_io_url = current_app.config.get("FORMIO_URL")
        self.__host_name = current_app.config.get("FORMSFLOW_DOC_API_URL")
        self.__chrome_driver_path = current_app.config.get("CHROME_DRIVER_PATH")
        self.__api_url = current_app.config.get("FORMSFLOW_API_URL")
        self.form_id = form_id
        self.submission_id = submission_id
        self.draft_id = draft_id
        self.formio = FormioService()
        self.language = language

    def __is_form_adapter(self) -> bool:
        """Returns whether th eapplication is using a form adaptor."""
        return self.__is_form_adaptor

    def __get_custom_submission_url(self) -> str:
        """Returns the custom submission url based on config."""
        return self.__custom_submission_url

    def __get_formio_url(self) -> str:
        """Returns the formio url based on config."""
        return self.__form_io_url

    def __get_formio_access_token(self) -> Any:
        """Returns formio access token."""
        return self.formio.get_formio_access_token()

    def __get_submission_data(self, token='') -> Any:
        """Returns the submission data from formio."""
        formio_get_payload = {"form_id": self.form_id, "sub_id": self.submission_id}
        return self.formio.get_submission(
            formio_get_payload, self.__get_formio_access_token()
        )

    def __get_form_data(self) -> Any:
        """Returns the form data from formio."""
        return self.formio.get_form(
            {"form_id": self.form_id}, self.__get_formio_access_token()
        )

    def __get_chrome_driver_path(self) -> str:
        """Returns the configured chrome driver path."""
        return self.__chrome_driver_path

    def __get_form_and_submission_urls(self, token: str) -> Tuple[str, str, str]:
        """Returns the appropriate form and submission url based on the config."""
        current_app.logger.debug("__get_form_and_submission_urls")
        form_io_url = self.__get_formio_url()
        if self.__is_form_adapter() or not self.draft_id == '':
            sub_url = self.__get_custom_submission_url()
            form_url = form_io_url + "/form/" + self.form_id
            if self.draft_id == '':
                submission_url = (
                        sub_url + "/form/" + self.form_id + "/submission/" + self.submission_id
                )
            else:
                submission_url = (
                        self.__api_url + "/draft/" + self.draft_id
                )
            auth_token = token
        else:
            form_url = (
                    form_io_url
                    + "/form/"
                    + self.form_id
                    + "/submission/"
                    + self.submission_id
            )
            submission_url = None
            auth_token = None
        current_app.logger.debug(f"Submission url - {submission_url}")
        return form_url, submission_url, auth_token

    def __get_template_params(self, token: str, tenant_key:str=None) -> dict:
        """Returns the jinja template parameters for pdf export with formio renderer."""
        form_io_url = self.__get_formio_url()
        current_app.logger.debug("__get_template_params")
        (form_url, submission_url, auth_token) = self.__get_form_and_submission_urls(
            token
        )
        return {
            "form": {
                "base_url": form_io_url,
                "project_url": form_io_url,
                "form_url": form_url,
                "token": self.__get_formio_access_token(),
                "submission_url": submission_url,
                "form_adapter": bool(self.__is_form_adapter() or not self.draft_id == ''),
                "auth_token": auth_token,
                "language": self.language,
                "translations": self.fetch_translations(auth_token, tenant_key),
                "data": self.fetch_data(auth_token, tenant_key)
            }
        }

    @staticmethod
    def __generate_template_name() -> str:
        """Generate unique template name."""
        return f"{str(uuid.uuid4())}.html"

    @staticmethod
    def __generate_template_variables_name() -> str:
        """Generate unique template variable name."""
        return f"{str(uuid.uuid4())}.json"

    @staticmethod
    def __get_template_path() -> str:
        """Returns the path to template directory."""
        path = os.path.dirname(__file__)
        path = path.replace("services", "templates")
        return path

    def __generate_pdf_file_name(self) -> str:
        """Generated the PDF file name."""
        return "Application_" + self.form_id + "_" + self.submission_id + "_export.pdf"

    def __get_render_url(
            self,
            template_name: Union[str, None] = None,
            template_variable_name: Union[str, None] = None,
            tenant_key: str = None
    ) -> str:
        if not self.submission_id == '':
            """Returns the render URL."""
            url = (
                    self.__host_name
                    + "/form/"
                    + self.form_id
                    + "/submission/"
                    + self.submission_id
                    + "/render"
            )
        else:
            url = (
                    self.__host_name
                    + "/form/"
                    + self.form_id
                    + "/draft/"
                    + self.draft_id
                    + "/render"
            )

        query_params = {}

        if template_name:
            query_params["template_name"] = {self.__url_encode(template_name)}

        if template_variable_name:
            query_params["template_variable_name"] = {self.__url_encode(template_variable_name)}
            
        if tenant_key:
            query_params["tenant_key"] = tenant_key
        
        query_params = urllib.parse.urlencode(query_params)
        if query_params:
            url += f"?{query_params}"

        current_app.logger.debug(f"Generated URL {url}")
        return url

    def __get_render_args(
            self, timezone: str, token: str, use_template: bool = False
    ) -> dict:
        """Returns PDF render arguments."""
        args = {
            "wait": "completed",
            "timezone": timezone,
            "auth_token": token,
        }
        if use_template and self.draft_id == '':
            del args["wait"]
        return args

    @staticmethod
    def __url_encode(payload: str) -> str:
        """Escapes url unsafe characters."""
        return urllib.parse.quote(payload)

    def __read_json(self, file_name: str) -> Any:
        """Reads the json file contents to a variable."""
        path = self.__get_template_path()
        with open(f"{path}/{file_name}", encoding="utf-8") as file:
            data = json.load(file)
            file.close()
            return data

    def get_render_status(
            self, 
            token: str, 
            tenant_key:str = None, 
            template_name: Union[str, None] = None
    ) -> int:
        """Returns the render status code."""
        headers = self.__generate_headers( token=token, tenant_key=tenant_key)

        res = requests.get(
            url=self.__get_render_url(template_name, tenant_key=tenant_key),
            headers=headers,
            timeout=HTTP_TIMEOUT,
        )
        return res.status_code

    def __get_formatted_data(self, form_data, submission_data):
        """Returns the presentable data from the submission data."""
        submission_data_formatted = {"form": {"form": form_data, "data": {}}}
        for key, value in submission_data["data"].items():
            key_formatted = get_occurrences_and_values([form_data], value=key)[key][
                "values"
            ][0].get("label")
            if key_formatted is None:
                continue
            value_formatted = value
            if value and DocUtils.is_camel_case(value):
                value_formatted = get_occurrences_and_values([form_data], value=value)[
                    value
                ]["values"]
                value_formatted = (
                    value_formatted[0].get("label", "")
                    if len(value_formatted) > 0
                    else value
                )
            submission_data_formatted["form"]["data"][key] = {
                "label": key_formatted,
                "value": value_formatted,
            }
        return submission_data_formatted

    def get_render_data(
            self, 
            use_template: bool, 
            template_variable_name: Union[str, None], 
            token: str, 
            tenant_key: str = None
    ) -> Any:
        """
        Returns the render data for the pdf template.

        use_template: boolean, whether to use a template for generating pdf.
        template_variable_name: template variable file name for requests with
        template variable payload.
        token: token for the template parameters when using formio renderer.
        Raw data will be passed to the template if the export is using any
        form of template. Else default formio renderer will be used where
        only the formio render parameters will be passed to the template.
        """
        if not use_template:
            return self.__get_template_params(token=token, tenant_key=tenant_key)
        if template_variable_name:
            return self.__read_json(template_variable_name)
        submission_data = self.__get_submission_data(token)
        form_data = self.__get_form_data()
        return self.__get_formatted_data(form_data, submission_data)

    def __write_to_file(
            self, template_name: str, content: Union[str, dict], is_json: bool = False
    ) -> None:
        """Write data to file."""
        path = self.__get_template_path()
        with open(f"{path}/{template_name}", "w", encoding="utf-8") as file:
            # disabling pylint warning since it is a function call
            json.dump(  # pylint: disable=expression-not-assigned
                content, file
            ) if is_json else file.write(content)
            file.close()

    def create_template(
            self, template: str, template_var: dict = None
    ) -> Tuple[str, Union[str, None]]:
        """
        Creates a temporary template in the template directory.

        template: base64 encoded template, supported template formats: jinja
        """
        template_name = self.__generate_template_name()
        template_var_name = (
            self.__generate_template_variables_name() if template_var else None
        )
        decoded_template = DocUtils.b64decode(template)
        self.__write_to_file(template_name, content=decoded_template)
        if template_var:
            self.__write_to_file(template_var_name, content=template_var, is_json=True)
        return (template_name, template_var_name)

    def search_template(self, file_name: str) -> bool:
        """
        Check if the given file exists in the template directory.

        file_name: name of the file to check with extension.
        """
        path = self.__get_template_path()
        return os.path.isfile(f"{path}/{file_name}")

    def delete_template(self, file_name: str) -> None:
        """
        Delete the given file from template directory.

        file_name: name of the file with extension.
        """
        try:
            path = self.__get_template_path()
            os.remove(f"{path}/{file_name}")
        except BaseException as err:  # pylint: disable=broad-except
            current_app.logger.error(err)
            current_app.logger.error(
                f"Failed to delete template:- {file_name} not found"
            )

    def generate_pdf(
            self,
            timezone: str,
            token: str,
            tenant_key: str = None,
            template_name: Union[str, None] = None,
            template_variable_name: str = None,
    ) -> Any:
        """Generates PDF from HTML."""
        pdf = get_pdf_from_html(
            self.__get_render_url(template_name, template_variable_name, tenant_key=tenant_key),
            self.__get_chrome_driver_path(),
            args=self.__get_render_args(timezone, token, bool(template_name)),
        )
        return pdf_response(pdf, self.__generate_pdf_file_name()) if pdf else False

    def fetch_translations(self, token, tenant_key: str = None):
        headers = self.__generate_headers(token, tenant_key)
        current_app.logger.debug("Fetch translations")
        current_app.logger.debug(self.__api_url + '/translations/languages/' + self.language)
        current_app.logger.debug(headers)
        return requests.get(self.__api_url + '/translations/languages/' + self.language, headers=headers).json()

    def fetch_data(self, token,tenant_key: str = None):
        headers = self.__generate_headers(token, tenant_key)
        current_app.logger.debug("Fetch data")
        current_app.logger.debug(self.__api_url + '/draft/' + self.draft_id)
        current_app.logger.debug(headers)
        return requests.get(self.__api_url + '/draft/' + self.draft_id, headers=headers).json()

    def __generate_headers(self, token: str = None, tenant_key: str = None):
        headers = {}
        if token:
            headers["Authorization"] = token
        
        if tenant_key:
            headers["X-Tenant-Key"] = tenant_key

        return headers
