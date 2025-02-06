from typing import Optional
from http import HTTPStatus
import requests
import json
from typing import Optional
from flask import current_app


class SendMailServiceConfig:
    host: str

    def __init__(self, host: str):
        self.host = host


class EmailMessage:
    def __init__(self, mail_to: str, mail_subject: str, mail_body: Optional[str] = None):
        self.to = mail_to
        self.subject = mail_subject
        self.body = mail_body

    def to_dict(self):
        return {
            "to": self.to,
            "subject": self.subject,
            "body": self.body
        }

    @classmethod
    def from_dict(cls, data):
        return cls(
            mail_to=data.get("to"),
            mail_subject=data.get("subject"),
            mail_body=data.get("body")
        )

    def to_json(self):
        return json.dumps(self.to_dict(), encoding='utf-8', ensure_ascii=False)

    @classmethod
    def from_json(cls, json_str):
        data = json.loads(json_str)
        return cls.from_dict(data)


class SendMailService:
    _message: EmailMessage
    _config: SendMailServiceConfig

    def __init__(self, config: SendMailServiceConfig):
        self._config = config

    def send(self, message: EmailMessage):

        try:
            response = requests.post(
                url=self._config.host,
                json={
                    "to": message.to,
                    "subject": message.subject,
                    "body": message.body.decode("utf-8")
                }
            )

            return response
        except Exception as err:
            current_app.logger.error(err)
            return None

# THIS IS FOR TESTING PURPOSE ONLY - EXAMPLE USAGE
# if __name__ == "__main__":
#     config = SendMailServiceConfig("http://localhost:8002/integrations/notifications/post-mail-notification")
#     svc = SendMailService(config)

#     msg = EmailMessage("ruslan.kiskinov@gmail.com", "test email")
#     msg.body = "<p>Testing the mail send process!</p>"

#     svc.send(msg)
