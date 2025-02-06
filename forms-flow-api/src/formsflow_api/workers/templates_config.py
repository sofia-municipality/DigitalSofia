from flask import current_app
from marshmallow_dataclass import dataclass
from typing_extensions import Self, Optional
from formsflow_api.utils import Singleton
import marshmallow_dataclass

'''
This class contains the configuration information about what email templates are available.
It is a key/value structure where the key is the name of the template and the value contains the name of the template file.
The location of the templates is specified in the notification_config.json
'''


@dataclass
class TemplatesConfig(metaclass=Singleton):
    PAYMENT_RESULT: Optional[str] = None
    MATEUS_NOTIFICATION: Optional[str] = None

    def __init__(self):
        config = current_app.config
        self.PAYMENT_RESULT = config.get("PAYMENT_RESULT")
        self.MATEUS_NOTIFICATION = config.get("MATEUS_NOTIFICATION")

    @classmethod
    def load_from_file(cls, file_name: str) -> Self:
        with open(file_name, 'r') as file:
            return cls.load_from_json(file.read())

    @classmethod
    def load_from_json(cls, json_data: str) -> Self:
        TemplatesConfigSchema = marshmallow_dataclass.class_schema(TemplatesConfig)
        schema = TemplatesConfigSchema()
        return schema.loads(json_data)

# EXAMPLE USAGE:
# if __name__ == "__main__":
#     data = TemplatesConfig()
#     print(data)
