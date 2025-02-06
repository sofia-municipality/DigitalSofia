import marshmallow_dataclass
from flask import current_app
from marshmallow_dataclass import dataclass
from formsflow_api.utils import Singleton
from typing_extensions import Self, Optional

'''
This class holds the information to configure the connection and communication with the RabbitMQ
'''


@dataclass
class MessageBusConfig(metaclass=Singleton):
    url: Optional[str] = None  # URL address of the message bus (RabbitMQ)
    queue: Optional[str] = None  # Name of the RabbitMQ queue to send messages to
    exchange: Optional[str] = None  # Name of the RabbitMQ exchange
    routing_key: Optional[str] = None  # The routing key used to route the messages in the RabbitMQ
    type: Optional[str] = None  # Exchange type. Can be `direct`, `topic`, `fanout` or `headers`. We are using `topic`
    retry_delay: Optional[int] = None  # The delay in seconds before trying to recover from error and retry.
    max_retries: Optional[int] = None  # The number of retries to process a failed message before give up.
    dead_letter_key: Optional[str] = None  # The name of the dead-letter routing key where to put the failed messages.
    dead_queue: Optional[str] = None  # The name of the dead-letter queue where to put the failed messages.
    dead_exchange: Optional[str] = None  # The name of the dead-letter exchange where to put the failed messages.

    def __init__(self):
        config = current_app.config
        self.url = config.get("MESSAGE_BUS_URL")
        self.queue = config.get("MESSAGE_BUS_QUEUE")
        self.exchange = config.get("MESSAGE_BUS_EXCHANGE")
        self.routing_key = config.get("MESSAGE_BUS_ROUTING_KEY")
        self.type = config.get("MESSAGE_BUS_TYPE")
        self.retry_delay = config.get("MESSAGE_BUS_RETRY_DELAY")
        self.max_retries = config.get("MESSAGE_BUS_MAX_RETRIES")
        self.dead_letter_key = config.get("MESSAGE_BUS_DEAD_LETTER_KEY")
        self.dead_queue = config.get("MESSAGE_BUS_DEAD_QUEUE")
        self.dead_exchange = config.get("MESSAGE_BUS_DEAD_EXCHANGE")

    @classmethod
    def load_from_file(cls, file_name: str) -> Self:
        with open(file_name, 'r') as file:
            return cls.load_from_json(file.read())

    @classmethod
    def load_from_json(cls, json_data: str) -> Self:
        MessageBusConfigSchema = marshmallow_dataclass.class_schema(MessageBusConfig)
        schema = MessageBusConfigSchema()
        return schema.loads(json_data)


# if __name__ == "__main__":
#     data = MessageBusConfig()
#     print(data)
