import string
import os
from pathlib import Path
from flask import current_app
from formsflow_api.workers.send_mail_service import EmailMessage, SendMailService, SendMailServiceConfig
from formsflow_api.workers.templates_config import TemplatesConfig
from formsflow_api.workers.mailmerge import MailMergeService, NotificationConfig
from formsflow_api.workers import message_types

'''
This class is used to render the email body based on the provided data using preconfigured mako templates.
'''


class MailNotificationService:
    _service: MailMergeService
    _config: NotificationConfig

    def __init__(self, config: NotificationConfig):
        self._config = config

    '''
    This method renders the email body based on the provided message_data.
    It selects automatically which email template to use based on the message_data["message_type"]
    '''

    def notify(self, message_data):
        try:
            if not message_data: raise Exception("Message data is required!")

            templates_config = TemplatesConfig()

            template_file_name = ""
            mail_subject = ""

            # Select which email template will be used to render the message based on the message_data["message_type"]
            if not message_data["message_type"]: raise Exception("Mesage bus message_type is required!")

            # This is a message to the end user to notify him/her for the result of the payment they initiated
            if message_data["message_type"] == message_types.PAYMENT_RESULT:
                if not templates_config.PAYMENT_RESULT: raise Exception("Mesage bus message template name is empty!")

                template_file_name = string.Template(templates_config.PAYMENT_RESULT).substitute(
                    language=message_data["language"])
                template_path = os.path.abspath(Path(self._config.templates_location, template_file_name))
                mail_subject = "Статус на плащане" if message_data["language"] == "bg" else "Your payment status"
                current_app.logger.info(template_path)

            # This is a system email to support to notify them that attempt to notify Mateus has failed
            elif message_data["message_type"] == message_types.MATEUS_NOTIFICATION_FAILURE:
                if not templates_config.MATEUS_NOTIFICATION: raise Exception(
                    "Message bus message template name is empty!")

                template_file_name = string.Template(templates_config.MATEUS_NOTIFICATION).substitute(
                    language=message_data["language"])
                template_path = os.path.abspath(Path(self._config.templates_location, template_file_name))
                mail_subject = f'Error notifying Mateus for {message_data["user_email"]} ({message_data["user_name"]}) for payment status {message_data["payment_status"]}'
                current_app.logger.info(template_path)

            if not Path(template_path).is_file():
                raise Exception(f"Template '{template_file_name}' not found in '{template_path}'")

            # Calling the mail merge functionality to render the email body
            self._service = MailMergeService(self._config)

            render_result = self._service.render(template_path, data=message_data)

            if not render_result.success:
                raise Exception(
                    f"Rendering mail body from template '{template_path}' failed with error {render_result.output}")

            # Creating and calling the actual email sender service which internally is using external REST service to
            # send mails.
            mail_config = SendMailServiceConfig(self._config.service_url)
            svc = SendMailService(mail_config)

            msg = EmailMessage(message_data["user_email"], mail_subject)

            # In this case the recipient (emails to support team) is taken from the configuration
            if message_data["message_type"] == message_types.MATEUS_NOTIFICATION_FAILURE:
                msg.to = self._config.support_emails

            msg.body = render_result.output

            # Sending the email and return the result
            return svc.send(msg)

        except Exception as err:
            current_app.logger.error(err)
            return None
