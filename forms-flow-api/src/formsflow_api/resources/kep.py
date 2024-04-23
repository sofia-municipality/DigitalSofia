
import base64
import os
import json
from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from io import BytesIO
from werkzeug.datastructures import FileStorage
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    profiletime
)
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api.exceptions import KEPException
from formsflow_api.models import DocumentStatus, Application
from formsflow_api.services.external import BPMService, SignatureServicesIntegrationService
from formsflow_api.services import FormioServiceExtended, OtherFileService, AcstreService, DocumentsService

API = Namespace("KEP_API", description="KEP Signature")

mime_type = API.model(
    "MimeType",
    {
        "mimeTypeString": fields.String()
    }
)
kep_sign_request = API.model(
    "KEPSignRequest",
    {
        "signingCertificate": fields.String(),
        "certificateChain": fields.List(fields.String()),
        "encryptionAlgorithm": fields.String(),
        "digestToSign": fields.String(),
        "documentToSign": fields.String(),
        "signingDate": fields.String(),
        "signatureValue": fields.String()
    },
)
kep_sign_data_response = API.model(
    "KEPSignDataResponse",
    {
        "dataToSign": fields.String(),
    },
)

kep_sign_response = API.model(
    "KEPSignResponse",
    {
        "bytes": fields.String(),
        "digestAlgorithm": fields.String(),
        "mimeType": fields.Nested(mime_type),
        "name": fields.String(),
    },
)


@cors_preflight("POST, OPTIONS")
@API.route("/document/data", methods=["POST", "OPTIONS"])
class KEPDataResource(Resource):

    @staticmethod
    @auth.require
    @user_context
    @API.doc(body=kep_sign_request)
    @API.response(200, "OK:- Successful request.", model=kep_sign_data_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def post(**kwargs):
        """Retrieve regions"""
        service = SignatureServicesIntegrationService()
        try:
            return service.get_document_data(data=request.get_json())
        except KEPException as err:
            current_app.logger.warning(err.error)
            return err.error, err.status_code


@cors_preflight("POST, OPTIONS")
@API.route("/document/sign", methods=["POST", "OPTIONS"])
class KEPSignResource(Resource):
    
    @staticmethod
    @auth.require
    @user_context
    @API.doc(body=kep_sign_request)
    @API.response(200, "OK:- Successful request.", model=kep_sign_data_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def post(**kwargs):
        """Retrieve regions"""
        current_app.logger.info("KEPSignResource@post")
        current_app.logger.debug("Init SignatureServicesIntegrationService")
        service = SignatureServicesIntegrationService()
        
        document_service_client = DocumentsService()
        try:
            user: UserContext = kwargs["user"]
            tenant_key = user.tenant_key
            data = request.get_json()
            response = service.sign_document(data=data)
            current_app.logger.debug("/signature/document/sign Response mimeTpye")
            current_app.logger.debug(response.get("mimeType"))
            
            ### Find document in formio
            formio_client = FormioServiceExtended()
            formio_resource_id = data.get("formioId")

            ### Update formio status
            status = DocumentStatus.query.filter_by(title="Signed").first()

            ### Find correct path
            form_path = current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
            if tenant_key:
                form_path = f"{tenant_key}-{form_path}"

            document_service_client.update_document_status_in_formio(
                formio_id=formio_resource_id, 
                tenant_key=tenant_key,
                status=status,
            )

            mime_type = response.get("mimeType")
            mime_type_variable_type = type(mime_type)
            if mime_type_variable_type is str:           
                if mime_type == "PDF":
                    mime_type = "application/pdf"
                else:
                    mime_type = mime_type

            elif mime_type_variable_type is dict:
                mime_type = mime_type.get("mimeTypeString", "application/pdf")
            else:
                mime_type = "application/pdf"

            ### Although it is bytes response.get("bytes") this is a base64 encoding
            ### Dump the Response to a file just to check
            # file_path = os.path.join(current_app.static_folder, 'data', 'logs')
            # os.makedirs(name=file_path, exist_ok=True)
            # with open(file_path + "error-log.json", "w") as problem_file:
            #     problem_file.write(json.dumps(response))
                

            # current_app.logger.debug("Update Resource Formio File")
            # current_app.logger.debug(response.keys())
            # current_app.logger.debug(response.get("name"))
            # current_app.logger.debug(data.get("documentName"))
            # current_app.logger.debug(mime_type)

            formio_response = formio_client.update_resource_formio_file(
                    form_path=form_path,
                    formio_resource_id=formio_resource_id,
                    type=mime_type,
                    name=data.get("documentName"),
                    content=response.get("bytes"),
                    signature_source="kep"
                )

            ### Add signed files to acstre application
            should_save_file = data.get("shouldSave")
            current_app.logger.debug(f"Should save - {should_save_file}")
            if should_save_file:
                application_id = data.get("originalApplicationId")
                if application_id:
                    other_file_service = OtherFileService()
                    additional_path = f"{application_id}/"
                        
                    base64_string = response.get("bytes")
                    binary_data = base64.b64decode(base64_string)

                    stream = BytesIO(binary_data)

                    file = FileStorage(
                        stream=stream, 
                        filename=data.get("documentName"), 
                        content_type=mime_type
                    )
                    other_file_model = other_file_service.save_file(
                        user_id=user.user_name,
                        file=file,
                        application_id=application_id,
                        additional_path=additional_path
                    )
                    acstre_client = AcstreService()
                    other_files = acstre_client.get_application_files(application_id=application_id)
                    other_files.append({
                        "url": other_file_model.file_url,
                        "name": other_file_model.file_name,
                        "size": other_file_model.file_size
                    })
                    acstre_client.add_other_files_to_application(application_id=application_id, other_files=other_files)

            ### Return original response
            return response
        except BusinessException as err:

            current_app.logger.warning(err.status_code)
            return err.error, err.status_code
        except KEPException as err:
            current_app.logger.warning(err.error)
            return err.error, err.status_code