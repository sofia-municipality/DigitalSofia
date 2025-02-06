import json
from flask import current_app

from kombu import Connection, Exchange, Queue, Producer
from formsflow_api.models import MateusPaymentGroup
from formsflow_api.services.external.keycloak import KeycloakAdminAPIService

from formsflow_api.workers.message_bus_config import MessageBusConfig
from formsflow_api.workers import message_types

# This class is intended to send message to message bus for email notification
'''
The class is a message producer that creates a message in the RabbitMQ which have to be catched by the consumer mailesender_consumer.

Queue:    A queue is a buffer that stores messages. 
          In RabbitMQ, messages are sent to a queue where they wait until they can be processed by a consumer. 
          Queues are essentially the storage mechanism for messages.
Exchange: An exchange is a message routing mechanism that determines how messages are distributed to queues. 
          Producers send messages to an exchange, which then routes the messages to one or more queues based on certain rules.
          There are several types of exchanges:
          - Direct Exchange: Routes messages to queues based on a message routing key. WE ARE USING THIS TYPE
          - Fanout Exchange: Routes messages to all queues bound to it, without considering any routing keys.
          - Topic Exchange: Routes messages to queues based on wildcard matches between the routing key and the routing pattern specified in the binding.
          - Headers Exchange: Routes messages based on header values instead of the routing key.
'''


