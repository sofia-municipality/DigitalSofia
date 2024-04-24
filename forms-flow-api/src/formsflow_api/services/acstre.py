
import os, datetime, base64, mimetypes, json, re
from http import HTTPStatus
from io import BytesIO
from formsflow_api.services.external import BPMService
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.services import ApplicationService
from formsflow_api.models import Application, OtherFile
from flask import current_app
from werkzeug.datastructures import FileStorage
from formsflow_api.schemas import (
    ApplicationSchema
)

DEFAULT_ACSTRE_STATUS = None
INITIAL_ACSTRE_STATUS = "Получено заявление"
COMPLETE_ACSTRE_STATUS = "Получен резултат"
DENIED_ACSTRE_STATUS = "Отказана услуга"


EU_STATUSES = [
    {
        "code": "11",
        "name": "Получено заявление",
        "status": "not-ready"
    },
    {
        "code": "11",
        "name": "New",
        "status": "not-ready"
    },
    {
        "code": "12",
        "name": "Очаква плащане",
        "status": "not-ready"
    },
    {
        "code": "13",
        "name": "Отказано плащане",
        "status": "not-ready"
    },
    {
        "code": "15",
        "name": "Платена",
        "status": "not-ready"
    },
    {
        "code": "16",
        "name": "Очаква събиране на данни",
        "status": "not-ready"
    },
    {
        "code": "17",
        "name": "Очаква генериране на резултат",
        "status": "not-ready"
    },
    {
        "code": "18",
        "name": "Очаква подписване",
        "status": "not-ready"
    },
    {
        "code": "21",
        "name": "Подписано ",
        "status": "not-ready"
    },
    {
        "code": "25",
        "name": "Готова за предоставяне",
        "status": "ready"
    },
    {
        "code": "26",
        "name": "Готова за съгласуване",
        "status": "needs-agreement"
    },
    {
        "code": "27",
        "name": "Получен резултат",
        "status": "completed"
    },
    {
        "code": "97",
        "name": "Отказана услуга",
        "status": "denied"
    }
]


