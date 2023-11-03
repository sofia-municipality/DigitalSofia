"""This exposes submission service."""

from http import HTTPStatus
from typing import Dict
from PyPDF2 import PdfMerger, PdfReader, PdfWriter
import io


from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import ANONYMOUS_USER, DRAFT_APPLICATION_STATUS
from formsflow_api_utils.utils.enums import FormProcessMapperStatus
from formsflow_api_utils.utils.user_context import UserContext, user_context

from formsflow_api.models import Application, Draft, FormProcessMapper
from formsflow_api.schemas import DraftSchema
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services.document_meta_data import DocumentMetaData

from .application import ApplicationService
from flask import current_app, request
import requests
import base64
import json
import re
import os

FORMIO_FILENAME_CONDITIONS = [
    {
        "path": "changeofpernamentaddress",
        "data": [],
        "filename": "Address Change - permanent"
    },
    {
        "path": "changeofcurrentaddress",
        "data": [],
        "filename": "Address Change - current"
    },
    {
        "path": "signitureform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "current",
            }
        ],
        "filename": "Address Change - current"
    },
    {
        "path": "signitureform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "permanent",
            }
        ],
        "filename": "Address Change - permanent"
    },
    {
        "path": "trusteesignform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "current",
            }
        ],
        "filename": "Address Change - current"
    },
    {
        "path": "trusteesignform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "permanent",
            }
        ],
        "filename": "Address Change - permanent"
    },
    {
        "path": "secondParentForm",
        "data": [
            {
                "key": "addressChangeType",
                "value": "permanent",
            }
        ],
        "filename": "Address Change - permanent"
    },
    {
        "path": "secondParentForm",
        "data": [
            {
                "key": "addressChangeType",
                "value": "current",
            }
        ],
        "filename": "Address Change - current"
    },
    {
        "path": "ownersignform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "permanent",
            }
        ],
        "filename": "Owner Declaration - permanent"
    },
    {
        "path": "ownersignform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "current",
            }
        ],
        "filename": "Owner Declaration - current"
    },
    {
        "path": "ownersignform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "permanent",
            },
            {
                "key": "property",
                "value": "liveWithOwner",
            }
        ],
        "filename": "Spouce declaration - permanent"
    },
    {
        "path": "ownersignform",
        "data": [
            {
                "key": "addressChangeType",
                "value": "current",
            },
            {
                "key": "property",
                "value": "liveWithOwner",
            }
        ],
        "filename": "Spouce declaration - current"
    }
]
    
DEFAULT_DOCUMENT_FILENAME = "Document"

