import os
import json
from pathlib import Path
from typing import cast
from flask import Blueprint, current_app
from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.models import MateusPaymentGroup, MateusPaymentRequest, db
from formsflow_api.schemas import MateusPaymentGroupSchema, MateusPaymentGroupWithPaymentsSchema
from formsflow_api.workers.message_bus_config import MessageBusConfig
from formsflow_api.workers.notification_config import NotificationConfig
from formsflow_api.workers.mailsender_producer import NotificationProducer
from formsflow_api.services import ObligationService
from datetime import datetime, timedelta

MateusBlueprint = Blueprint('mateus', __name__)


@MateusBlueprint.cli.command('notify')
def notify():
    current_app.logger.debug("Mateus Notification Service Started")

    notification_config = NotificationConfig()

    current_app.logger.info(notification_config)

    notification_schema = notification_config.notification_schema.split(',')
    current_app.logger.info(notification_schema)

    result = MateusPaymentGroup.get_pending_notification()

    # The number of configured daily schedules
    numScheduledPeriods = len(notification_schema)

    # If nothing is configured assume there is at least one scheduled period
    if numScheduledPeriods == 0:
        numScheduledPeriods = 1

    if result[2] > 0:
        for item in result[0]:
            # How much days has passed from the first notification try
            # Used to determine which schedule to get from the notification_schema
            daysPassed = 0
            scheduleInterval = 0

            # This is the first attempt for notification
            if item.first_notification_try is None:
                scheduleInterval = int(notification_schema[0])
            else:
                daysPassed = (datetime.now() - item.first_notification_try).days
                current_app.logger.debug(f"Days passed: {daysPassed}")

                if daysPassed > numScheduledPeriods:  # We give up for the notification after the 5th day
                    # TODO: How to mark the group as failed?? Temporary we marked is as notified which is not exactly true.
                    current_app.logger.info(
                        f"Passed more than 5 days in retries so we mark request group {item.id} as failed")
                    item.is_notified = True
                    item.save()
                    return
                else:
                    scheduleInterval = int(notification_schema[daysPassed])

            current_app.logger.debug(f"Schedule Interval: {scheduleInterval}")

            # Check when has the last notification attempt
            if item.last_notification_try is not None:

                # Calculate how much seconds has passed since the last notification attempt
                secondsPassed = (datetime.now() - item.last_notification_try).total_seconds()

                current_app.logger.debug(f"Seconds passed: {secondsPassed}")

                # The time for the next attempt to notify Mateus has come
                if secondsPassed >= scheduleInterval:
                    notify_mateus(item)
                else:  # We'll skip this pass of the cronjob
                    current_app.logger.debug("SKIP THIS PASS")
            else:
                notify_mateus(item)
                current_app.logger.debug("set first_notification_try")
    else:
        current_app.logger.debug("No pending mateus payment groups for notification")


# Notify the Mateus for successfully payment
def notify_mateus(group: MateusPaymentGroup):
    print(f"Notify Mateus for payment group {group.id}")

    try:
        # Get the payment requests for the payment group
        items = MateusPaymentRequest.get_by_mateus_payment_group(group.id)

        # Prepare the data for mateus and notify it
        # To test it you can pass a third parameter test with value of 
        # - test="400" for error notification
        # - test="success" for sending successfully payment notification
        notify_data = ObligationService.update_mateus_for_payments(group, items)
        status_code = notify_data["statusCode"]

        if status_code < 400:
            transaction_status = notify_data["data"].get("transactionStatus", "NO_TRANSACTION_STATUS")

            if notify_data and notify_data["success"]:
                print(f"Status Code: {status_code}")
                # TODO: What else we can have as a return status here???
                if transaction_status == 'COMPLETED':
                    pay_transaction_id = notify_data["data"].get("payTransaction", None).get("payTransactionId", None)
                    transaction_time = notify_data["data"].get("payTransaction", None).get("transactionTime", None)
                    agent_transaction_id = notify_data["data"].get("payTransaction", None).get("agentTransactionId",
                                                                                               None)

                    group.is_notified = send_payment_notification(group, items, "success")

                    # Store some of the returned data from Mateus in our database in the mateus_payment_group table
                    group.pay_transaction_id = pay_transaction_id
                    group.transaction_time = transaction_time
                    group.agent_transaction_id = agent_transaction_id

                elif transaction_status == "NOT_REGISTERED":
                    current_app.logger.debug(f"Payments for group {group.id} was already sent to Mateus. SKIPPING")
                else:
                    if group.retry_count < 1:
                        send_error_notification(group, items, transaction_status, notify_data)
                    group.is_notified = False

            else:
                if group.retry_count < 1:
                    send_error_notification(group, items, transaction_status, notify_data)
                group.is_notified = False

        else:
            if group.retry_count < 1:
                send_error_notification(group, items, "MATEUS_ERROR", notify_data)
            group.is_notified = False

        if group.first_notification_try is None:
            group.first_notification_try = datetime.utcnow()

        group.last_notification_try = datetime.utcnow()
        group.retry_count += 1

        group.save()

    except Exception as ex:
        current_app.logger.error(f"Error notify_mateus with Payment group: {group.id}. Error: {ex}")


def send_payment_notification(group, items, payment_status):
    sender = create_sender()
    print(f"send_payment_notification for group {group.id}, {len(items)}, paymentStatus: {payment_status} ")
    return sender.send_payment_notification_task(group.id, "bg", payment_status)


def send_error_notification(group, items, payment_status, notify_data):
    sender = create_sender()
    print(f"send_error_notification for group {group.id}, {len(items)}, data: {notify_data} ")
    return sender.send_mateus_notification_failure(group.id, "bg", payment_status, notify_data)


def create_sender():
    config = MessageBusConfig()

    current_app.logger.info(config)

    return NotificationProducer(config, is_test=False)
