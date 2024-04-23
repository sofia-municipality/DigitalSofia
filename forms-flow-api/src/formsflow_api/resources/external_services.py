from http import HTTPStatus

import re
import base64
from flask_restx import Namespace, Resource
from http import HTTPStatus
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services import OtherFileService
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    profiletime,
    UserContext,
    user_context
)
from formsflow_api_utils.exceptions import BusinessException
from flask import send_file, current_app, make_response, request


API = Namespace("ExternalServices", description="Temporary ep for external services")


@API.route("/documents/", methods=["GET", "POST", "OPTIONS"])
class ExternalServicesSaveFile(Resource):

    def options(self, *args, **kwargs):  # pylint: disable=unused-argument
        return (
            {"Allow": "GET"},
            200,
            {
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
                "Access-Control-Allow-Headers": "Authorization, Content-Type, X-Tenant-Key, X-Jwt-Token",
            },
        )

    @staticmethod
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(400, "BAD_REQUEST:- Invalid request.")
    @API.response(404, "NOT_FOUND:- No document found to check status.")
    def get(**kwargs):
        return {
            "Foo": "Bar"
        }

    @staticmethod
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(400, "BAD_REQUEST:- Invalid request.")
    @API.response(404, "NOT_FOUND:- No document found to check status.")
    def post(**kwargs):
        person_identifier = request.args.get("personIdentifier")

        FormioServiceExtended()

        if 'file' not in request.files:
            current_app.logger.debug("No file")
        else:
            file = request.files['file']
            # If the user does not select a file, the browser submits an
            # empty file without a filename.
            current_app.logger.debug(file.filename)
            current_app.logger.debug(file)

        client = OtherFileService()
        other_file_model = client.save_file(
            user_id=person_identifier,
            file=file,
        )

        current_app.logger.debug(other_file_model)

        return {
            "url": other_file_model.file_url,
            "name": other_file_model.file_name,
            "size": other_file_model.file_size
        }, HTTPStatus.OK
    

@API.route("/documents/<string:hash_value>", methods=["GET", "DELETE", "OPTIONS"])
class ExternalServicesOtherFile(Resource):

    @staticmethod
    def options(*args, **kwargs):  # pylint: disable=unused-argument
        return (
            {"Allow": "GET"},
            200,
            {
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "GET,DELETE,OPTIONS",
                "Access-Control-Allow-Headers": "Authorization, Content-Type, X-Tenant-Key, X-Jwt-Token",
            },
        )
    
    @staticmethod
    def get(hash_value:str):
        client = OtherFileService()
        other_file = client.get_file_by_hash(hash_value)
        if not other_file:
            response, status = {
                "type": "Not found",
                "message": f"No document with supplied hash value '{hash_value}' found",
            }, HTTPStatus.BAD_REQUEST

            return response, status

        return send_file(
            client.save_path + other_file.file_path,
            mimetype=other_file.file_mimetype,
            download_name=other_file.file_name
        )

    @staticmethod
    def delete(hash_value:str):
        current_app.logger.debug("ExternalServicesOtherFile@delete")
        client = OtherFileService()
        response = client.delete(hash=hash_value)

        if response:
            return (
                {
                    "response": f"Document with hash {hash_value} was deleted"
                }, 
                HTTPStatus.OK
            )
        
        return (
                {
                    "response": f"Can't find file with {hash_value}"
                }, 
                HTTPStatus.NOT_FOUND
            )


@cors_preflight("GET,OPTIONS")
@API.route("/documents/<string:hash>/serve", methods=["GET", "OPTIONS"])
class ExternalServicesServeOtherFile(Resource):

    @staticmethod
    @profiletime
    @user_context
    def get(hash:str, **kwargs):
        return "bar"


@cors_preflight("GET,OPTIONS")
@API.route("/documents/<string:submission_formio_id>/serve", methods=["GET", "OPTIONS"])
class ExternalServicesServeFile(Resource):

    @staticmethod
    @profiletime
    @API.response(200, "OK:- Successful request.")
    @API.response(400, "BAD_REQUEST:- Invalid request.")
    @API.response(404, "NOT_FOUND:- No document found to check status.")
    def get(submission_formio_id: str, **kwargs):
        try:
            # user: UserContext = kwargs["user"]

            # realm_access = user.token_info.get("realm_access", {})
            # roles = realm_access.get("roles", [])
            # current_app.logger.debug(user.user_name)
            # current_app.logger.debug(roles)
            # if "axterAdmin" not in roles:
            #     return (
            #         {
            #             "type": "Invalid Role Error",
            #             "message": "Authorized Account is not an axterAdmin."
            #         },
            #         HTTPStatus.UNAUTHORIZED
            #     )

            formio_client = FormioServiceExtended()

            form_path = "sofia" + "-" + current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
            formio_token = formio_client.get_formio_access_token()
            file_form_id = formio_client.fetch_form_id_by_path(form_path=form_path, formio_token=formio_token)

            response = formio_client.debug_get_submission(
                data={
                    "form_id": file_form_id,
                    "sub_id": submission_formio_id
                },
                formio_token=formio_token
            )

            submission_data = response.get("data")
            file_data = submission_data.get("file", [])[-1]

            if not file_data:
                return {
                            "type": "Not found",
                            "message": f"No document with supplied formioId {submission_formio_id} found"
                        }, HTTPStatus.NOT_FOUND

            file = file_data['url']
            file = re.split(";|,", file)

            ### If the string is as following - data:application/pdf;base64,JVBERi0xLjQKJdPr6eEKMSAwIG9ia...
            ### The above split will make a file corresponding to:
            ### 0 - data:application/pdf - file data type
            ### 1 - base64 - encoding type
            ### 2 - JVBERi0xLjQKJdPr6eEKMSAw... - 

            image_binary = base64.b64decode(file[2])
            response = make_response(image_binary)
            response.headers.set('Content-Type', file_data['type'])
            response.headers.set(
                'Content-Disposition', 'inline', filename=file_data['name'].encode("utf-8"))
            return response

            # return send_from_directory(current_app.static_folder, 
            #     'data/temporary/dummy.pdf', 
            #     download_name=file_hash, 
            #     mimetype="application/pdf"
            # )
        except BusinessException as err:
            current_app.logger.error(err.error)
            current_app.logger.error(err.status_code)
            if err.status_code == 400:
                response, status = {
                                       "type": "Not found",
                                       "message": f"No document with supplied formioId {submission_formio_id} found",
                                   }, HTTPStatus.BAD_REQUEST
            else:
                if err.status_code == 401:
                    response, status = {
                                           "type": "Invalid Token Error",
                                           "message": "Access to formsflow.ai API Denied. Check if the bearer token is passed for Authorization or has expired.",
                                       }, HTTPStatus.UNAUTHORIZED
                else:
                    response, status = {
                                           "type": "Bad request error",
                                           "message": "Internal server error",
                                       }, HTTPStatus.BAD_REQUEST

            return response, status
        
        response = make_response(image_binary)
        response.headers.set('Content-Type', file_data['type'])
        response.headers.set(
            'Content-Disposition', 'inline', filename=file_data['name'].encode("utf-8"))
        return response
