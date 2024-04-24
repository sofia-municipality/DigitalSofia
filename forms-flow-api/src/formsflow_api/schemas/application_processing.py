"""This manages application Response Schema."""

import re, base64, binascii, json, mimetypes
from marshmallow import (
    EXCLUDE, 
    Schema, 
    fields, 
    validates, 
    validates_schema, 
    post_load,
    ValidationError
)
from flask import current_app
from io import BytesIO
from PyPDF2 import PdfReader, errors
from formsflow_api.models import Application


# class ApplicationProcessingDocument

class ApplicationDocumentProcessedRequest(Schema):
    
    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        unknown = EXCLUDE

    status = fields.String(required=True, nullable=False)
    description = fields.String(required=True, nullable=True)
    documents = fields.List(fields.Dict(), required=True)

    @validates("status")
    def validate_status(self, status_name):
        valid_statuses = ["completed", "denied"]
        if status_name not in valid_statuses:
            raise ValidationError("Invalid status value. Valid values are:" + ",".join(valid_statuses))

    @validates("documents")
    def validate_documents(self, documents_list):
        ### Check if the documents list is good
        
        if not documents_list:
            return

        documents_error_list = {}
        other_files = []

        for index, document in enumerate(documents_list):
            ### Check if item is a correct dict
            ### {
            ###     "name": str,
            ###     "file": str // base64
            ### }
            if ("name" not in document) or ("file" not in document):
                documents_error_list[index] = "Invalid format for file item. File must contain a 'name' and 'file' attribute"
            
            ### Check if file is a correctly encoded base64 string
            else:
                try:
                    ### We only care that it is a correct base64 string
                    base64.b64decode(document.get("file"), validate=True)
                except binascii.Error:
                    documents_error_list[index] = "Invalid base64 string set as 'file'"
                
                # decoded_bytes = BytesIO(decoded_string)
                # reader = PdfReader(decoded_bytes)
                # # application_json_submission = .get(, {})
                # current_app.logger.debug(f"===== PDF Metadata =====")
                # current_app.logger.debug(reader.metadata.keys())
                # if "/application.json_submission" in reader.metadata or "/application_json_submission" in reader.metadata:
                #     continue
                # else:
                #     other_files.append(document)

        if documents_error_list:
            raise ValidationError(documents_error_list)

        self.other_files = other_files