class AcstreService:

    def __init__(self):
        self.formio_client = FormioServiceExtended()
        from formsflow_api.services import OtherFileService
        self.other_file_service = OtherFileService()
        self.bpm_service = BPMService()
        self.base_api_url = current_app.config.get("FORMSFLOW_API_URL")

    def set_application_origin_process_instance(self, application_id, origin_process_instance_id):
        application = Application.query.filter_by(id=application_id).first()
        if not application:
            return None

        response = self.bpm_service.create_process_variable(
            process_instance_id=application.process_instance_id,
            token=None,
            variable_name="originProcessInstanceId",
            payload={
                "value": origin_process_instance_id,
                "type": "String"
            }
        )
        return response


    def get_application_origin_process_instance_id(self, application_id) -> str:
        application = Application.query.filter_by(id=application_id).first()
        if not application:
            return None
        
        result = self.bpm_service.get_process_instance_variable(
            application.process_instance_id, 
            token=None, 
            variable_name="originProcessInstanceId"
        )

        return result.get("value") if result else None 


    def handle_payment_status_change(
            self,
            application_id:int,
            is_paid:bool,
            is_final:bool = None
        ):
        process_instance_id = self.get_application_origin_process_instance_id(application_id=application_id)
        if not process_instance_id:
            current_app.logger.debug(f"Couldn't find origin process instance")
            return

        
        current_app.logger.debug(f"Process Instance - {process_instance_id}")
        process_variables = {
            "isPaymentPaid": {
                "value": is_paid
            }
        }

        if is_final is not None:
            process_variables["isFinalDecision"] = {
                "value": is_final
            }

        payload_for_origin = {
            "messageName": "payment_status_update",
            "processInstanceId": process_instance_id,
            "processVariables": process_variables
        }

        self.bpm_service.send_message(payload_for_origin, token=None)



    def send_payment_generated_message_to_bpm(
            self, 
            application_id: int, 
            payment_access_code: str, 
            payment_access_code_deadline_date: str, 
            payment_sum: str
        ):
        current_app.logger.info("AcstreService@send_payment_generated_message_to_bpm")
        current_app.logger.debug(f"Application ID - {application_id}")

        process_instance_id = self.get_application_origin_process_instance_id(application_id=application_id)
        if not process_instance_id:
            current_app.logger.debug(f"Couldn't find origin process instance")
            return


        current_app.logger.debug(f"Process Instance - {process_instance_id}")
        payload = {
            "messageName": "payment_generated",
            "processInstanceId": process_instance_id,
            "processVariables": {
                "paymentAccessCode": {
                    "value": payment_access_code
                },
                "paymentAccessCodeDeadlineDate": {
                    "value": payment_access_code_deadline_date
                },
                "paymentSum": {
                    "value": payment_sum
                }
            }
        }
        self.bpm_service.send_message(payload, token=None)

    
    def send_payment_status_update_message(self, application_id, payment_status: str):
        current_app.logger.info("AcstreService@send_payment_status_update_message")
        current_app.logger.debug(f"Application ID - {application_id}")
        application = Application.query.filter_by(id=application_id).first()
        if not application:
            return None
        
        # payload = {
        #     "messageName": "payment_status_update",
        #     "processInstanceId": application.process_instace_id,  ### TODO
        #     "applicationStatus": payment_status,
        # }
        # self.bpm_service.send_message(payload, token=None)

        current_app.logger.debug(f"Payment Status - {payment_status}")
        payload_for_origin = None
        if payment_status not in ["paid", "canceled", "expired", "suspended"]:
            return None

        ### Notify original application
        origin_process_instance_id = self.get_application_origin_process_instance_id(application_id=application_id)
        if origin_process_instance_id:
            is_paid = True if payment_status == "paid" else False

            current_app.logger.debug(f"Process Instance - {origin_process_instance_id}")
            payload_for_origin = {
                "messageName": "payment_status_update",
                "processInstanceId": origin_process_instance_id, ### TODO
                "processVariables": {
                    "isPaymentPaid": {
                        "value": is_paid
                    }
                }
            }

        
        current_app.logger.debug("Formio Payload creation")
        formio_payload = []
        if payment_status == "paid":
            formio_payload.append(
                self.formio_client.generate_rfc6902_object(
                    "/data/couldOfficialChangePaymentStatus", False
                )
            )
            formio_payload.append(
                self.formio_client.generate_rfc6902_object(
                    "/data/paymentStatus1", payment_status
                )
            )

        formio_payload.append(
            self.formio_client.generate_rfc6902_object(
                "/data/paymentStatus", payment_status
            )
        )


        ### The resource which should be updated is that of the latest task
        current_app.logger.debug("Get latest process instance task")
        tasks = self.bpm_service.get_process_instance_tasks(
            process_instance_id=application.process_instance_id, 
            token=None
        )
        if not tasks:
            current_app.logger.error("No tasks attached, wont send formio update")
            return

        latest_task = tasks[0]
        latest_task_variables = self.bpm_service.get_task_variables(
            task_id=latest_task.get("id"), 
            token=None
        )
        form_url = latest_task_variables.get("taskFormUrl")
        form_url = form_url.get("value")
        current_app.logger.debug(f"Form url - {form_url}")

        ### Match form id and submission id
        match = re.match(r".+/form/(\w+)\/submission\/(\w+)", form_url)
        if not match or len(match.groups()) != 2:
            current_app.logger.error("Invalid matches group")
            return
        
        form_id = match.group(1)
        resource_id = match.group(2)
        
        current_app.logger.debug(f"Update formio resource {form_id}/submission/{resource_id}")

        if payload_for_origin:
            self.bpm_service.send_message(payload_for_origin, token=None)

        self.formio_client.update_formio_resource(
            form_id=form_id,
            resource_id=resource_id,
            data=formio_payload
        )


    def send_document_processed_message(self, 
            application_id: int, 
            is_request_approved: bool,
            certificate_url: str
        ):
        current_app.logger.info("AcstreService@send_document_processed_message")
        current_app.logger.debug(f"Application ID - {application_id}")

        process_instance_id = self.get_application_origin_process_instance_id(application_id=application_id)
        if not process_instance_id:
            current_app.logger.debug(f"Couldn't find origin process instance")
            return

        current_app.logger.debug(f"Process Instance - {process_instance_id}")
        payload = {
            "messageName": "request_status_update",
            "processInstanceId": process_instance_id, ### TODO
            "processVariables": {
                "isRequestApproved": {
                    "value": "true" if is_request_approved else "false" ### TODO
                },
                "resultingCertificateUrl": {
                    "value": certificate_url ### TODO
                }
            }
        }
        self.bpm_service.send_message(payload, token=None)


    def document_processed_application(
            self, 
            user_id,
            application_id, 
            status, 
            description, 
            documents
        ):
        current_app.logger.info("AcstreService@document_processed_application")
        ### 1. Get application
        application = Application.query.filter(Application.id==application_id).first()
        
        current_app.logger.debug("1. Check application latest_form_id and submission_id")
        if not application or not application.latest_form_id or not application.submission_id:
            return None

        current_app.logger.debug(f"application.latest_form_id - {application.latest_form_id}")
        current_app.logger.debug(f"application.submission_id - {application.submission_id}")

        
        ### 2. Gather form.io values
        data = []
        ### 2.1. Status
        is_completed = status == "completed"
        application_status = COMPLETE_ACSTRE_STATUS if is_completed else DENIED_ACSTRE_STATUS
        data.append(
            self.formio_client.generate_rfc6902_object(
                "/data/applicationStatus", application_status
            )
        )
        current_app.logger.debug(f"2. Adding status '{application_status}' to /data/applicationStatus")

        ### 2.2. Description
        data.append(
            self.formio_client.generate_rfc6902_object(
                "/data/acstreData/description", description
            )
        )
        current_app.logger.debug(f"3. Adding description '{description}' to /data/acstreData/description")

        ### 2.3. Documents
        ### 2.3.1. Check if there are files at all
        if documents:
            ### 2.3.2. If there are files, save them in a datetime subfolder in application folder
            other_files = self.get_application_root_files(application_id=application_id)

            request_datetime = datetime.datetime.now()
            datetime_folder = request_datetime.strftime("%Y-%m-%d %H-%M-%S")
            additional_path = f"{application_id}/{datetime_folder}/"
            
            for file in documents:
                base64_string = file["file"]
                binary_data = base64.b64decode(base64_string)

                
                stream = BytesIO(binary_data)

                
                mime_type, encoding = mimetypes.guess_type(file["name"])
                
                file = FileStorage(
                    stream=stream, 
                    filename=file["name"], 
                    content_type=mime_type
                )
                
                other_file_model = self.other_file_service.save_file(
                    user_id=user_id,
                    file=file,
                    application_id=application_id,
                    additional_path=additional_path,
                    created_at=request_datetime
                )

                other_files.append(
                    {
                        "url": other_file_model.file_url,
                        "name": other_file_model.file_name,
                        "size": other_file_model.file_size
                    }
                )
                
            current_app.logger.debug(f"4. Adding other files list")
            current_app.logger.debug(other_files)
            data.append(
                self.formio_client.generate_rfc6902_object(
                    "/data/otherFiles", other_files
                )
            )
        ### 2.3.3. Get files from 
        ### 3. Update formio
        current_app.logger.debug("5. Updating formio resource")
        response = self.formio_client.update_formio_resource(
            form_id=application.latest_form_id,
            resource_id=application.submission_id,
            data=data
        )
        
        ### 4. Update Camunda
        modifications =  {
            'applicationStatus': {
                'type': 'String', 
                'value': application_status
            }
        }

        self.bpm_service.update_process_variables(
            application.process_instance_id, 
            token=None,
            modifications=modifications
        )

        ### 5. Notify origin process instance
        self.send_document_processed_message(
            application_id=application_id,
            is_request_approved=is_completed,
            certificate_url=other_file_model.file_url
        )

        return response


    def set_application_assignees(self, application_id, assignees: list = []):
        ### Set application assignees
        if not assignees:
            return None

        current_app.logger.info("AcstreService@complete_application")
        ### Get application
        application = Application.query.filter(Application.id==application_id).first()

        if not application or not application.process_instance_id:
            return None
        
        ### Init service
        current_app.logger.debug(application.process_instance_id)
        tasks = self.bpm_service.get_process_instance_tasks(
            process_instance_id=application.process_instance_id,
            token=None
        )

        ### Get task
        task_id = tasks[0]["id"]
        identity_links = self.bpm_service.get_identity_links(
            task_id,
            token=None
        )

        ### Format person identifiers, to add pnobg-
        ### ["pnobg-XXXXXXXXXX", "pnobg-XXXXXXXXXX"]
        formatted_assignees = list(map(lambda person_identifier: f"pnobg-{person_identifier}", assignees))

        ### Handle identity links
        if identity_links:
            to_remove = []
            for identity in identity_links:
                user_id = identity["userId"]

                if user_id not in assignees and identity["type"] != "assignee":
                    to_remove.append(user_id)
                elif user_id in assignees:
                    formatted_assignees.remove(user_id)

                ## Remove old assignee
                if identity["type"] == "assignee":
                    self.bpm_service.delete_identity_links(task_id, token=None, data={"userId":user_id, "groupId": None, "type":"assignee"})

            current_app.logger.debug("Candidates to remove")
            current_app.logger.debug(to_remove)
            for assignee_to_remove in to_remove:
                self.bpm_service.delete_identity_links(
                    task_id, 
                    token=None, 
                    data={
                        "userId": assignee_to_remove,
                        "groupId": None,
                        "type": "candidate"
                    }
                )


            current_app.logger.debug("Candidates to add")
            current_app.logger.debug(formatted_assignees)
            for assignee_to_add in formatted_assignees:
                self.bpm_service.post_identity_links(
                    task_id, 
                    token=None, 
                    data={
                        "userId": assignee_to_add,
                        "groupId": None,
                        "type": "candidate"
                    }
                )

        else:
        ### No Identity links for the task, lets add them
            for assignee in formatted_assignees:
                self.bpm_service.post_identity_links(
                    task_id, 
                    token=None, 
                    data={
                        "userId": f"{assignee}",
                        "groupId": None,
                        "type": "candidate"
                    }
                )

        modifications = {
            'assignedCandidateUsers': {
                'type': 'Object', 
                'value': json.dumps(formatted_assignees), 
                'valueInfo': {
                    'objectTypeName': 'java.util.ArrayList', 
                    'serializationDataFormat': 'application/json'
                }
            }
        }
        
        ### If only one assignee
        if len(formatted_assignees) == 1:
            assignee = formatted_assignees[0]
            self.bpm_service.post_identity_links(
                task_id, 
                token=None, 
                data={
                    "userId": assignee,
                    "groupId": None,
                    "type": "assignee"
                }
            )
            modifications["assignedUser"] = {
                'type': 'string',
                "value": assignee
            }
        else:
            modifications["assignedUser"] = {
                "type": "string",
                "value": ""
            }

        ### Update ${assignedCandidateUsers}
        self.bpm_service.update_process_variables(
            application.process_instance_id, 
            token=None,
            modifications=modifications
        )


    def change_application_assignees(self, application_id, assignees: list = []):
        if not assignees:
            return None
        
        current_app.logger.info("AcstreService@complete_application")
        application = Application.query.filter(Application.id==application_id).first()

        if not application or not application.latest_form_id or not application.submission_id:
            return None
        current_app.logger.debug(application.latest_form_id)
        current_app.logger.debug(application.submission_id)

        if not application.process_instance_id:
            return None


    ### TODO: Move to application service
    def complete_application(self, application_id, tenant_key, request_data):
        current_app.logger.info("AcstreService@complete_application")
        application = Application.query.filter(Application.id==application_id).first()

        if not application or not application.latest_form_id or not application.submission_id:
            return None
        current_app.logger.debug(application.latest_form_id)
        current_app.logger.debug(application.submission_id)

        ### TODO: Update Camunda process

        ### Set formio status
        data = []
        data.append(
            self.formio_client.generate_rfc6902_object(
                "/data/applicationStatus", COMPLETE_ACSTRE_STATUS
            )
        )
        submission = self.formio_client.update_formio_resource(
            form_id=application.latest_form_id,
            resource_id=application.submission_id,
            data=data
        )

        ### Set model status
        application.application_status = COMPLETE_ACSTRE_STATUS
        application.save()

        submission_data = submission.get("data")
        acstre_data = submission_data.get("acsterData")
        reference_id = submission_data.get("businessKey")
        documents = self.get_application_root_files(application_id)

        return {
            "status": "completed",
            "description": acstre_data.get("description"),
            "documents": documents,
            "externalId": acstre_data.get("externalId"),
            "taxAmount": acstre_data.get("taxAmount"),
            "paymentTill": acstre_data.get("paymentTill")
        }

    
    def add_other_files_to_application(
        self, 
        application_id,
        other_files : dict
    ):
        current_app.logger.info("AcstreService@add_other_files_to_application")
        current_app.logger.debug(f"1. Getting application {application_id}")
        application = Application.query.filter(Application.id==application_id).first()

        
        current_app.logger.debug("2. Check application latest_form_id and submission_id")
        if not application or not application.latest_form_id or not application.submission_id:
            return None

        current_app.logger.debug(f"application.latest_form_id - {application.latest_form_id}")
        current_app.logger.debug(f"application.submission_id - {application.submission_id}")

        current_app.logger.debug("3. Add other files to application")
        data = [
            {
                "op": "add",
                "path": "/data/otherFiles",
                "value": other_files
            }
        ]

        
        update_formio_response = self.formio_client.update_formio_resource(
            form_id=application.latest_form_id,
            resource_id=application.submission_id,
            data=data
        )
        
        current_app.logger.debug(update_formio_response)
        # submission_data = submission.get("data")
        pass


    def get_application_status(self, application_id, tenant_key):
        current_app.logger.info("AcstreService@get_application_status")
        current_app.logger.debug(f"1. Getting application {application_id}")
        application = Application.query.filter(Application.id==application_id).first()

        current_app.logger.debug("2. Check application latest_form_id and submission_id")
        if not application or not application.latest_form_id or not application.submission_id or not application.process_instance_id:
            return None

        application_status_bpm = self.bpm_service.get_process_instance_variable(
            process_instance_id=application.process_instance_id,
            token=None,
            variable_name="applicationStatus"
        )

        if not application_status_bpm:
            return None
        
        current_app.logger.debug("3. Generate formio access token")
        formio_token = self.formio_client.get_formio_access_token()

        current_app.logger.debug("4. Get submission")
        submission = self.formio_client.debug_get_submission(
            data={
                "form_id": application.latest_form_id,
                "sub_id": application.submission_id
            },
            formio_token=formio_token
        )

        submission_data = submission.get("data")
        application_status = application_status_bpm.get("value",)
        current_app.logger.debug(f"5. Application status - {application_status}")

        for valid_config in EU_STATUSES:
            current_app.logger.debug(f'|{valid_config.get("name")}| == |{application_status}|')
            if valid_config.get("name") == application_status:

                current_app.logger.debug("6. Get acstre data")
                acstre_data = submission_data.get("acstreData")
                current_app.logger.debug(acstre_data)
                if acstre_data:
                    acstre_status = valid_config.get("status", DEFAULT_ACSTRE_STATUS)

                    documents = []
                    if acstre_status in ["ready", "denied", "needs-agreement"]:
                        documents = self.get_application_root_files(application_id)

                    return {
                        "status": acstre_status,
                        "description": acstre_data.get("description"),
                        "documents": documents,
                        "externalId": acstre_data.get("externalId"),
                        "taxAmount": acstre_data.get("taxAmount"),
                        "paymentTill": acstre_data.get("paymentTill")
                    }

        return {
            "status": DEFAULT_ACSTRE_STATUS,
            "description": None,
            "documents": [],
            "externalId": None,
            "taxAmount": None,
            "paymentTill": None
        }


    def get_application_root_files(self, application_id) -> list[dict]:
        application_directory = self.other_file_service.save_path + f"{application_id}"
        
        if not os.path.exists(application_directory):
            current_app.logger.error(f"No application directory exist {application_directory}")
            return []

        file_hashes = [f.name for f in os.scandir(application_directory) if f.is_file()]
        other_files = self.other_file_service.get_files_by_hash(file_hashes)

        to_return = []
        for other_file_model in other_files:
            to_return.append(
                {
                    "url": other_file_model.file_url,
                    "name": other_file_model.file_name,
                    "size": other_file_model.file_size
                }
            )

        return to_return

    def get_application_files(self, application_id):
        ### TODO: Get files based on directory, not on db record
        ### Construct directory
        application_directory = self.other_file_service.save_path + f"{application_id}"

        ####
        # folder_names = [f.name for f in os.scandir(application_directory) if f.is_dir()]
        file_hashes = [f.name for f in os.scandir(application_directory) if f.is_file()]

        # if folder_names:
        #     sorted_folder_names = sorted(folder_names, key=lambda x: datetime.datetime.strptime(x, "%Y-%m-%d %H-%M-%S"))
        #     latest_folder_name = sorted_folder_names[0]
        #     file_hashes.extend([f.name for f in os.scandir(application_directory + f"/{latest_folder_name}") if f.is_file()])

        other_files = self.other_file_service.get_files_by_hash(file_hashes)

        to_return = []
        for other_file_model in other_files:
            to_return.append(
                {
                    "url": other_file_model.file_url,
                    "name": other_file_model.file_name,
                    "size": other_file_model.file_size
                }
            )

        return to_return
    
    def create_application_on_submission_data(
            self, 
            tenant_key, 
            submission_json, 
            externalId:str, 
            hasPayment:bool, 
            taxAmount:str, 
            paymentTill: str,
            assignees:list = []
        ):

        current_app.logger.info("AcstreService@create_application_on_submission_data")
        current_app.logger.debug("1. Getting application service ID data.caseDataSource.data.serviceId")
        if not submission_json['data']['caseDataSource']['data']['serviceId']:
            return False

        service_id = submission_json['data']['caseDataSource']['data']['serviceId']
        service_id_and_tenant = tenant_key + "-" + str(service_id)
        current_app.logger.debug(f"2. Tenant and service_id - {service_id_and_tenant}")

        current_app.logger.debug("3. Create formio access token")  
        formio_token = self.formio_client.get_formio_access_token()

        current_app.logger.debug("4. Getting form id by path {service_id_and_tenant}/forma/request")
        form_id = self.formio_client.fetch_form_id_by_path(service_id_and_tenant + '/forma/request', formio_token)

        current_app.logger.debug("5. Creating submission data")
        submission_data = submission_json['data']
        submission_data["applicationId"] = ""
        submission_data["serviceId"] = service_id
        submission_data["serviceSupplierId"] = submission_json['data']['caseDataSource']['data'][
            'serviceSupplierId']
        submission_data["serviceSupplierName"] = submission_json['data']['caseDataSource']['data'][
            'serviceSupplierName']
        submission_data["applicationStatus"] = INITIAL_ACSTRE_STATUS
        submission_data["acstreData"] = {
                "externalId": externalId,
                "hasPayment": hasPayment,
                "taxAmount": taxAmount,
                "paymentTill": paymentTill
            }

        current_app.logger.debug("6. Creating formio data")
        formio_data = {
            "formId": form_id,
            "data": submission_data
        }

        current_app.logger.debug(f"7. Posting submission to formio form {form_id}")
        submission = self.formio_client.post_submission(data=formio_data, formio_token=formio_token)

        current_app.logger.debug(f"8. Creating application data")
        application_schema = ApplicationSchema()
        application_data = application_schema.load({
            "formId": form_id,
            "submissionId": submission['_id'],
            "formUrl": current_app.config.get("FORMIO_WEB_URL") + "/form/" + form_id + "/submission/" +
                        submission[
                            '_id'],
            "webFormUrl": current_app.config.get("WEB_BASE_URL") + "/form/" + form_id + "/submission/" +
                            submission['_id'],

        })
        # current_app.logger.info(application_data)

        current_app.logger.debug("9. Create application")
        application, status = ApplicationService.create_application(
            data=application_data, token=None
        )
        response = application_schema.dump(application)

        ### HERE
        data = []
        data.append(
            self.formio_client.generate_rfc6902_object(
                "/data/originalApplicationId", application.id
            )
        )
        submission = self.formio_client.update_formio_resource(
            form_id=application.latest_form_id,
            resource_id=application.submission_id,
            data=data
        )

        current_app.logger.debug("10. Set assignees")

        return response