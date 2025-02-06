from flask import Blueprint, current_app

from formsflow_api.workers.mailsender_consumer import NotificationConsumer
from formsflow_api.workers.message_bus_config import MessageBusConfig

WorkersBlueprint = Blueprint('workers', __name__)


@WorkersBlueprint.cli.command('notification-worker')
def notification():
    current_app.logger.debug("Notification Worker Started")

    config = MessageBusConfig()
    current_app.logger.info(config)

    consumer = NotificationConsumer(config)
    consumer.run()