class ApplicationProcessingCreateRequest(Schema):
    """This class manages aggregated application response schema."""

    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        unknown = EXCLUDE

    documents = fields.List(fields.Dict(), required=True)
    assignees = fields.List(
        fields.String(), 
        required=True, 
        error_messages={
          'required': 'Missing data for required field.', 
        }
    )
    valid_groups = fields.List(fields.String(), required=True)
    external_id = fields.String(data_key="externalId", required=True)
    has_payment = fields.Boolean(data_key="hasPayment", required=True)
    tax_amount = fields.String(data_key="taxAmount", required=True)
    payment_till = fields.String(data_key="paymentTill", required=True)

    application_json_submission = fields.Dict(required=False)
    
    origin_process_instance_id = fields.String(required=False)
    application_id = fields.String(required=False)
    has_digitall_sofia_origin = fields.Boolean(required=False, default=False)

    other_files = fields.List(fields.Dict(),required=False)

    def has_key(self, element, *keys):
        if not isinstance(element, dict):
            raise AttributeError('keys_exists() expects dict as first argument.')
        
        if len(keys) == 0:
            raise AttributeError('keys_exists() expects at least two arguments, one given.')

        _element = element
        for key in keys:
            try:
                _element = _element[key]
            except KeyError:
                return False
            
        return True

    @validates_schema
    def validate_users(self, data, **kwargs):
        ### Check if the person identifier exists within keycloak
        ### importing here due to circular import error
        from formsflow_api.services.external import KeycloakAdminAPIService
        keycloak_client = KeycloakAdminAPIService()

        assignees_list = data["assignees"]
        if not assignees_list:
            raise ValidationError({"assignees": "Missing data for required field"})


        assignees_error_list = {}
    
        for person_identifier in assignees_list:
            
            ### Check if it is a valid person identifier
            if not re.match("^\d{10}$",person_identifier):
                assignees_error_list[person_identifier] = f"Invalid personal identifier {person_identifier}. Incorrect format."
                continue

            ### Check if the pnobg is a user within keycloak
            url_path = f"users?username=pnobg-{person_identifier}&exact={True}"
            keycloak_user = keycloak_client.get_request(url_path)

            if not keycloak_user:
                assignees_error_list[person_identifier] = f"Invalid personal identifier {person_identifier}."
                continue
            
            ### Check if user is within the correct group for axter
            keycloak_user = keycloak_user[0]
            # current_app.logger.debug(f"{person_identifier} - {keycloak_user}")
            reviewer_groups = keycloak_client.get_user_groups(keycloak_user["id"])

            has_valid_group = False
            # current_app.logger.debug(f"----------\n{data['valid_groups']}\n----------")
            for group in reviewer_groups:
                # current_app.logger.debug(group)
                if group.get("id") in data["valid_groups"]:
                    has_valid_group = True
                    break
            
            if not has_valid_group:
                assignees_error_list[person_identifier] = f"Invalid group. Current Axter Service Account, can't access the user resource."


        if assignees_error_list:
            raise ValidationError({"assignees": assignees_error_list})

    @validates("documents")
    def validate_documents(self, documents_list):
        ### Check if the documents list is good
        
        if not documents_list:
            raise ValidationError("Documents can not be empty")
        
        documents_error_list = {}
        application_json_submission = None
        other_files = []

        for index, document in enumerate(documents_list):
            ### Check if item is a correct dict
            ### {
            ###     "name": str,
            ###     "file": str // base64
            ### }
            if ("name" not in document) or ("file" not in document):
                documents_error_list[index] = "Invalid format for file item. File must contain a 'name' and 'file' attribute"
            
            ### Check if file is a correctly encoded base64 string
            else:
                try:
                    
                    mime_type, encoding = mimetypes.guess_type(document.get("name"))
                    current_app.logger.debug(mime_type)
                    if mime_type != 'application/pdf':
                        other_files.append(document)
                        continue

                    decoded_string = base64.b64decode(document.get("file"), validate=True)
                    decoded_bytes = BytesIO(decoded_string)
                    reader = PdfReader(decoded_bytes)
                    # application_json_submission = .get(, {})
                    current_app.logger.debug(f"===== PDF Metadata =====")
                    current_app.logger.debug(reader.metadata.keys())
                    if "/application.json_submission" in reader.metadata or "/application_json_submission" in reader.metadata:
                        application_json_submission_string = reader.metadata.get("/application.json_submission")
                        if not application_json_submission_string:
                            application_json_submission_string = reader.metadata.get("/application_json_submission")
                            
                        application_json_submission = json.loads(application_json_submission_string)
                    else:
                        other_files.append(document)
                except errors.PdfReadError:
                    documents_error_list[index] = f"Invalid pdf file - {document.get('name')}"
                except binascii.Error:
                    documents_error_list[index] = "Invalid base64 string set as 'file'"
                except ValueError:
                    documents_error_list[index] = "Passed application.json_submission string is not a valid json"

        if documents_error_list:
            raise ValidationError(documents_error_list)

        if not application_json_submission:
            raise ValidationError("Invalid List - No main file with application.json_submission")

        ### Check if the application_json_submission has the needed keys
        ### data.caseDataSource.data.serviceId
        try:
            has_case_data_source = self.has_key(application_json_submission, "data", "caseDataSource", "data")
        except AttributeError as err:
            raise ValidationError("Invalid List - the provided application_json_submission was invalid")

        case_data_source_data = application_json_submission["data"]["caseDataSource"]["data"]
        has_service_id = case_data_source_data.get("serviceId")

        if not has_service_id:
            raise ValidationError("Invalid List - the provided application_json_submission does not have the needed key 'data.caseDataSource.data.serviceId'")


        self.application_json_submission = application_json_submission
        self.other_files = other_files
        self.has_digitall_sofia_origin = False
        self.origin_process_instance_id = None

        ### Check origin
        ### What we have:
        generation_source = case_data_source_data.get("generationSource")
        current_app.logger.debug(f"Generation Source - {generation_source}")
        if generation_source == "digitalSofia":
            origin_application = Application.query.filter(
                Application.id==case_data_source_data.get("applicationId"),
                Application.form_process_mapper.has(process_key='Process_sofiade')
            ).first()

            if origin_application:
                self.has_digitall_sofia_origin = True
                self.origin_process_instance_id = origin_application.process_instance_id
                current_app.logger.debug(f"Origin process instance - {self.origin_process_instance_id}")


    @post_load
    def make_data(self, data, **kwargs):
        data["application_json_submission"] = self.application_json_submission
        data["other_files"] = self.other_files
        data["has_digitall_sofia_origin"] = self.has_digitall_sofia_origin
        data["origin_process_instance_id"] = self.origin_process_instance_id
        return data



class ApplicationProcessingChangeAssigneesRequest(Schema):

    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        unknown = EXCLUDE
    
    assignees = fields.List(
        fields.String(), 
        required=True, 
        error_messages={
          'required': 'Missing data for required field.', 
        }
    )
    valid_groups = fields.List(fields.String(), required=True)

    @validates_schema
    def validate_users(self, data, **kwargs):
        ### Check if the person identifier exists within keycloak
        ### importing here due to circular import error
        from formsflow_api.services.external import KeycloakAdminAPIService
        keycloak_client = KeycloakAdminAPIService()

        assignees_list = data["assignees"]
        if not assignees_list:
            raise ValidationError({"assignees": "Missing data for required field"})
        assignees_error_list = {}
    
        for person_identifier in assignees_list:
            
            ### Check if it is a valid person identifier
            if not re.match("^\d{10}$",person_identifier):
                assignees_error_list[person_identifier] = f"Invalid personal identifier {person_identifier}. Incorrect format."
                continue

            ### Check if the pnobg is a user within keycloak
            url_path = f"users?username=pnobg-{person_identifier}&exact={True}"
            keycloak_user = keycloak_client.get_request(url_path)

            if not keycloak_user:
                assignees_error_list[person_identifier] = f"Invalid personal identifier {person_identifier}. No user exists within IDM."
                continue
            
            ### Check if user is within the correct group for axter
            keycloak_user = keycloak_user[0]
            # current_app.logger.debug(f"{person_identifier} - {keycloak_user}")
            reviewer_groups = keycloak_client.get_user_groups(keycloak_user["id"])

            has_valid_group = False
            for group in reviewer_groups:
                if group.get("id") in data["valid_groups"]:
                    has_valid_group = True
                    break
            
            if not has_valid_group:
                assignees_error_list[person_identifier] = f"Invalid group. Current Axter Service Account, can't access the user resource."


        if assignees_error_list:
            raise ValidationError({"assignees": assignees_error_list})