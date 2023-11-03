"""API endpoints for managing languages API resource."""

from http import HTTPStatus

from flask_restx import Namespace, Resource
from formsflow_api_utils.utils import cors_preflight, profiletime, auth

from flask import current_app, request
from formsflow_api.schemas import TenantSchema

from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api_utils.utils.user_context import UserContext, user_context

API = Namespace("Translations", description="Translations")


@cors_preflight("GET,OPTIONS")
@API.route("/languages/<string:language>", methods=["GET", "OPTIONS"])
class TranslationsResource(Resource):
    """Resource for getting all translations for a given languages."""

    @staticmethod
    @profiletime
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get(language: str):
        try:
            current_app.logger.debug("1. Getting user info")
            headers_data = TenantSchema().load(dict(request.headers))
            tenant_key = headers_data.get("tenant_key")
            formio_service = FormioServiceExtended()
            formio_token = formio_service.get_formio_access_token()

            current_app.logger.debug(f"2. Trying to get {tenant_key}-language submissions")
            languages_resource_response = formio_service.get_all_submissions(
                f"{tenant_key}-languages", 
                formio_token, 
                'data.language=' + language.upper()
            )
            current_app.logger.warning(languages_resource_response)

            current_app.logger.debug(f"3. Trying to get {tenant_key}-language-translations submissions")
            translation_response = formio_service.get_all_submissions(
                f"{tenant_key}-language-translations", formio_token, (
                    'data.language=' + languages_resource_response[0]['data']['languageLong'] +
                    '&data.status=public'
                )
            )
            response = []
            for trans in translation_response:
                response.append({trans['data']['key']: trans['data']['translation']})

            return (
                response,
                HTTPStatus.OK,
            )
        except Exception as err:
            current_app.logger.error(err)
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            return response, status

@cors_preflight("GET,OPTIONS")
@API.route("/languages", methods=["GET", "OPTIONS"])
class LanguageResource(Resource):
    """Resource for getting available languages."""

    @staticmethod
    @profiletime
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get():
        try:
            current_app.logger.debug("1. Getting user info")
            current_app.logger.debug(request.headers)
            headers_data = TenantSchema().load(dict(request.headers))
            tenant_key = headers_data.get("tenant_key")
            

            formio_service = FormioServiceExtended()
            formio_token = formio_service.get_formio_access_token()
            current_app.logger.debug("2. Getting lang submissions")
            languages = formio_service.get_all_submissions(f"{tenant_key}-languages", formio_token, 'data.status=public')
            response = []
            for lang in languages:
                response.append(lang['data'])

            return (
                response,
                HTTPStatus.OK,
            )
        except Exception as err:
            current_app.logger.error(err)
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
        
            return response, status
