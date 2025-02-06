from datetime import datetime, timedelta
from flask import Blueprint, current_app
import traceback

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.models import DocumentTransaction, MateusPaymentGroup, db
from formsflow_api.schemas import MateusPaymentGroupWithPaymentsSchema
from formsflow_api.services import (
    DocumentsService,
    ApplicationService,
    ObligationService,
)
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api.utils.enums import DocumentStatusesEnum
from formsflow_api.services.receipts import ReceiptService
from formsflow_api.services.factory import KeycloakFactory
from formsflow_api.services.user_status_transaction import UserStatusTransactionService
from formsflow_api.services.external.eurotrust_integrations import (
    EurotrustIntegrationsService,
)
from formsflow_api.services.external.firebase import FirebaseService
from formsflow_api.services.external.keycloak import KeycloakAdminAPIService
from formsflow_api.services.login_event_service import LoginEventService

CronBlueprint = Blueprint("cron", __name__)


@CronBlueprint.cli.command("expire-documents")
def cron_expire_documents():
    current_time = datetime.utcnow()
    minutes = current_app.config.get("CRON_DOCUMENT_TRANSACTION_TIMEOUT")
    before = current_time - timedelta(minutes=int(minutes))

    current_app.logger.info(f"{current_time.isoformat()} - Cron Job - Expire documents")
    current_app.logger.info(f"All documents before {before.isoformat()}")
    transactions: list[DocumentTransaction] = DocumentTransaction.query.filter(
        DocumentTransaction.modified < before
    ).all()

    document_service = DocumentsService()

    for transaction in transactions:
        print(f"{transaction.transaction_id} - {transaction.formio_id}")
        if transaction.status_id:
            # If pending, check status
            if (
                transaction.status.title == DocumentStatusesEnum.PENDING
                or transaction.status.title == DocumentStatusesEnum.ON_HOLD
            ):
                current_app.logger.debug(
                    f"Checking status for transaction {transaction.transaction_id}"
                )
                status = document_service.get_document_transaction_status_in_eurotrust(
                    transaction.transaction_id
                )
                if status and status.title != DocumentStatusesEnum.PENDING:
                    # transaction.status_id = status.id
                    # A valid status is returned
                    # Try and update status in formio
                    try:
                        document_service.update_document_status_in_formio(
                            transaction.formio_id,
                            tenant_key=transaction.tenant_key,
                            status=status,
                        )

                        if status.title == DocumentStatusesEnum.SIGNED:
                            document_service.set_signed_file_from_eurotrust(transaction)

                        transaction.update_status_send_notification(new_status=status)

                        ApplicationService.update_message_for_application_by_status(
                            status=status, transaction=transaction
                        )

                        db.session.delete(transaction)
                    except BusinessException as err:
                        current_app.logger.error("An error occurred when updating")
                        current_app.logger.error(err)
                        continue
                elif status.title == DocumentStatusesEnum.PENDING:
                    current_app.logger.debug(f"Document status is still pending")
                else:
                    # If a status is not found
                    current_app.logger.critical(
                        f"While checking status for transaction '{transaction.stransaction_id}' an invalid status was returned from eurotrust"
                    )
                    # Don't delete it and continue
                    continue

            # Handle document delivery statuses
            elif transaction.status.title == DocumentStatusesEnum.DELIVERING:
                status_in_eurotrust = (
                    document_service.get_document_status_transaction_in_eurotrust(
                        transaction.thread_id
                    )
                )
                if status_in_eurotrust.title != DocumentStatusesEnum.DELIVERING:
                    try:
                        current_app.logger.debug("Eurotrust status title")
                        current_app.logger.debug(status_in_eurotrust.title)

                        document_service.update_document_status_in_formio(
                            transaction.formio_id,
                            tenant_key=transaction.tenant_key,
                            status=status_in_eurotrust,
                        )

                        receipts_service = ReceiptService()
                        receipts_service.download_and_save_receipts(
                            transaction.transaction_id,
                            transaction.tenant_key,
                            transaction.application_id,
                        )

                        db.session.delete(transaction)
                    except BusinessException as err:
                        current_app.logger.error("An error occurred when updating")
                        current_app.logger.error(err.error)
                        current_app.logger.error(err.status_code)
                        tb_str = traceback.format_exc()
                        current_app.logger.error("Traceback of the error")
                        current_app.logger.error(tb_str)
                        continue
                else:
                    current_app.logger.debug(f"Document status is still delivering")

            else:
                db.session.delete(transaction)

            # Delete transaction

    db.session.commit()