class NotificationProducer:
    config: MessageBusConfig
    exchange: Exchange
    queue: Queue
    is_test: bool

    def __init__(self, config: MessageBusConfig, is_test: bool = False):
        self.config = config
        self.is_test = is_test

        # Define the RabbitMQ connection parameters
        # Define the exchange and queue

        self.exchange = Exchange(self.config.exchange, type=self.config.type, durable=True)
        self.queue = Queue(self.config.queue, self.exchange, routing_key=self.config.routing_key)

    # Sends email notification to the user for successfully or failed payment
    '''
        payment_group - The Mateus payment group ID from the table mateus_payment_group
        language_code - in which language the notification must be sent. Options are 'bg' or 'en'
        payment_status - status of the user payment. It can be 'success', 'failed'...
    '''

    def send_payment_notification_task(self, payment_group: int, language_code: str, payment_status: str):

        try:
            if not payment_group: raise Exception(f"payment_group is required")
            if not payment_group: raise Exception(f"language_code is required")
            if not payment_status: raise Exception(f"payment_status is required")

            if not self.is_test:
                # Extract the user information given the payment_group and language_code
                data = self._extract_data(payment_group, language_code)
            else:
                data = {
                    "user_email": "user@email.com",
                    "user_name": "Ivan Ivanov",
                    "payment_status": "success",
                    "message_type": message_types.PAYMENT_RESULT,
                    "person_identifier": "123456789",
                    "language": "bg",
                    "debug_info": "Payment notification for user user@email.com with payment status: 'success'"
                }

            if not data: raise Exception(f"Error retrieving data for user info in payment group {payment_group}")
            if not data['user_email']: raise Exception(f"Error retrieving user_email for user info")
            if not data['user_name']: raise Exception(f"Error retrieving user_name for user info")
            if not data['person_identifier']: raise Exception(f"Error retrieving person_identifier for user info")

            # Prepare the message
            message = {
                'user_email': data['user_email'],
                'user_name': data['user_name'],
                "payment_status": payment_status,
                'message_type': message_types.PAYMENT_RESULT,
                'person_identifier': data['person_identifier'],
                'language': language_code,
                "debug_info": json.dumps(
                    f"Payment notification for user {data['user_email']} with payment status: '{payment_status}'",
                    ensure_ascii=False),
                "error": None
            }

            # Sending message to message bus
            with Connection(self.config.url) as conn:
                with conn.channel() as channel:
                    producer = Producer(channel)

                    producer.publish(
                        json.dumps(message),
                        exchange=self.exchange,
                        routing_key=self.config.routing_key,
                        content_type='application/json',
                        serializer='json',
                        declare=[self.queue]
                    )
            return True
        except Exception as err:
            current_app.logger.error(f"Error send_payment_notification_task {err}")
            return False

    # Sends notification to support team about the problems with updating Mateus for the user payment status.
    def send_mateus_notification_failure(self, payment_group: int, language_code: str, payment_status: str, error: str):

        try:

            if not payment_group: raise Exception(f"payment_group is required")
            if not payment_group: raise Exception(f"language_code is required")
            if not payment_status: raise Exception(f"payment_status is required")

            if not self.is_test:
                user_data = self._extract_data(payment_group, language_code)
            else:
                user_data = {
                    "user_email": "user@email.com",
                    "user_name": "Ivan Ivanov",
                    "payment_status": "success",
                    "message_type": message_types.MATEUS_NOTIFICATION_FAILURE,
                    "person_identifier": "123456789",
                    "language": "bg",
                    "debug_info": "Mateus notification failure for user user@email.com, payment_group: 1234, payment status: 'success'",
                    "error": "### Some Mateus Error Code ###"
                }

            if not user_data: raise Exception(f"Error retrieving data for user info in payment group {payment_group}")
            if not user_data['user_email']: raise Exception(f"Error retrieving user_email for user info")
            if not user_data['user_name']: raise Exception(f"Error retrieving user_name for user info")
            if not user_data['person_identifier']: raise Exception(f"Error retrieving person_identifier for user info")

            # Prepare the message
            message = {
                'user_email': user_data['user_email'],
                'user_name': user_data['user_name'],
                "payment_status": payment_status,
                'message_type': message_types.MATEUS_NOTIFICATION_FAILURE,
                'person_identifier': user_data['person_identifier'],
                'language': language_code,
                'debug_info': json.dumps(
                    f"Mateus notification failure for user {user_data['user_email']}, payment_group: {payment_group}, payment status: '{payment_status}'.",
                    ensure_ascii=False),
                'error': json.dumps(error, ensure_ascii=False)
            }

            # Sending message to message bus
            with Connection(self.config.url) as conn:
                with conn.channel() as channel:
                    producer = Producer(channel)
                    producer.publish(
                        json.dumps(message),
                        exchange=self.exchange,
                        routing_key=self.config.routing_key,
                        content_type='application/json',
                        serializer='json',
                        declare=[self.queue]
                    )

            return True
        except Exception as err:
            current_app.logger.error(f"Error send_mateus_notification_failure {err}")
            return False

    # Extracting user information from Kaycloak and Mateus based on the payment group and EGN
    # Language code is used to select which user names will be used the one written in Cyrillyc ones or the ones written i Latin
    def _extract_data(self, payment_group: int, language_code: str):

        try:

            if not payment_group: raise Exception(f"payment_group is required")
            if not language_code: raise Exception(f"Two letter language code is required such as 'en' or 'bg'")
            if len(language_code) != 2: raise Exception(f"The language code must be two letter code e.g. 'en' or 'bg'")

            # Get the status of the payment
            payment_info = MateusPaymentGroup.find_by_id(payment_group)

            if not payment_info: raise Exception(f"Error getting payment info for payment group {payment_group}")

            # Get the user info from keycloak
            keycloak_client = KeycloakAdminAPIService()

            # TODO: WHAT ABOUT OTHER PREFIXES?????
            prefix = "pnobg-"

            url_path = f"users?username={prefix}{payment_info.person_identifier}&exact={True}"
            keycloak_users = keycloak_client.get_request(url_path)

            # User not found?
            if not keycloak_users: raise Exception(f"Error getting info for user {payment_info.person_identifier}")

            user = keycloak_users[0]

            user_name = f"{user['firstName']} {user['lastName']}"
            user_email = user["email"]

            if not user_name: raise Exception(
                f"Error extracting the user name from kaycloak for user {payment_info.person_identifier}")
            if not user_email: raise Exception(
                f"Error extracting the user email from keycloak for user {payment_info.person_identifier}")

            user_name = user_name.replace('[', '').replace(']', '').replace("'", '')
            print(user_name)

            # Return the collected user data
            return {
                "user_email": user_email,
                "user_name": user_name,
                "person_identifier": payment_info.person_identifier,
                "language": language_code
            }

        except Exception as err:
            current_app.logger.error(f"Error in _extract_data: {err}")

# SAMPLE USAGE:
# if __name__ == "__main__":
#     config = MessageBusConfig()
#     sender = NotificationProducer(config, is_test = False)
#     sender.send_payment_notification_task(40, "bg", "success")
