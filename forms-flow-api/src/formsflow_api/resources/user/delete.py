from http import HTTPStatus
import traceback
import re

from flask import current_app
from flask_restx import Resource

from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import cors_preflight, auth, user_context, UserContext
from formsflow_api.services import (
    ApplicationService,
    DocumentsService,
    ApplicationHistoryService,
    ObligationService,
)
from formsflow_api.services.external import BPMService, KeycloakAdminAPIService
from formsflow_api.resources.user.namespace import API
from formsflow_api.services.overriden import FormioServiceExtended
from formsflow_api.models import (
    FormProcessMapper,
    MateusPaymentGroup,
    MateusPaymentRequest,
)
from formsflow_api.utils import validate_person_identifier
from formsflow_api.services.login_event_service import LoginEventService


@cors_preflight("GET, DELETE, OPTIONS")
@API.route(
    "/delete",
    methods=["GET", "DELETE", "OPTIONS"],
)
class UserDelete(Resource):
    @classmethod
    @auth.require
    @user_context
    @API.doc()
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        409,
        "CONFLICT:- Not permitted to delete user.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get(cls, **kwargs):
        """Resource to check if we can delete keycloak user."""
        try:
            user: UserContext = kwargs["user"]
            person_identifier = validate_person_identifier(
                user.token_info["personIdentifier"]
            )

            # Check if user has any unfinished obligations
            data, status = ObligationService.verify_payment_status(person_identifier)

            if data:
                raise BusinessException(
                    {
                        "key": data.get("key"),
                        "error": data.get("message"),
                        "status": HTTPStatus.CONFLICT.phrase,
                    },
                    HTTPStatus.CONFLICT,
                )

            # Check if user has any unfinished applications
            ApplicationService.check_user_for_unfinished_applications()

            return {}, HTTPStatus.OK
        except BusinessException as err:
            return err.error, err.status_code
        except BaseException as error:
            response, status = {
                "error": "Something went wrong while checking user for unfinished applications.",
                "status": HTTPStatus.BAD_REQUEST.phrase,
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.debug(response)
            current_app.logger.error(error)

            return response, status

    @classmethod
    @auth.require
    @API.doc()
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        409,
        "CONFLICT:- Not permitted to delete user.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    @user_context
    def delete(cls, **kwargs):
        """Resource to delete keycloak user."""
        try:
            user: UserContext = kwargs["user"]
            tenant_key = user.tenant_key
            person_identifier = user.token_info["personIdentifier"]
            match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

            if not match:
                raise BusinessException(
                    "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
                )

            person_identifier = match[0]

            # # Check again if there are any unfinished applications
            # current_app.logger.debug(
            #     "Check if there any unfinished applications before delete!"
            # )
            # ApplicationService.check_user_for_unfinished_applications()

            # # Check again if user has any unfinished obligations
            # current_app.logger.debug(
            #     "Check if there any unfinished payments before delete!"
            # )
            # data, status = ObligationService.verify_payment_status(person_identifier)

            # if data:
            #     raise BusinessException(
            #         {
            #             "key": data.get("key"),
            #             "error": data.get("message"),
            #             "status": HTTPStatus.CONFLICT.phrase,
            #         },
            #         HTTPStatus.CONFLICT,
            #     )

            related_application_ids = []
            formio_client = FormioServiceExtended()

            # Get all user applications
            current_app.logger.debug("Getting all user applications")
            all_user_applications = ApplicationService.get_all_user_applications()
            for application in all_user_applications:
                current_app.logger.debug(
                    f"Start delete process for application {application.id}"
                )
                process_instance_id = application.process_instance_id
                if process_instance_id:
                    # Get related application ids
                    current_app.logger.debug("Getting related applications ids")
                    process_variables = BPMService.get_process_instance(
                        process_instance_id
                    )
                    if process_variables:
                        if owner_application_id_dict := process_variables.get(
                            "ownerFormApplicationId"
                        ):
                            if owner_application_id := owner_application_id_dict.get(
                                "value"
                            ):
                                related_application_ids.append(owner_application_id)

                        if trustee_application_id_dict := process_variables.get(
                            "trusteeFormApplicationId"
                        ):
                            if trustee_application_id := trustee_application_id_dict.get(
                                "value"
                            ):
                                related_application_ids.append(trustee_application_id)

                        # Delete process instance
                        current_app.logger.debug("Delete process instance")
                        BPMService.delete_process_instance(process_instance_id, None)

                # Delete formio application resource
                if application.submission_id:
                    current_app.logger.debug("Delete application submission id")
                    formio_path = application.form_process_mapper.form_path

                    current_app.logger.debug(formio_path)
                    formio_client.delete_submission_formio(
                        formio_path, formio_submission_id=application.submission_id
                    )

                # Delete generated files for specific application
                file_formio_path = current_app.config.get("FORMIO_FILE_RESOURCE_PATH")
                file_formio_path = f"{tenant_key}-{file_formio_path}"
                current_app.logger.debug(
                    f"Deleting from formio file resources - {file_formio_path}"
                )
                application_ids_to_delete = [application.id] + related_application_ids
                if file_formio_path:
                    formio_token = formio_client.get_formio_access_token()
                    application_ids_to_delete_query_param = ",".join(
                        map(str, application_ids_to_delete)
                    )

                    related_file_submissions = formio_client.get_all_submissions(
                        file_formio_path,
                        formio_token=formio_token,
                        options=f"data.applicationId__in={application_ids_to_delete_query_param}",
                    )

                    document_service_client = DocumentsService()
                    for file_submission in related_file_submissions:
                        file_id = file_submission.get("_id")

                        if file_id:
                            document_transaction = (
                                document_service_client.get_document_by_submission_id(
                                    formio_id=file_id
                                )
                            )
                            if document_transaction:
                                document_transaction.delete()

                            # Delete submission in formio
                            current_app.logger.debug(
                                f"Deleting formio file resource with id - {file_id}"
                            )
                            formio_client.delete_submission_formio(
                                file_formio_path, file_id
                            )

                ### 8. Delete related formio applications
                ### 8.1. Get related form paths
                ### NOTE: Form paths can change, this is the initial part of the form paths
                related_formio_form_paths = [
                    "ownersignform",
                    "trusteesignform",
                    "signitureform",
                ]
                current_app.logger.debug(
                    f"Deleting applications in related formio forms"
                )
                for related_formio_form_path in related_formio_form_paths:
                    form_process_mappers = (
                        FormProcessMapper.query.filter(
                            FormProcessMapper.form_path.like(
                                related_formio_form_path + "%"
                            ),
                            FormProcessMapper.deleted == False,
                        )
                        .distinct(FormProcessMapper.form_id)
                        .all()
                    )
                    formio_token = formio_client.get_formio_access_token()
                    for mapper in form_process_mappers:
                        current_app.logger.debug(
                            f"Deleting submissions from related form path - {mapper.form_path}"
                        )

                        related_submissions = formio_client.get_all_submissions(
                            mapper.form_path,
                            formio_token=formio_token,
                            options=f"data.applicationId={application.id}",
                        )

                        for submission in related_submissions:
                            submission_id = submission.get("_id")
                            current_app.logger.debug(
                                f"Deleting submission - {submission_id}"
                            )
                            formio_client.delete_submission_formio(
                                mapper.form_path, submission_id
                            )

                # Delete application from our db
                drafts_to_delete = application.draft
                if drafts_to_delete:
                    for draft in drafts_to_delete:
                        current_app.logger.debug(f"Delete draft {draft.id}")
                        draft.delete()

                application.delete()

                # Delete application history
                current_app.logger.debug(f"Delete application history {application.id}")
                ApplicationHistoryService.delete_application_history_by_application_id(
                    application.id
                )

            # Delete payment related data
            current_app.logger.debug("Start delete of payment related data")

            user_payment_groups_query = (
                MateusPaymentGroup.find_all_by_personal_identifier_query(
                    person_identifier
                )
            )
            user_payment_groups: list[MateusPaymentGroup] = (
                MateusPaymentGroup.find_all_by_personal_identifier(
                    user_payment_groups_query
                )
            )
            current_app.logger.debug(user_payment_groups)

            user_payment_groups_ids = [
                payment_group.id for payment_group in user_payment_groups
            ]

            requests_deleted_count = MateusPaymentRequest.delete_by_payment_groups_ids(
                user_payment_groups_ids
            )
            current_app.logger.debug("Deleted payment requests count:")
            current_app.logger.debug(requests_deleted_count)

            groups_deleted_count = MateusPaymentGroup.delete_by_personal_identifier(
                user_payment_groups_query
            )
            current_app.logger.debug("Deleted payment groups count:")
            current_app.logger.debug(groups_deleted_count)

            # Delete user files
            submissions = ApplicationService.get_user_file_submissions(user.token_info["personIdentifier"])
            generated_formio_token = formio_client.generate_formio_token()
            files_form_id = formio_client.fetch_form_id_by_path("sofia-generated-files", generated_formio_token)
            for submission in submissions:
                current_app.logger.debug(f"Delete submission with id {submission['_id']}")
                formio_client.delete_submission_formio("sofia-generated-files", submission["_id"])

            # Delete user login event
            LoginEventService.delete_user_login_event(person_identifier)

            # Delete Keycloak user
            current_app.logger.debug("Delete Keycloak user")
            keycloak_client = KeycloakAdminAPIService()
            keycloak_client.delete_user_by_bearer(user.bearer_token)

            return {"ok": "ok"}, HTTPStatus.OK
        except BusinessException as err:
            return err.error, err.status_code
        except BaseException as error:
            response, status = {
                "error": "Something went wrong while deleting user.",
                "status": HTTPStatus.BAD_REQUEST.phrase,
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.debug(response)
            current_app.logger.error(error)
            traceback.print_exc()

            return response, status