@CronBlueprint.cli.command("update_obligation_payments_statuses")
def cron_update_obligation_payments_statuses():
    try:
        current_time = datetime.utcnow()

        current_app.logger.info(
            f"{current_time.isoformat()} - Cron Job - Update Obligation Payments Statuses"
        )

        # Get all new obligation groups
        obligation_groups = MateusPaymentGroup.find_by_status("Pending")

        # Serialize the obligation groups
        schema = MateusPaymentGroupWithPaymentsSchema()
        obligation_groups = schema.dump(obligation_groups, many=True)

        client = ObligationService()

        # Update the status of the payments in each group
        for group in obligation_groups:
            client.update_group_by_payment(group)
    except EFormIntegrationException as err:
        response = {
            "type": "EForm IntegrationException",
            "message": err.message,
            "data": err.data,
        }
        current_app.logger.warning(response)
        current_app.logger.warning(err)
    except BaseException as submission_err:  # pylint: disable=broad-except
        response = {
            "type": "Bad request error",
            "message": str(submission_err),
        }
        current_app.logger.warning(response)
        current_app.logger.warning(submission_err)


@CronBlueprint.cli.command("delete-users")
def cron_delete_users():
    kc_admin = KeycloakFactory.get_instance()
    login_events = LoginEventService.select_events_of_unactive_users()
    for login_event in login_events:
        user_identifier = login_event.user_identifier
        users_list, _ = kc_admin.search_realm_users(
            search=user_identifier,
            page_no=1,
            limit=10,
            role=False,
            count=False
        )
        if not users_list:
            current_app.logger.warning(f"User with identifier {user_identifier} is not found!")
            continue

        user = users_list[0]

        # Check if user have some applications
        applications_count = ApplicationService.get_all_user_applications_count(user["username"])
        if applications_count == 0:
            # Check if user have owner, trustee or signiture forms
            have_e_other_identification_requests = ApplicationService.check_user_have_signed_documents(user_identifier)
            if have_e_other_identification_requests:
                current_app.logger.debug("This user have e identification requests.")  
                continue          

            # Check does the user have some obligation entities
            if not ObligationService.check_user_have_obligation_entities(user["username"].replace("pnobg-", "")):
                # Check if user is administrator
                user_groups = kc_admin.get_user_groups(user["id"])
                current_app.logger.debug("User groups")
                current_app.logger.debug(user_groups)
                official_group_names = [group.get("name") for group in user_groups if "София" in group.get("name")]
                if official_group_names:
                    current_app.logger.debug("This user is an official.")
                    continue

                # Delete the user in Keycloak
                try:
                    LoginEventService.delete_user_login_event(user_identifier)
                    kc_admin.delete_user(user["id"])
                except Exception as e:
                    current_app.logger.warning(f"Error while deleting user with identifier {user_identifier}")
                    current_app.logger.warning(e)

            else:
                current_app.logger.debug(f"User with identifier {user_identifier} have at least one paid obligation.")
                LoginEventService.add_user_have_service(user_identifier)

        else:
            current_app.logger.debug(f"User with identifier {user_identifier} have at least one application.")
            not_draft_applications_count = ApplicationService.get_user_not_draft_applications_count(user["username"])
            if not_draft_applications_count != 0:
                LoginEventService.add_user_have_service(user_identifier)


@CronBlueprint.cli.command("user-status-change")
def cron_user_status_change():
    current_time = datetime.utcnow()
    expired = current_time - timedelta(minutes=15)
    current_app.logger.debug("Start user status change cron")
    transactions = UserStatusTransactionService.select_all_transactions()
    eurotrust_service = EurotrustIntegrationsService()
    firebase_service = FirebaseService()
    for transaction in transactions:
        if transaction.created < expired:
            user_status_data = eurotrust_service.check_user_extented(
                transaction.user_identifier
            )

            keycloak_client = KeycloakAdminAPIService()
            url_path = f"users?username={transaction.user_identifier}"
            keycloak_users = keycloak_client.get_request(url_path)
            current_app.logger.debug(keycloak_users)
            keycloack_user = keycloak_users[0]
            fcm_list = keycloack_user.get("attributes", {}).get("fcm")
            current_app.logger.debug("user fcm list")
            current_app.logger.debug(fcm_list)

            user_fcm = fcm_list[0]

            stringified_data = {
                key: str(value) for key, value in user_status_data.items()
            }
            resp = firebase_service.send_user_status_change_message(
                stringified_data, user_fcm
            )

            current_app.logger.debug("Firebase notification resp")
            current_app.logger.debug(resp)

            transaction.delete()

@CronBlueprint.cli.command("clear-unactive-applications")
def cron_clear_unactive_applications():
    try:
        ApplicationService.delete_unactive_draft_applications()
    except Exception as e:
        current_app.logger("Error occured")
        current_app.logger(e)
