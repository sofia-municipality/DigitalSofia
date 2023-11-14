"""API endpoints for managing form resource."""
import string
from http import HTTPStatus

from flask import current_app, make_response, render_template, request
from flask_restx import Namespace, Resource
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    auth,
    cors_preflight,
    profiletime,
)
from werkzeug.utils import secure_filename

from formsflow_documents.services import PDFService
from formsflow_documents.utils import DocUtils

API = Namespace("Form", description="Form")


@API.route("/<string:form_id>/submission/<string:submission_id>/render", doc=False)
class FormResourceRenderPdf(Resource):
    """Resource to render form and submission details as html."""

    @staticmethod
    @auth.require
    @profiletime
    def get(form_id: string, submission_id: string):
        """Form rendering method."""
        pdf_service = PDFService(form_id=form_id, submission_id=submission_id)
        default_template = "index.html"
        template_name = request.args.get("template_name")
        template_variable_name = request.args.get("template_variable")
        
        ### Get tenant key if applicable
        tenant_key = request.args.get("tenant_key")
        current_app.logger.debug(f"tenant_key={tenant_key}")

        use_template = bool(template_name)
        template_name = (
            DocUtils.url_decode(secure_filename(template_name))
            if use_template
            else default_template
        )

        template_variable_name = (
            DocUtils.url_decode(secure_filename(template_variable_name))
            if template_variable_name
            else None
        )
        if not pdf_service.search_template(template_name):
            raise BusinessException("Template not found!", HTTPStatus.BAD_REQUEST)
        if template_variable_name and not pdf_service.search_template(
                template_variable_name
        ):
            raise BusinessException(
                "Template variables not found!", HTTPStatus.BAD_REQUEST
            )
        render_data = pdf_service.get_render_data(
            use_template=use_template,
            template_variable_name=template_variable_name,
            token=request.headers.get("Authorization"),
            tenant_key=tenant_key
        )
        headers = {"Content-Type": "text/html"}
        return make_response(
            render_template(template_name, **render_data), 200, headers
        )


@cors_preflight("POST,OPTIONS")
@API.route(
    "/<string:form_id>/submission/<string:submission_id>/export/pdf",
    methods=["POST", "OPTIONS"],
)
@API.doc(
    params={
        "timezone": {
            "description": "Timezone of client device eg: Asia/Calcutta",
            "in": "query",
            "type": "string",
        }
    }
)
class FormResourceExportPdf(Resource):
    """Resource to export form and submission details as pdf."""

    @staticmethod
    @auth.require
    @profiletime
    def post(form_id: string, submission_id: string):
        """PDF generation and rendering method."""
        try:

            timezone = request.args.get("timezone")
            request_json = request.get_json()
            template = request_json.get("template")
            template_variables = request_json.get("templateVars")
            token = request.headers.get("Authorization")
            tenant_key = request.headers.get("X-Tenant-Key")
            use_template = bool(template)

            pdf_service = PDFService(form_id=form_id, submission_id=submission_id)

            template_name = None
            template_variable_name = None
            if use_template:
                (
                    template_name,
                    template_variable_name,
                ) = pdf_service.create_template(template, template_variables)
            
            ### HERE
            assert pdf_service.get_render_status(token, template_name) == 200
            current_app.logger.info("Generating PDF...")
            result = pdf_service.generate_pdf(
                timezone, token, tenant_key, template_name, template_variable_name
            )
            if result:
                if use_template:
                    current_app.logger.info("Removing temporary files...")
                    pdf_service.delete_template(template_name)
                    if template_variable_name:
                        pdf_service.delete_template(template_variable_name)
                return result
            response, status = (
                {
                    "message": "Cannot render pdf.",
                },
                HTTPStatus.BAD_REQUEST,
            )
            return response, status

        except BusinessException as err:
            current_app.logger.warning(err)
            current_app.logger.critical(err.error, exc_info=True)
            return err.error, err.status_code


