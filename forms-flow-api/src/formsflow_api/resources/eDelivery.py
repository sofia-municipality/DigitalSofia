"""API endpoints for processing applications resource."""
import base64
import re
from datetime import date, datetime
from http import HTTPStatus
import requests

from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import (

    cors_preflight,
    profiletime, user_context, auth, UserContext,
)

from formsflow_api.exceptions import EFormIntegrationException
from formsflow_api.models.region import ADDITIONAL_REGION_DATA
from formsflow_api.services import DraftService, FormioServiceExtended
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.resources.assurance_level_decorator import require_assurance_level

API = Namespace("EDelivery", description="EDelivery")

edelivery_response_model = API.model(
    "EDeliveryResponseModel",
    {
        "SendMessageOnBehalfToPersonResult": fields.Integer(),
    }
)


@cors_preflight("POST,OPTIONS")
@API.route("", methods=["POST", "OPTIONS"])
class EDeliveryResource(Resource):
    """Resource for edelivery."""
    @staticmethod
    @profiletime
    @auth.require
    @user_context
    # @require_assurance_level("high")
    @API.response(200, "CREATED:- Successful request.", model=edelivery_response_model)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    @API.response(
        403,
        "Forbidden:- Request forbidden -- authorization will not help",
    )    
    def post(**kwargs):
        try:
            user: UserContext = kwargs["user"]
            person_identifier = user.token_info["personIdentifier"]
            data = request.get_json()

            # Get form submission
            form_service = FormioServiceExtended()
            form_token = form_service.get_formio_access_token()
            form_submission = form_service.get_submission(data=data, formio_token=form_token)["data"]
            current_app.logger.info(form_submission)

            client = EFormIntegrationsService()
            # Find which municipality we are trying to sent to
            region = ADDITIONAL_REGION_DATA.get(int(form_submission["region"]["code"].split("-")[1]))

            eik = region["eik"]
            file_ids = []
            for key,value in form_submission.items():
                if isinstance(value, list):
                    for item in value:
                        if item["storage"]  and item["storage"]  == "url":
                            response = requests.get(item["data"]["url"], headers={
                                "Authorization": request.headers["Authorization"]
                            })
                            current_app.logger.info(item["name"])
                            file_response = client.eDelivery_upload_blob("000696327000001",
                                                                         {'file': (item["name"],
                                                                                   response.content)})
                            file_ids.append(file_response["blobId"])

            # Find it's id in eDelivety
            profile_response = client.eDelivery_search_profile(eik, 3)
            region_recipient_profile_id = profile_response["profileId"]

            # Fetch file to sent
            blob_id = client.eDelivery_upload_blob_from_base64(form_submission["pdfUrl"], request.headers["Authorization"])
            file_ids.append(blob_id)
            if form_submission["propertyOwnerPdfUrl"]:
                blob_id = client.eDelivery_upload_blob_from_base64(form_submission["propertyOwnerPdfUrl"], request.headers["Authorization"])
                file_ids.append(blob_id)


            # Get name and it arId for message title
            if form_submission["addressChangeType"] == "permanent":
                serviceId = 2079
                serviceName = "Издаване на удостоверение за постоянен адрес след подаване на заявление за промяна на постоянен адрес"
            else:
                serviceId = 2107
                serviceName = "Издаване на удостоверение за настоящ адрес след подаване на адресна карта за заявяване или за промяна на настоящ адрес"

            # Prepare data to sent
            sent_data = {
                "recipientProfileIds": [
                    region_recipient_profile_id
                ],
                "senderProfileId": "000696327000001",
                "subject": f"{form_submission['reference_number']}-{serviceId}-{serviceName}",
                "templateId": "1",
                "fields": {
                    "e2135802-5e34-4c60-b36e-c86d910a571a": file_ids
                }
            }

            # Sent to eDelivery
            response = client.sent_to_eDelivery(data=sent_data)

            return response, HTTPStatus.OK
        except EFormIntegrationException as err:
            response, status = {
                                   "type": "EForm IntegrationException",
                                   "message": err.message,
                                   "data": err.data,
                               }, err.error_code
            current_app.logger.warning(response)
            current_app.logger.warning(err)
            return response, status
        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status
