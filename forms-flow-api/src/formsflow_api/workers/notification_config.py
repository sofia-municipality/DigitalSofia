from flask import current_app
from marshmallow_dataclass import dataclass
from typing_extensions import Self, Optional
from formsflow_api.utils import Singleton
import marshmallow_dataclass

'''
This class holds configuration information for the notification.
It is used by the MailNotificationService class in notification_config.py
'''


@dataclass
class NotificationConfig(metaclass=Singleton):
    templates_location: Optional[str] = None
    service_url: Optional[str] = None
    support_emails: Optional[str] = None
    notification_schema: Optional[str] = None

    def __init__(self):
        config = current_app.config
        self.templates_location = config.get("TEMPLATES_LOCATION")
        self.service_url = config.get("SERVICE_URL")
        self.support_emails = config.get("SUPPORT_EMAILS")
        self.notification_schema = config.get("NOTIFICATION_SCHEMA")

    @classmethod
    def load_from_file(cls, file_name: str) -> Self:
        with open(file_name, 'r') as file:
            return cls.load_from_json(file.read())

    @classmethod
    def load_from_json(cls, json_data: str) -> Self:
        NotificationConfigSchema = marshmallow_dataclass.class_schema(NotificationConfig)
        schema = NotificationConfigSchema()
        return schema.loads(json_data)

# if __name__ == "__main__":
#     data = NotificationConfig()
#     print(data)
