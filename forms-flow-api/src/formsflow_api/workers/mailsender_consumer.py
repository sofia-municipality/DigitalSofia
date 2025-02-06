import time
from kombu import Connection, Exchange, Queue, Consumer, Producer
from flask import current_app
from pathlib import Path
import os
from formsflow_api.workers.notification_config import NotificationConfig
from formsflow_api.workers.notification import MailNotificationService
from formsflow_api.workers.message_bus_config import MessageBusConfig


# This class listens for messages from the message bus then sends emails
class NotificationConsumer:
    _config: MessageBusConfig
    _exchange: Exchange
    _queue: Queue
    _is_test: bool

    def __init__(self, config: MessageBusConfig, is_test: bool = False):
        self._config = config
        self._is_test = is_test

        # Define the exchange and queue
        self._exchange = Exchange(self._config.exchange, type=self._config.type, durable=True)
        self._queue = Queue(self._config.queue, self._exchange, routing_key=self._config.routing_key)

    def process_email_task(self, body, message):

        # Message Format: "user_email":"user@email.com", "user_name" :"Ivan Ivanov", "payment_status":"success",
        # "message_type": message_types.MATEUS_NOTIFICATION_FAILURE, "person_dentifier": "123456789",
        # "language":"bg", "debug_info": "Mateus notification failure for user user@email.com, payment_group: 1234,
        # payment status: 'success'", "error": "<<Some Mateus Error Code>>"

        try:
            if not body: raise Exception("Mesage bus message Body is empty!")
            if not body["debug_info"]: raise Exception("Message bus's message info field is required!")

            notification_config = NotificationConfig()

            mail_svc = MailNotificationService(notification_config)
            result = mail_svc.notify(body)

            current_app.logger.info(f'Processing email task for {body["debug_info"]}')

            # Acknowledge the message
            if result is not None:
                if result.ok:
                    message.ack()
                    current_app.logger.info(f'Email sent for {body["debug_info"]}')
                    return True
                else:
                    raise Exception(
                        f'Email sent FAILED for {body["debug_info"]} with http status: {result.status_code}({result.text})')

            raise Exception(f'Email sent FAILED for {body["debug_info"]}')

        except Exception as e:
            current_app.logger.error(f"Error processing message: {e}")
            try:
                # Get the current retry count from message headers
                headers = message.headers
                retries = headers.get('x-retries', 0)

                if retries < self._config.max_retries:
                    # Increment retry count
                    headers['x-retries'] = retries + 1

                    # Requeue the message with incremented retry count
                    with Connection(self._config.url) as conn:
                        with conn.channel() as channel:
                            producer = Producer(channel)
                            producer.publish(
                                body,
                                exchange=self._exchange,
                                routing_key=self._config.routing_key,
                                headers=headers,
                                retry=True,
                                retry_policy={
                                    'interval_start': self._config.retry_delay,
                                    'interval_step': self._config.retry_delay,
                                    'interval_max': self._config.retry_delay * retries,
                                }
                            )

                    message.ack()  # Acknowledge the original message to remove it from the main queue so there will
                    # be no duplicate messages
                    current_app.logger.info(f"Message requested with retry count {headers['x-retries']}")
                else:
                    current_app.logger.error("Max retries reached. Moving message to dead-letter queue.")
                    # Move the message to the dead-letter queue
                    dead_exchange = Exchange(self._config.dead_exchange, type=self._config.type, durable=True)
                    dead_queue = Queue(self._config.dead_queue, dead_exchange, routing_key=self._config.dead_letter_key)

                    with Connection(self._config.url) as conn:
                        with conn.channel() as channel:
                            producer = Producer(channel)
                            producer.publish(
                                body,
                                exchange=dead_exchange,
                                routing_key=self._config.dead_letter_key,
                                headers=headers,
                                declare=[dead_queue],
                                retry=True
                            )
                    message.ack()  # Acknowledge the original message to remove it from the main queue
            except Exception as e:
                current_app.logger.error(
                    f"Unexpected error in NotificationConsumer while retrying or moving message to dead-letter queue: {e}. Operation will not be retried!")

    # Start the listening for messages process
    def run(self):
        while True:
            try:
                with Connection(self._config.url) as conn:
                    with conn.channel() as channel:
                        consumer = Consumer(channel, queues=[self._queue], callbacks=[self.process_email_task],
                                            accept=['application/json'])
                        consumer.consume()

                        print("NotificationConsumer is waiting for messages...")
                        current_app.logger.info("NotificationConsumer is waiting for messages...")

                        while True:
                            conn.drain_events()

            except Exception as e:
                current_app.logger.error(
                    f"Unexpected error in NotificationConsumer while listening for messages: {e}. Will retry in {self._config.retry_delay} seconds.")
                time.sleep(self._config.retry_delay)  # Wait before trying to reconnect


# Consumer startup
if __name__ == "__main__":
    config = MessageBusConfig()

    current_app.logger.info(f"Starting NotificationConsumer with configuration: {config}")

    consumer = NotificationConsumer(config)
    consumer.run()
