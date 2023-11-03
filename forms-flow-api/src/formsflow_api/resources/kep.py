from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import (
    cors_preflight,
    auth,
    profiletime
)
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api.exceptions import KEPException
from formsflow_api.models import DocumentStatus
from formsflow_api.services.external.signature_services_integration import SignatureServicesIntegrationService
from formsflow_api.services import FormioServiceExtended

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
        service = SignatureServicesIntegrationService()
        try:
            user: UserContext = kwargs["user"]
            tenant_key = user.tenant_key
            data = request.get_json()
            response = service.sign_document(data=data)
            
            ### Find document in formio
            formio_client = FormioServiceExtended()
            formio_resource_id = data.get("formioId")

            ### Update formio status
            status = DocumentStatus.query.filter_by(title="Signed").first()

            ### Find correct path
            form_path = current_app.config.get('FORMIO_FILE_RESOURCE_PATH')
            if tenant_key:
                form_path = f"{tenant_key}-{form_path}"

            formio_client.update_resource_formio_status(
                formio_form_path=form_path,
                formio_resource_id=formio_resource_id,
                formio_status=status.formio_status
            )

            formio_client.update_resource_formio_file(
                    form_path=form_path,
                    formio_resource_id=formio_resource_id,
                    type=response.get("mimeType").get("mimeTypeString"),
                    name=data.get("documentName"),
                    content=response.get("bytes")
                )

            ### Return original response
            return response
        except BusinessException as err:
            current_app.logger.warning(err.error)
            return err.error, err.status_code
        except KEPException as err:
            current_app.logger.warning(err.error)
            return err.error, err.status_code