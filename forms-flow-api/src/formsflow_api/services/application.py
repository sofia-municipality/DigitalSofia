"""This exposes application service."""
import json
from datetime import datetime, timedelta
from functools import lru_cache
from http import HTTPStatus
from typing import Dict, Set

from flask import current_app
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    DRAFT_APPLICATION_STATUS,
    NEW_APPLICATION_STATUS,
    REVIEWER_GROUP,
)
from formsflow_api_utils.utils.user_context import UserContext, user_context

from formsflow_api.models import (
    Application,
    Authorization,
    AuthType,
    Draft,
    FormProcessMapper,
    DocumentStatus,
    DocumentTransaction
)
from formsflow_api.schemas import (
    AggregatedApplicationSchema,
    AggregatedApplicationsSchema,
    ApplicationSchema,
    ApplicationWithReceiptsSchema,
    FormProcessMapperSchema,
)
from formsflow_api.schemas.receipt_schemas import ReceiptSchema
from formsflow_api.services.external import BPMService
from formsflow_api.services.receipts import ReceiptService
from formsflow_api.services.overriden.formio_extended import FormioServiceExtended
from .form_process_mapper import FormProcessMapperService

application_schema = ApplicationSchema()


class ApplicationService:  # pylint: disable=too-many-public-methods
    """This class manages application service."""

    @staticmethod
    def get_start_task_payload(
        application: Application,
        mapper: FormProcessMapper,
        form_url: str,
        web_form_url: str,
    ) -> Dict:
        """Returns the payload for initiating the task."""
        return {
            "variables": {
                "applicationId": {"value": application.id},
                "formUrl": {"value": form_url},
                "webFormUrl": {"value": web_form_url},
                "formName": {"value": mapper.form_name},
                "submitterName": {"value": application.created_by},
                "submissionDate": {"value": str(application.created)},
                "tenantKey": {"value": mapper.tenant},
            }
        }

    @staticmethod
    def start_task(
        mapper: FormProcessMapper, payload: Dict, token: str, application: Application
    ) -> None:
        """Trigger bpmn workflow to create a task."""
        try:
            if mapper.process_tenant:
                camunda_start_task = BPMService.post_process_start_tenant(
                    process_key=mapper.process_key,
                    payload=payload,
                    token=token,
                    tenant_key=mapper.process_tenant,
                )
            else:
                camunda_start_task = BPMService.post_process_start(
                    process_key=mapper.process_key,
                    payload=payload,
                    token=token,
                    tenant_key=mapper.tenant,
                )
            application.update({"process_instance_id": camunda_start_task["id"]})
        except TypeError as camunda_error:
            response = {
                "message": "Camunda workflow not able to create a task",
                "error": camunda_error,
            }
            current_app.logger.critical(response)

    @staticmethod
    @user_context
    def create_application(data, token, **kwargs):
        """Create new application."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        tenant_key = user.tenant_key
        if user_id is not None:
            # for anonymous form submission
            data["created_by"] = user_id
        data["application_status"] = NEW_APPLICATION_STATUS
        mapper = FormProcessMapper.find_form_by_form_id(data["form_id"])
        if mapper is None:
            if tenant_key:
                raise PermissionError(f"Permission denied, formId - {data['form_id']}.")
            raise KeyError(f"Mapper does not exist with formId - {data['form_id']}.")
        if tenant_key is not None and mapper.tenant != tenant_key:
            raise PermissionError("Tenant authentication failed.")
        data["form_process_mapper_id"] = mapper.id
        # Function to create application in DB
        application = Application.create_from_dict(data)
        # process_instance_id in request object is usually used in Scripts
        if "process_instance_id" in data:
            application.update({"process_instance_id": data["process_instance_id"]})
        # In normal cases, it's through this else case task is being created
        else:
            form_url = data["form_url"]
            web_form_url = data.get("web_form_url", "")
            payload = ApplicationService.get_start_task_payload(
                application, mapper, form_url, web_form_url
            )

            current_app.logger.debug("BPM start_task payload")
            current_app.logger.debug(payload)

            ApplicationService.start_task(mapper, payload, token, application)
        return application, HTTPStatus.CREATED

    @staticmethod
    @lru_cache(maxsize=32)
    def get_authorised_form_list(token):
        """
        Function to get the authorized forms based on token passed.

        Used LRU cache to memoize results and parameter maxsize defines
        the no of function calls.
        """
        response = BPMService.get_auth_form_details(token=token)
        return response

    @staticmethod
    def _application_access(token: str) -> bool:
        """Checks if the user has access to all applications."""
        auth_form_details = ApplicationService.get_authorised_form_list(token=token)
        assert auth_form_details is not None
        current_app.logger.info(auth_form_details)
        auth_list = auth_form_details.get("authorizationList") or {}
        resource_list = [group["resourceId"] for group in auth_list]
        return (
            auth_form_details.get("adminGroupEnabled") is True or "*" in resource_list,
            resource_list,
        )

    @staticmethod
    @user_context
    def get_auth_applications_and_count(  # pylint: disable=too-many-arguments,too-many-locals
        page_no: int,
        limit: int,
        order_by: str,
        created_from: datetime,
        created_to: datetime,
        modified_from: datetime,
        modified_to: datetime,
        application_id: int,
        application_name: str,
        application_status: str,
        created_by: str,
        sort_order: str,
        **kwargs,
    ):
        """Get applications only from authorized groups."""
        # access, resource_list = ApplicationService._application_access(token)
        user: UserContext = kwargs["user"]
        user_name: str = user.user_name
        form_ids: Set[str] = []
        forms = Authorization.find_all_resources_authorized(
            auth_type=AuthType.APPLICATION,
            roles=user.group_or_roles,
            user_name=user.user_name,
            tenant=user.tenant_key,
        )
        for form in forms:
            form_ids.append(form.resource_id)
        (
            applications,
            get_all_applications_count,
        ) = Application.find_applications_by_auth_formids_user(
            application_id=application_id,
            application_name=application_name,
            application_status=application_status,
            created_by=created_by,
            page_no=page_no,
            limit=limit,
            order_by=order_by,
            modified_from=modified_from,
            modified_to=modified_to,
            sort_order=sort_order,
            created_from=created_from,
            created_to=created_to,
            form_ids=form_ids,
            user_name=user_name,
        )
        draft_count = Draft.get_draft_count()
        applications_with_receipts = ApplicationService.get_receipts_of_applications(applications)
        return (
            applications_with_receipts,
            get_all_applications_count,
            draft_count,
        )
    
    @staticmethod
    def get_receipts_of_applications(applications: list[Application]) -> list[dict]:
        applications_with_receipts = []
        resp_schema = ApplicationWithReceiptsSchema()
        receipt_schema = ReceiptSchema()
        receipts_service = ReceiptService()
        for application in applications:
            if application.application_status == "Готова за предоставяне":
                application_receipts = receipts_service.get_application_receipts(str(application.id), application.process_tenant)
                receipts_response = []
                receipts_response = [
                    receipt_schema.dump({
                        "_id": receipt["_id"],
                        "form": receipt["form"],
                        "name": receipt["data"]["file"][0]["name"],
                    }) for receipt in application_receipts
                ]

                application_dict = dict(application._mapping)
                application_dict["receipts"] = receipts_response
                applications_with_receipts.append(resp_schema.dump(application_dict))

            else:
                applications_with_receipts.append(resp_schema.dump(application))

        
        return applications_with_receipts

    @staticmethod
    @user_context
    def get_auth_by_application_id(application_id: int, **kwargs):
        """Get authorized Application by id."""
        user: UserContext = kwargs["user"]
        parent_form_ref = Application.find_form_parent_id_by_application_id(
            application_id=application_id
        )
        if parent_form_ref is None:
            raise BusinessException("Invalid application", HTTPStatus.BAD_REQUEST)
        application_auth = Authorization.find_resource_authorization(
            auth_type=AuthType.APPLICATION,
            roles=user.group_or_roles,
            user_name=user.user_name,
            tenant=user.tenant_key,
            resource_id=parent_form_ref,
        )
        if application_auth:
            application = Application.find_auth_by_id(application_id=application_id)
        else:
            # Reviewer lack application permissions can still have form permissions,
            # submit and view their application.
            application = Application.find_id_by_user(application_id, user.user_name)
        if application is None and user.tenant_key is not None:
            raise PermissionError(
                f"Access to application - {application_id} is denied."
            )
        return application_schema.dump(application), HTTPStatus.OK

    @staticmethod
    @user_context
    def get_all_applications_by_user(  # pylint: disable=too-many-arguments,too-many-locals
        page_no: int,
        limit: int,
        order_by: str,
        sort_order: str,
        created_from: datetime,
        created_to: datetime,
        modified_from: datetime,
        modified_to: datetime,
        created_by: str,
        application_status: str,
        application_name: str,
        application_id: int,
        **kwargs,
    ):
        """Get all applications based on user."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        applications, get_all_applications_count = Application.find_all_by_user(
            user_id=user_id,
            page_no=page_no,
            limit=limit,
            order_by=order_by,
            sort_order=sort_order,
            application_id=application_id,
            application_name=application_name,
            application_status=application_status,
            created_by=created_by,
            modified_from=modified_from,
            modified_to=modified_to,
            created_from=created_from,
            created_to=created_to,
        )
        draft_count = Draft.get_draft_count()
        return (
            application_schema.dump(applications, many=True),
            get_all_applications_count,
            draft_count,
        )

    @staticmethod
    def get_all_application_status():
        """Get all application status."""
        status_list = Application.find_all_application_status()
        status_list = [
            x.application_status
            for x in status_list
            if x.application_status != DRAFT_APPLICATION_STATUS
        ]
        current_app.logger.debug(status_list)
        return {"applicationStatus": status_list}

    @staticmethod
    def get_all_applications_form_id(form_id, page_no: int, limit: int):
        """Get all applications."""
        if page_no:
            page_no = int(page_no)
        if limit:
            limit = int(limit)

        applications = Application.find_by_form_id(
            form_id=form_id, page_no=page_no, limit=limit
        )
        return application_schema.dump(applications, many=True)

    @staticmethod
    @user_context
    def get_all_applications_form_id_user(
        form_id: str, page_no: int, limit: int, **kwargs
    ):
        """Get all applications."""
        user: UserContext = kwargs["user"]
        user_id = user.user_name
        if page_no:
            page_no = int(page_no)
        if limit:
            limit = int(limit)

        applications = Application.find_by_form_id_user(
            form_id=form_id, user_id=user_id, page_no=page_no, limit=limit
        )
        return application_schema.dump(applications, many=True)

    @staticmethod
    def get_all_applications_form_id_count(form_id: str):
        """Get application count."""
        return Application.find_all_by_form_id_count(form_id=form_id)

    @staticmethod
    @user_context
    def get_all_applications_form_id_user_count(form_id: str, **kwargs):
        """Get application count."""
        user: UserContext = kwargs["user"]
        user_id = user.user_name
        return Application.find_all_by_form_id_user_count(
            form_id=form_id, user_id=user_id
        )

    @staticmethod
    @user_context
    def get_application_by_user(application_id: int, **kwargs):
        """Get application by user id."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        application = Application.find_id_by_user(
            application_id=application_id, user_id=user_id
        )
        if application:
            return ApplicationSchema().dump(application), HTTPStatus.OK

        return ApplicationSchema().dump([]), HTTPStatus.FORBIDDEN

    @staticmethod
    @user_context
    def update_application(application_id: int, data: Dict, **kwargs):
        """Update application."""
        user: UserContext = kwargs["user"]
        data["modified_by"] = user.user_name
        application = Application.find_by_id(application_id=application_id)
        if application is None and user.tenant_key is not None:
            raise PermissionError(f"Access to application - {application_id} is denied")
        if application:
            application.update(data)
        else:
            raise BusinessException("Invalid application", HTTPStatus.BAD_REQUEST)

    @staticmethod
    def get_application_process_variables(
        application_id,
        token=None
    ):
        application = Application.query.filter_by(
            id=application_id
        ).first()

        
        if not application or not application.process_instance_id:
            current_app.logger.debug(f"Couldn't find application {application_id} or has no assigned process instance")
            return False

        return BPMService.get_process_variables(
            application.process_instance_id, 
            token=token
        )
    
    @staticmethod
    def update_message_for_application_by_status(
        status: DocumentStatus,
        transaction: DocumentTransaction
    ):
        from formsflow_api.services import FormioServiceExtended
        current_app.logger.debug("ApplicationService@update_message_for_application_by_status")
        current_app.logger.debug("1. Generate formio access token")
        form_formio_id = transaction.origin_form_formio_id
        formio_service = FormioServiceExtended()
        formio_token = formio_service.get_formio_access_token()

        current_app.logger.debug(f"2. Get transaction path for form {form_formio_id}")
        formio_form = formio_service.get_form(
            data={"form_id": form_formio_id}, 
            formio_token=formio_token
        )

        path = formio_form.get("path")
        current_app.logger.debug(f"3. Get full path - {path}")

        if not path:
            return False
        
        form_path = path.split("-", 1)[-1]
        current_app.logger.debug(f"4. Get path without the tenant_key suffix - {form_path}")

        form_path_to_message_suffix = {
            "ownersignform": "owner",
            "trusteesignform": "trustee"
        }
        

        ### If we recognize the form, a corresponding "{message_suffix}-" key will be generated
        message_suffix = form_path_to_message_suffix.get(form_path, "")
        ### AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        if message_suffix:
            message_suffix = message_suffix + ("_" if status.title == "Expired" else "-")
        # message_suffix = message_suffix + "-" if message_suffix else None
        current_app.logger.debug(f"5. Message suffix will be {message_suffix}")
        
        ### Get the correct message name, depending on status
        status_to_message_name = {
            "Rejected": "document-update",
            "Signed": "document-update",
            "Expired": "invitation_expired"
        }

        message_name = status_to_message_name.get(status.title, None)
        current_app.logger.debug(f"6. Message name will be - {message_name} - if no message name, we won't continue")

        if not message_name:
            return False
        
        status_expecting_variables = ["Rejected", "Signed"]
        form_variables = {
            "ownersignform": {
                "propertyOwnerDocumentRejected": {
                    "value": status.title == "Rejected"
                }
            }, 
            "trusteesignform": {
                "trusteeDocumentRejected": {
                    "value": status.title == "Rejected"
                }
            },
            "changeofpernamentaddress": {
                "documentRejected": {
                    "value": status.title == "Rejected"
                }
            },
            "changeofcurrentaddress": {
                "documentRejected": {
                    "value": status.title == "Rejected"
                }
            }
        }
        process_variables = {}
        if status.title in status_expecting_variables and form_path in form_variables.keys():
            process_variables = form_variables.get(form_path)
        
        current_app.logger.debug(f"7. Process variables")
        current_app.logger.debug(process_variables)
        
        current_app.logger.debug(f"8. Sending application message")
        response = ApplicationService.send_application_message(
            application_id=transaction.application_id,
            message_name=message_suffix + message_name,
            process_variables=process_variables
        )
        
        
        current_app.logger.debug(f"9. Was message sent - {response}")
        return response


    @staticmethod
    def send_application_message(
            application_id, 
            message_name: str, 
            process_variables: {} = None,
            **kwargs
        ):
        application = Application.query.filter_by(
            id=application_id
        ).first()

        if not application or not application.process_instance_id:
            current_app.logger.debug(f"Couldn't find application {application_id} or has no assigned process instance")
            return False
    
        data = {
            "messageName": message_name,
            "processInstanceId": application.process_instance_id
        }
        
        if process_variables:
            data["processVariables"] = process_variables

        response = BPMService.send_message(
            data=data,
            token=None
        )
        
        if response != True:
            current_app.logger.error(f"Couldn't send message to {application_id}")
            current_app.logger.debug(f"Message - {data}")
            return False

        current_app.logger.debug(response)
        return True

    @staticmethod
    def get_aggregated_applications(  # pylint: disable=too-many-arguments
        from_date: str,
        to_date: str,
        page_no: int,
        limit: int,
        form_name: str,
        sort_by: str,
        sort_order: str,
        order_by: str,
    ):
        """Get aggregated applications."""
        applications, get_all_metrics_count = Application.find_aggregated_applications(
            from_date=from_date,
            to_date=to_date,
            page_no=page_no,
            limit=limit,
            form_name=form_name,
            sort_by=sort_by,
            sort_order=sort_order,
            order_by=order_by,
        )

        schema = AggregatedApplicationsSchema()
        return (
            schema.dump(applications, many=True),
            get_all_metrics_count,
        )

    @staticmethod
    @user_context
    def get_applications_status_by_parent_form_id(
        parent_form_id: str,
        from_date: datetime,
        to_date: datetime,
        order_by: str,
        **kwargs,
    ):
        """Get aggregated application status by parent form id."""
        user: UserContext = kwargs["user"]
        application_status = (
            Application.find_aggregated_application_status_by_parent_form_id(
                form_id=parent_form_id,
                from_date=from_date,
                to_date=to_date,
                order_by=order_by,
            )
        )
        schema = AggregatedApplicationSchema()
        result = schema.dump(application_status, many=True)
        if user.tenant_key and len(result) == 0:
            raise PermissionError(f"Access to resource-{parent_form_id} is denied.")
        return result

    @staticmethod
    def get_applications_status_by_form_id(
        form_id: int, from_date: str, to_date: str, order_by: str
    ):
        """Get aggregated application status by form id."""
        application_status = Application.find_aggregated_application_status_by_form_id(
            form_id=form_id, from_date=from_date, to_date=to_date, order_by=order_by
        )
        schema = AggregatedApplicationSchema()
        result = schema.dump(application_status, many=True)
        return result

    @staticmethod
    @user_context
    def get_application_form_mapper_by_id(application_id: int, **kwargs):
        """Get form process mapper."""
        user: UserContext = kwargs["user"]
        tenant_key = user.tenant_key
        mapper = Application.get_form_mapper_by_application_id(
            application_id=application_id
        )
        if mapper:
            if mapper.id and tenant_key:
                FormProcessMapperService.check_tenant_authorization(
                    mapper_id=mapper.id, tenant_key=tenant_key
                )
            mapper_schema = FormProcessMapperSchema()
            return mapper_schema.dump(mapper)

        raise BusinessException("Invalid application", HTTPStatus.BAD_REQUEST)

    @staticmethod
    def get_total_application_corresponding_to_mapper_id(mapper_id: int):
        """Retrieves application count related to a mapper_id."""
        count = Application.get_total_application_corresponding_to_mapper_id(mapper_id)
        if count == 0:
            return ({"message": "No Applications found", "value": count}, HTTPStatus.OK)

        return (
            {"message": f"Total Applications found are: {count}", "value": count},
            HTTPStatus.OK,
        )

    @staticmethod
    @user_context
    def get_application_count(auth, **kwargs):
        """Retrieves the active application count."""
        user: UserContext = kwargs["user"]
        user_name = user.user_name
        form_ids: Set[str] = []
        application_count = None
        if auth.has_role([REVIEWER_GROUP]):
            forms = Authorization.find_all_resources_authorized(
                auth_type=AuthType.APPLICATION,
                roles=user.group_or_roles,
                user_name=user.user_name,
                tenant=user.tenant_key,
            )
            for form in forms:
                form_ids.append(form.resource_id)
            application_count = Application.get_auth_application_count_by_form_id_user(
                form_ids, user_name
            )
        else:
            application_count = Application.get_user_based_application_count(
                user.user_name
            )
        assert application_count is not None
        return application_count
    
    @staticmethod
    def resubmit_application(application_id: int, payload: Dict, token: str):
        """Resubmit application and update process variables."""
        mapper = ApplicationService.get_application_form_mapper_by_id(application_id)
        task_variable = json.loads(mapper.get("taskVariable"))
        form_data = payload.pop("data", None)
        process_variables = {"isResubmit": {"value": False}}
        if task_variable and form_data:
            task_keys = [val["key"] for val in task_variable]
            process_variables.update(
                {
                    key: {"value": form_data[key]}
                    for key in task_keys
                    if key in form_data
                }
            )
        payload["processVariables"] = process_variables
        ApplicationService.update_application(application_id, {"is_resubmit": False})
        response = BPMService.send_message(data=payload, token=token)
        if not response:
            raise BusinessException(
                "No process definition or execution matches the parameters.",
                HTTPStatus.BAD_REQUEST,
            )

    @staticmethod
    def check_user_for_unfinished_applications():
        """Check if user has unfinished applications."""

        applications = Application.filter_applications_by_specific_statuses()

        current_app.logger.debug("Checking for unfinished applications: %s", applications)

        if applications:
            raise BusinessException(
                {
                    "error": "User has unfinished application",
                    "status": HTTPStatus.CONFLICT.phrase
                },
                HTTPStatus.CONFLICT
            )

    @staticmethod
    @user_context
    def get_all_user_applications(**kwargs) -> list[Application]:
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        user_applications = Application.find_all_by_user_without_pagination(user_id)
        return user_applications
    
    @staticmethod
    def get_all_user_applications_count(user_id: str) -> int:
        count = Application.select_all_user_applications_count(user_id)
        return count
    
    @staticmethod
    def get_user_not_draft_applications_count(user_id: str) -> int:
        count = Application.select_all_user_applications_count_without_draft(user_id)
        return count

    @staticmethod
    def delete_unactive_draft_applications():
        current_time = datetime.utcnow()
        expired = current_time - timedelta(days=30)
        expired_draft_applications = Application.select_all_old_draft_applications(expired)
        for application in expired_draft_applications:
            current_app.logger.debug(f"Start deleting of application {application.id}")
            draft = Draft.get_by_application_id(application.id)
            draft.delete()
            application.delete()

    @staticmethod
    def check_user_have_signed_documents(user_identifier: str) -> bool:
        """This method is related to automatic deletion of the users."""
        related_formio_form_paths_and_data_keys = (
            ("sofia-generated-files", "data.userId__eq"),
        )
        formio_client = FormioServiceExtended()
        formio_token = formio_client.generate_formio_token()
        for path, property in related_formio_form_paths_and_data_keys:
            submissions, _ = formio_client.get_submissions(
                form_path=path,
                formio_token=formio_token,
                params={property: user_identifier, "data.signatureSource__eq": "digitalSofia"}
            )
            if len(submissions) != 0:
                return True
            
        return False
    
    @staticmethod
    def get_user_file_submissions(user_identifier: str) -> list[dict]:
        """This method returns all file submissions for an user."""
        formio_client = FormioServiceExtended()
        formio_token = formio_client.generate_formio_token()
        submissions = []
        for i in range(6):
            paginated_submissions, status_code = formio_client.get_submissions(
                form_path="sofia-generated-files",
                formio_token=formio_token,
                params={"data.userId__eq": user_identifier},
                limit=20,
                skip=i*20
            )

            if isinstance(paginated_submissions, str) or isinstance(paginated_submissions, bytes) or len(paginated_submissions) == 0:
                break

            submissions = submissions + paginated_submissions

        return submissions
