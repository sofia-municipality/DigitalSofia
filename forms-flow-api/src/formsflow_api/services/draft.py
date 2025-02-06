"""This exposes submission service."""

from http import HTTPStatus
from typing import Dict
from PyPDF2 import PdfMerger, PdfReader, PdfWriter
from PyPDF2.errors import PdfReadError
import io

from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import ANONYMOUS_USER, DRAFT_APPLICATION_STATUS
from formsflow_api_utils.utils.enums import FormProcessMapperStatus
from formsflow_api_utils.utils.user_context import UserContext, user_context

from formsflow_api.exceptions import CommonException
from formsflow_api.models import Application, Draft, FormProcessMapper
from formsflow_api.schemas import DraftSchema
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services.document_meta_data import DocumentMetaData
from formsflow_api.services.external.bpm import BPMService

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
        "filename": "Spouse declaration - permanent"
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
        "filename": "Spouse declaration - current"
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
        current_app.logger.info(mapper)

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
            matches = re.match(r"(\w+)-(.+)", formio_path_name)
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
            if relevant_path_name.startswith("changeofpernamentaddress") or relevant_path_name.startswith(
                    "changeofcurrentaddress") or relevant_path_name.startswith(
                "trusteesignform"):
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
                requestorName = draft.data.get("firstName") + " " + draft.data.get("lastName")
                service = {"serviceId": draft.data.get("serviceId"),
                        "serviceName": draft.data.get("serviceName")}
                # # Add the metadata
                json_submission_data = draft.generate_application_json_submission_dict(requestorName, service)
                json_submission_data.pop('document', None)
                json_submission_data.pop('attorneyDocument', None)
                json_submission_data.pop('childCustodyDocument', None)

                writer.add_metadata(
                    {
                        "/application_json_xml": xml_string,
                        "/application_json_json": json_string,
                        "/application_json_submission": json.dumps({"data": json_submission_data}, ensure_ascii=False)
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
                    "referenceId": reference_number,
                    "formPath": formio_path_name
                }

            }

            current_app.logger.debug(application_id)
            current_app.logger.debug(person_identifier)
            current_app.logger.debug(reference_number)
            formio_service.post_submission(data=data, formio_token=formio_token)
            return (current_app.config.get(
                "FORMSFLOW_API_URL") + f"/form/{user.tenant_key}-generated-files/submission?data.applicationId=" +
                    str(application_id) +
                    "&userId=" +
                    str(person_identifier))
        except PdfReadError as err:
            response, status = {
                "type": "Bad request error",
                "message": "PDF file error.",
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.debug(url)
            current_app.logger.debug(request.get_json())
            current_app.logger.debug(result.content)

            raise BusinessException(response, status)
        except Exception as err:
            template = "An exception of type {0} occurred. Arguments:\n{1!r}"
            message = template.format(type(err).__name__, err.args)
            current_app.logger.debug(message)
            response, status = {
                "type": "Bad request error",
                "message": str(err),
            }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)

    @staticmethod
    def __exists_application_for_child(form_path: str, created_by: str, tenant: str, process: str, child_person_identifier: str = None):
        current_app.logger.warning("DraftService:__exists_application_for_child")

        child_result = Draft.find_app_for_child(form_path, 
                                                created_by, 
                                                tenant,
                                                process)

        current_app.logger.debug(f"child_result: {child_result}")

        # Remove prefix from the EGN to compare without prefixes
        if child_person_identifier:
            child_person_identifier = child_person_identifier.lower().replace("pnobg-", "")

        for record in child_result:
            current_app.logger.warning(f"record in child_result: {record}")

            # При споделено попечителство няма данни за behalf и няма данни в базата за драфт.
            # Да се провери тогава дали няма вече в камундата дали има вече 
            # подадено заявление за същото дете.
            # Ако подаваме заявление от лично качество, тогава ако лицето има 
            # поне едно подадено заявление за дете, не разрешаваме да продължи.
            if not "behalf" in record[3]:
                current_app.logger.warning("check for not completed application in camunda (no behalf data)")

                app_check_result = BPMService.get_apps_not_completed_for_child(
                    personIdentifier=created_by,
                    tenantId=tenant,
                    definitionId=process)

                current_app.logger.warning(f"app_check_result: {app_check_result}")

                if not app_check_result["success"]:
                    current_app.logger.warning(f"app_check_result ERROR: {app_check_result['error']}")
                    raise CommonException(app_check_result["error"])

                # Има поне едно заявление
                if app_check_result["data"] is not None and len(app_check_result["data"]) > 0:
                    
                    if not child_person_identifier:
                        # Не би трябвало да се влиза в този клон.
                        current_app.logger.warning(f"provided child_person_identifier is empty when there is no behalf property.")
                        return True
                    else: 
                        # Ако подаваме заявление за второ дете се проверява дали вече няма подадено
                        # вече заявление за същото дете.
                        for record in app_check_result["data"]:
                            current_app.logger.warning(f"record in app_check_result['data']: {record}")

                            variables = BPMService.get_process_variables(
                                                    process_instance_id=record["id"], 
                                                    token=None)
                            
                            current_app.logger.warning("executed BPMService.get_process_variables")

                            # Може да бъде върнато като обект или като стринг
                            existing_child_pi = variables.get("childPersonIdentifier")

                            current_app.logger.info(f"existing_child_pi: {existing_child_pi}")

                            if isinstance(existing_child_pi, str):
                                existing_child_pi = existing_child_pi.lower().replace("pnobg-", "")
                            else:
                                existing_child_pi = existing_child_pi.get("value").lower().replace("pnobg-", "")

                            if existing_child_pi == child_person_identifier:
                                return True
                        
            # При заявление от името на дете да се провери дали вече не е
            # подадено заявление за същото дете, като се провери за същото ЕГН.
            # Ако се подава от лично качество тогава нямаме childPersonIdentifier 
            # в данните и ако има поне едно заявление от името на дете се връща True
            elif record[3]["behalf"] == "child":
                if not child_person_identifier:
                    return True
                else:
                    existing_child_pi = record[3]["childPersonIdentifier"]
                    existing_child_pi = existing_child_pi.lower().replace("pnobg-", "")
                
                    if existing_child_pi == child_person_identifier:
                        return True

        return False

    @staticmethod
    @user_context
    def check_existing_application_for_child(service_id: str, for_person_identifier: str, child_person_identifier: str, **kwargs):
        """Check for existing, not completed applications for a child."""

        try:
            # 'serviceId': 2079  / sofia-changeofpernamentaddress - Постоянен адрес
            # 'serviceId': 2107 / sofia-changeofcurrentaddress - Текущ адрес

            current_app.logger.debug("check_existing_application_for_child")

            if for_person_identifier == "":
                return "Target person identifier is required", HTTPStatus.BAD_REQUEST

            if "user" not in kwargs:
                return "User not found in arguments", HTTPStatus.BAD_REQUEST

            if service_id is None or service_id == 0:
                return "serviceId is required", HTTPStatus.BAD_REQUEST

            form_path = None

            current_app.logger.debug(f"ServiceID: {service_id}")

            if service_id == "2079":
                form_path = "sofia-changeofpernamentaddress"

            if service_id == "2107":
                form_path = "sofia-changeofcurrentaddress"

            if form_path is None:
                return "form_path cannot be extracted from the serviceId", HTTPStatus.BAD_REQUEST

            user = kwargs.get('user')

            if not user:
                return "User not found in arguments", HTTPStatus.BAD_REQUEST

            # Remove the EGN prefix
            target_person_identifier = for_person_identifier.lower().replace("pnobg-", "")

            # Prepare data input to check for the existing application on behalf of the child
            process_definition = current_app.config.get("CAMUNDA_CHANGE_ADDRESS_PROCESS")
            tenant = user.token_info["tenantKey"]

            child_exists_result = DraftService.__exists_application_for_child(form_path,
                                                                        f"pnobg-{target_person_identifier}",
                                                                        tenant,
                                                                        process_definition, 
                                                                        child_person_identifier)

            current_app.logger.debug(
                f"Checking for existing application on behalf of the child with: RESULT:{child_exists_result} "
                f"personIdentifier:{target_person_identifier}; "
                f"tenantKey:{tenant}; processDefinitionId:{process_definition}")

            if child_exists_result:
                return {
                    "error": "There is at least one unfinished application on behalf of the child.",
                    "errorMessageTranslation": "app_on_behalf_child_error",
                    "personIdentifier": target_person_identifier,
                    "tenantKey": tenant,
                    "formPath": form_path,
                    "processDefinition": process_definition,
                    "resultSource": "database"
                }, HTTPStatus.UNPROCESSABLE_ENTITY

            return None, HTTPStatus.OK

        except Exception as ex:
            current_app.logger.error(f"Error check_existing_application_for_child: {ex}")
            return {
                "error": ex
            }, HTTPStatus.INTERNAL_SERVER_ERROR
