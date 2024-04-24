from flask import current_app, request
from flask_restx import Namespace, Resource
from formsflow_api_utils.utils import (
    cors_preflight,
    profiletime,
)
from formsflow_api.services import FirebaseService

API = Namespace("Firebase", description="Test Firebase Service")


@cors_preflight("POST,OPTIONS")
@API.route("", methods=["POST", "OPTIONS"])
class FirebaseApi(Resource):

    
    @staticmethod
    @profiletime
    def post():
        firebase_message_json = request.get_json()

        client = FirebaseService()
        response = client.send_multicast_message(
            title=firebase_message_json.get("title", "No message provided"),
            body=firebase_message_json.get("body", "No message body provided"),
            registration_tokens=[firebase_message_json.get("registration_token")],
            data_object=firebase_message_json.get("data", {"message": "No data provided"}),
        )

        return response