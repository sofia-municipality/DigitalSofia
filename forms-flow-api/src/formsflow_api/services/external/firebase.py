from flask import current_app
from firebase_admin import credentials, messaging
import firebase_admin
import os
from formsflow_api.models import DocumentTransaction, DocumentStatus
from firebase_admin._messaging_utils import UnregisteredError
from firebase_admin.exceptions import FirebaseError, InvalidArgumentError



class FirebaseService:

    def __init__(self):
        current_app.logger.debug("FirebaseService@init")
        path = current_app.config.get("FIREBASE_CREDENTIALS_PATH")
        current_app.logger.debug(path)
        cred = credentials.Certificate(path)
        try:   
            self.client = firebase_admin.get_app(name='[DEFAULT]')
        except ValueError:
            self.client = firebase_admin.initialize_app(cred, name='[DEFAULT]')
        current_app.logger.debug(f"Firebase Client Name - {self.client.name}")


    def send_status_change_message(self, 
            transaction: DocumentTransaction, 
            firebase_user_registration_token: str,
            locale: str = "bg"
        ):
        current_app.logger.info("FirebaseService@send_status_change_message")
        status = DocumentStatus.query.filter_by(id=transaction.status_id).first()
        if not status:
            current_app.logger.debug(f"Couldn't find status with id - {transaction.status_id}")
            return None
        current_app.logger.debug(f"Transaction with status - {status.formio_status}")
        body_based_on_status = {
            "signing": {
                "en":"You have a new document, ready for signing",
                "bg": "Имате нов електронен документ за подписване"
            },
            "signed": {
                "en":"Your document is signed successfully",
                "bg": "Вашият документ е успешно подписан"
            },
            "expired": {
                "en":"Your document has expired",
                "bg": "Срокът за подписване на Вашият документ е изтекъл"
            }
        }

        body = body_based_on_status.get(status.formio_status)
        if not body:
            current_app.logger.debug(f"Couldn't find body matching status - {status.formio_status}")
            return None

        localized_body = body.get(locale, None)

        current_app.logger.debug(f"Localized body - {localized_body}")

        data_object = {
            "transactionId": str(transaction.transaction_id),
            "threadId": str(transaction.thread_id),
            "filename": "",
            "applicationId": str(transaction.application_id),
            "status": status.formio_status
        }

        title = "Digitall Sofia"


        android_notification = messaging.AndroidNotification(
            title=title,
            body=localized_body
        )
        
        android_config = messaging.AndroidConfig(
            data=data_object,
            notification=android_notification
        )

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=localized_body
            ),
            data=data_object,
            android=android_config,
            token=firebase_user_registration_token
        )

        
        try:
            response = messaging.send(message)
        except FirebaseError as err:
            current_app.logger.error(f"A firebase error has occurred:{err}")
            return None
        except ValueError as err:
            current_app.logger.error(f"A value error has occurred when sending message:{err}")
            return None

        return response

    def single_message(self,
                       title:str,
                       body:str,
                       fcm_token:str,
                       data: dict= {}):

        android_notification = messaging.AndroidNotification(
            title=title,
            body=body
        )
        
        android_config = messaging.AndroidConfig(
            data=data,
            notification=android_notification
        )

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body
            ),
            data=data,
            android=android_config,
            token=fcm_token
        )

        try:
            response = messaging.send(message)
        except FirebaseError as err:
            current_app.logger.error(f"A firebase error has occurred:{err}")
            return None
        except ValueError as err:
            current_app.logger.error(f"A value error has occurred when sending message:{err}")
            return None

        return response

    def send_multicast_message(self, 
                     title:str, 
                     body:str, 
                     registration_tokens:list, 
                     data_object:dict=None):

        android_notification = messaging.AndroidNotification(
            title=title,
            body=body
        )

        android_config = messaging.AndroidConfig(
            data=data_object,
            notification=android_notification
        )

        message = messaging.MulticastMessage(
            notification=messaging.Notification(
                title=title,
                body=body
            ),
            data=data_object,
            android=android_config,
            tokens=registration_tokens
        )

        batch_response = messaging.send_multicast(message)
        current_app.logger.debug("Successfully sent message:")
        current_app.logger.debug(batch_response)
        
        to_return = []
        for single_response in batch_response.responses:
            
            current_app.logger.debug(single_response)
            to_return.append({
                "message_id": str(single_response.message_id),
                "success": str(single_response.success),
                "exception": str(single_response.exception)
            })

        return to_return