class DraftService:
    """This class manages submission service."""

    @staticmethod
    def __create_draft(data):
        """Create new draft entry."""
        return Draft.create_draft_from_dict(data)

    @staticmethod
    def __create_draft_application(data):
        """Create draft application."""
        application = Application.create_from_dict(data)
        return application

    @classmethod
    @user_context
    def create_new_draft(cls, application_payload, draft_payload, token=None, **kwargs):
        """Creates a new draft entry and draft application."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name or ANONYMOUS_USER
        tenant_key = user.tenant_key
        application_payload["created_by"] = user_id
        mapper = FormProcessMapper.find_form_by_form_id(application_payload["form_id"])
        if mapper is None:
            if tenant_key:
                raise BusinessException(
                    f"Permission denied, formId - {application_payload['form_id']}.",
                    HTTPStatus.FORBIDDEN,
                )
            raise BusinessException(
                f"Mapper does not exist with formId - {application_payload['form_id']}.",
                HTTPStatus.BAD_REQUEST,
            )
        if (mapper.status == FormProcessMapperStatus.INACTIVE.value) or (
            not token and not mapper.is_anonymous
        ):
            raise BusinessException(
                f"Permission denied, formId - {application_payload['form_id']}.",
                HTTPStatus.FORBIDDEN,
            )
        if tenant_key is not None and mapper.tenant != tenant_key:
            raise BusinessException(
                "Tenant authentication failed.", HTTPStatus.FORBIDDEN
            )
        application_payload["form_process_mapper_id"] = mapper.id

        application_payload["application_status"] = DRAFT_APPLICATION_STATUS
        application_payload["submission_id"] = None
        application = cls.__create_draft_application(application_payload)
        if not application:
            response, status = {
                "type": "Internal server error",
                "message": "Unable to create application",
            }, HTTPStatus.INTERNAL_SERVER_ERROR
            raise BusinessException(response, status)
        draft_payload["application_id"] = application.id
        draft = cls.__create_draft(draft_payload)
        return draft

    @staticmethod
    @user_context
    def get_draft(draft_id: int, **kwargs):
        """Get submission."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        draft = Draft.find_by_id(draft_id=draft_id, user_id=user_id)
        if draft:
            draft_schema = DraftSchema()
            return draft_schema.dump(draft)

        response, status = {
            "type": "Bad request error",
            "message": f"Invalid request data - draft id {draft_id} does not exist",
        }, HTTPStatus.BAD_REQUEST
        raise BusinessException(response, status)

    @staticmethod
    @user_context
    def update_draft(draft_id: int, data, **kwargs):
        """Update draft."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name or ANONYMOUS_USER
        draft = Draft.get_by_id(draft_id, user_id)
        if draft:
            draft.update(data)
        else:
            response, status = {
                "type": "Bad request error",
                "message": f"Invalid request data - draft id {draft_id} does not exist",
            }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)

    @staticmethod
    @user_context
    def get_all_drafts(query_params, **kwargs):
        """Get all drafts."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        page_number = query_params.get("page_no")
        limit = query_params.get("limit")
        sort_by = query_params.get("order_by", "id")
        sort_order = query_params.get("sort_order", "desc")
        form_name = query_params.get("form_name")
        draft_id = query_params.get("id")
        modified_from_date = query_params.get("modified_from_date")
        modified_to_date = query_params.get("modified_to_date")
        draft, count = Draft.find_all_active(
            user_id,
            page_number,
            limit,
            sort_by,
            sort_order,
            modified_from=modified_from_date,
            modified_to=modified_to_date,
            form_name=form_name,
            id=draft_id,
        )
        draft_schema = DraftSchema()
        return draft_schema.dump(draft, many=True), count

    @staticmethod
    @user_context
    def make_submission_from_draft(data: Dict, draft_id: str, token=None, **kwargs):
        """Makes the draft into an application."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name or ANONYMOUS_USER
        draft = Draft.make_submission(draft_id, data, user_id)
        if not draft:
            response, status = {
                "type": "Bad request error",
                "message": f"Invalid request data - draft id {draft_id} does not exist",
            }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)

        application = Application.find_by_id(draft.application_id)
        mapper = FormProcessMapper.find_form_by_form_id(application.latest_form_id)
        if application.form_process_mapper_id != mapper.id:
            # The form mapper version got updated after the draft entry
            # was created, update the application with new mapper
            application.update({"form_process_mapper_id": mapper.id})
        payload = ApplicationService.get_start_task_payload(
            application, mapper, data["form_url"], data["web_form_url"]
        )
        ApplicationService.start_task(mapper, payload, token, application)
        return application

    @staticmethod
    @user_context
    def delete_draft(draft_id: int, **kwargs):
        """Delete draft."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        draft = Draft.get_by_id(draft_id=draft_id, user_id=user_id)
        if draft:
            # deletes the draft and application entry related to the draft.
            draft.delete()
        else:
            response, status = {
                "type": "Bad request error",
                "message": f"Invalid request data - draft id {draft_id} does not exist",
            }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)
        
        
    @staticmethod
    @user_context
    def export_draft_to_pdf(draft_id: int, **kwargs):
        try:
            ### Get user
            user: UserContext = kwargs["user"]
            draft = Draft.query.get(draft_id)
            application = draft.get_application()
            application_id = application.id
            form_id = application.latest_form_id
            current_app.logger.debug("Getting application")
            if not application or not form_id:
                raise BusinessException("No application bound to draft", HTTPStatus.BAD_REQUEST)

            url = (
                    current_app.config.get("FORMSFLOW_DOC_API_URL") +
                    "/form/" +
                    form_id +
                    "/draft/" +
                    str(draft_id) +
                    "/export/pdf"
            )

            headers = {
                "Authorization": request.headers["Authorization"],
                "Content-Type": "application/json"
            }

            tenant_key = user.tenant_key
            if tenant_key:
                headers['X-Tenant-Key'] = user.tenant_key
            
            current_app.logger.debug("Post to documents")
            result = requests.post(url, headers=headers, data=json.dumps(request.get_json()))

            ### Generate correct pdf name based on path
            name = ''
            ### Get formio path name from application form_process_mapper
            formio_path_name = application.form_process_mapper.form_path

            ### Generatting the correct form
            current_app.logger.debug(f"Generating correct filename for path_name - {formio_path_name}")
            matches = re.match(r"(\w+)-(.+)",formio_path_name)
            tenant_key = matches[1]
            current_app.logger.debug(f"tenant_key - {tenant_key}")
            relevant_path_name = matches[2]
            current_app.logger.debug(f"relevant_path_name - {relevant_path_name}")


            name = DEFAULT_DOCUMENT_FILENAME
            ### Go throuh filename conditions
            for item in FORMIO_FILENAME_CONDITIONS:

                ### Check if formio entry's path starts with the condition path
                condition_path = item.get("path")
                current_app.logger.debug(f"condition_path - {condition_path}")
                if condition_path and relevant_path_name and relevant_path_name.startswith(condition_path):

                    ### Check if we have additional values to check
                    values_data_to_check = item.get("data")
                    if not values_data_to_check:
                        name = item.get("filename")
                    else:
                        shouldChangeName = True

                        ### If we fail any of the checks
                        for key_value_data_to_check in values_data_to_check:
                            key = key_value_data_to_check.get("key")
                            value = key_value_data_to_check.get("value")
                            if draft.data.get(key) != value:
                                shouldChangeName = False
                        
                        if shouldChangeName:
                            name = item.get("filename")

            ### Add reference_number
            reference_number = request.get_json().get("reference_number", " ")
            name = name + " - " + reference_number + ".pdf"
            
            current_app.logger.debug("Generate name")
            current_app.logger.debug(name)

            xml_string = None
            json_string = None

            ### Should we get metadata
            if relevant_path_name.startswith("changeofpernamentaddress") or relevant_path_name.startswith("changeofcurrentaddress"):
                current_app.logger.debug("Generate metadata dict")
                ### Get metadata
                xml_file_name = os.path.join(current_app.static_folder, 'data', 'xml', 'change_address_template.xml')
                metadata_instance = DocumentMetaData(xml_file_name)
                metadata_instance.parse_with_dict(data=draft.generate_metadata_dict(user))

                xml_string = metadata_instance.convert_to_string()
                json_string = json.dumps(metadata_instance.convert_to_dict(), ensure_ascii=False)


            ### Write metadata
            current_app.logger.debug("PyPdf2")
            open_pdf_file = io.BytesIO(result.content)
            
            reader = PdfReader(open_pdf_file)
            writer = PdfWriter()

            # Add all pages to the writer
            for page in reader.pages:
                writer.add_page(page)

            if xml_string and json_string:
                # # Add the metadata
                writer.add_metadata(
                    {
                        "/application_json_xml": xml_string,
                        "/application_json_json": json_string,
                    }
                )

            write_buffer = io.BytesIO()
            writer.write(write_buffer)
            
            current_app.logger.debug("Finishing adding metadata")
            base64File = base64.b64encode(write_buffer.getvalue())
            base64File = base64File.decode()
            write_buffer.close()
            open_pdf_file.close()

            person_identifier = ''
            if "personIdentifier" in user.token_info:
                person_identifier = user.token_info["personIdentifier"]

            current_app.logger.debug("Fetch formio file")
            formio_service = FormioServiceExtended()
            formio_token = formio_service.get_formio_access_token()
            file_form_id = formio_service.fetch_form_id_by_path(user.tenant_key + '-generated-files', formio_token)

            data = {
                "formId": file_form_id,
                "data": {
                    "applicationId": str(application_id),
                    "file": [{
                        "name": name,
                        "originalName": name,
                        "size": len(str(base64File)),
                        "storage": "base64",
                        "type": "application/pdf",
                        "url": "data:application/pdf;base64," + base64File
                    }],
                    "status": 'unsigned',
                    "userId": person_identifier,
                    "referenceId": reference_number
                }

            }

            formio_service.post_submission(data=data, formio_token=formio_token)
            return (current_app.config.get(
                "FORMSFLOW_API_URL") + f"/form/{user.tenant_key}-generated-files/submission?data.applicationId=" +
                    str(application_id) +
                    "&userId=" +
                    str(person_identifier))
        except Exception as err:
            current_app.logger.debug(err)
            response, status = {
                                   "type": "Bad request error",
                                   "message": str(err),
                               }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)