@API.route("/<string:form_id>/draft/<string:draft_id>/render", doc=False)
class FormResourceRenderPdfFromDraft(Resource):
    """Resource to render form and submission details as html."""

    @staticmethod
    @auth.require
    @profiletime
    def get(form_id: string, draft_id: string):
        """Form rendering method."""
        current_app.logger.debug("1. FormResourceRenderPdfFromDraft")
        language = "bg"
        if request.args.get("language") is not None:
            language = request.args.get("language")

        current_app.logger.debug("2. Init PDF Service")
        pdf_service = PDFService(form_id=form_id, submission_id='', draft_id=draft_id, language=language)
        default_template = "index.html"
        template_name = request.args.get("template_name")
        template_variable_name = request.args.get("template_variable")
        ### Get tenant key if applicable
        tenant_key = request.args.get("tenant_key")
        current_app.logger.debug(f"tenant_key={tenant_key}")

        use_template = bool(template_name)
        template_name = (
            DocUtils.url_decode(secure_filename(template_name))
            if use_template
            else default_template
        )

        template_variable_name = (
            DocUtils.url_decode(secure_filename(template_variable_name))
            if template_variable_name
            else None
        )
        
        current_app.logger.debug(f"3.1 Template name - {template_name}")
        current_app.logger.debug(f"3.2 Template variable - {template_variable_name}")
        if not pdf_service.search_template(template_name):
            raise BusinessException("Template not found!", HTTPStatus.BAD_REQUEST)
        if template_variable_name and not pdf_service.search_template(
                template_variable_name
        ):
            raise BusinessException(
                "Template variables not found!", HTTPStatus.BAD_REQUEST
            )

        current_app.logger.debug(f"4. Get render data")
        render_data = pdf_service.get_render_data(
            use_template=use_template, 
            template_variable_name=template_variable_name, 
            token=request.headers.get("Authorization"),
            tenant_key=tenant_key
        )
        
        current_app.logger.debug(f"5. Headers")
        headers = {"Content-Type": "text/html"}
        current_app.logger.debug(f"6. Render template")
        # current_app.logger.debug(render_data)
        return make_response(
            render_template(template_name, **render_data), 200, headers
        )


@cors_preflight("POST,OPTIONS")
@API.route(
    "/<string:form_id>/draft/<string:draft_id>/export/pdf",
    methods=["POST", "OPTIONS"],
)
@API.doc(
    params={
        "timezone": {
            "description": "Timezone of client device eg: Asia/Calcutta",
            "in": "query",
            "type": "string",
        }
    }
)
class FormResourceExportPdfFromDraft(Resource):
    """Resource to export form and submission details as pdf."""

    @staticmethod
    @auth.require
    @profiletime
    def post(form_id: string, draft_id: string):
        """PDF generation and rendering method."""
        try:
            current_app.logger.debug("1. Getting variables")
            timezone = request.args.get("timezone")
            request_json = request.get_json()
            template = request_json.get("template")
            template_variables = request_json.get("templateVars")
            token = request.headers.get("Authorization")
            tenant_key = request.headers.get("X-Tenant-Key")

            use_template = bool(template)

            current_app.logger.debug("2. Init PDF Service")
            pdf_service = PDFService(form_id=form_id, submission_id='', draft_id=draft_id)

            template_name = None
            template_variable_name = None
            current_app.logger.debug(f"3. Should use template - {use_template}")
            if use_template:
                (
                    template_name,
                    template_variable_name,
                ) = pdf_service.create_template(template, template_variables)
            # assert pdf_service.get_render_status(token, template_name) == 200
            current_app.logger.info("4. Generating PDF from Draft...")
            result = pdf_service.generate_pdf(
                timezone, 
                token, 
                tenant_key=tenant_key, 
                template_name=template_name, 
                template_variable_name=template_variable_name
            )
            if result:
                if use_template:
                    current_app.logger.info("Removing temporary files...")
                    pdf_service.delete_template(template_name)
                    if template_variable_name:
                        pdf_service.delete_template(template_variable_name)
                return result
            response, status = (
                {
                    "message": "Cannot render pdf.",
                },
                HTTPStatus.BAD_REQUEST,
            )
            return response, status

        except BusinessException as err:
            current_app.logger.warning(err.error)
            return err.error, err.status_code
