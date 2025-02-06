"""API endpoints for draft resource."""
from http import HTTPStatus

import xml.etree.ElementTree as ET
import os
from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api.schemas.invalidate_app_flag import InvalidateAppFlagSchema
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    NEW_APPLICATION_STATUS,
    auth,
    cors_preflight,
    profiletime,
    user_context
)
from marshmallow.exceptions import ValidationError

from formsflow_api.schemas import (
    ApplicationSchema,
    ApplicationSubmissionSchema,
    DraftListSchema,
    DraftSchema,
    DraftCheckForChildApp
)
from formsflow_api.services import ApplicationService, DraftService
from formsflow_api.resources.assurance_level_decorator import require_assurance_level
from formsflow_api.services.external.redis_manager import RedisManager
from formsflow_api_utils.utils.user_context import UserContext
from formsflow_api.models.draft import Draft

API = Namespace("Draft", description="Manage Drafts")

message = API.model("Message", {"message": fields.String()})

draft = API.model(
    "Draft",
    {
        "data": fields.Raw(),
        "formId": fields.String(),
    },
)

draft_response = API.inherit(
    "DraftResponse",
    draft,
    {
        "CreatedBy": fields.String(),
        "DraftName": fields.String(),
        "applicationId": fields.Integer(),
        "created": fields.String(),
        "id": fields.Integer(),
        "modified": fields.String(),
        "processName": fields.String(),
    },
)

draft_response_by_id = API.inherit(
    "DraftResponseById", draft_response, {"processKey": fields.String()}
)

draft_create_response = API.model(
    "DraftCreated",
    {
        "applicationId": fields.Integer(),
        "created": fields.String(),
        "data": fields.Raw(),
        "id": fields.Integer(),
        "modified": fields.String(),
        "_id": fields.String(),
    },
)

drafts = API.model(
    "Drafts",
    {
        "drafts": fields.List(
            fields.Nested(draft_response, description="List of drafts")
        ),
        "applicationCount": fields.Integer(),
        "totalCount": fields.Integer(),
    },
)

submission = API.model(
    "Submission",
    {
        "formId": fields.String(),
        "formUrl": fields.String(),
        "submissionId": fields.String(),
        "webFormUrl": fields.String(),
    },
)

submission_response = API.model(
    "SubmissionResponse",
    {
        "applicationStatus": fields.String(),
        "created": fields.String(),
        "createdBy": fields.String(),
        "formId": fields.String(),
        "formProcessMapperId": fields.String(),
        "id": fields.Integer(),
        "modified": fields.String(),
        "modifiedBy": fields.String(),
        "processInstanceId": fields.String(),
        "submissionId": fields.String(),
    },
)

draftPDF = API.model(
    "DraftPDF",
    {
        "url": fields.String()
    },
)


@cors_preflight("GET,POST,OPTIONS")
@API.route("", methods=["GET", "POST", "OPTIONS"])
class DraftResource(Resource):
    """Resource for managing drafts."""

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(
        params={
            "pageNo": {
                "in": "query",
                "description": "Page number for paginated results",
                "default": "1",
            },
            "limit": {
                "in": "query",
                "description": "Limit for paginated results",
                "default": "5",
            },
            "sortBy": {
                "in": "query",
                "description": "Specify field for sorting the results.",
                "default": "id",
            },
            "sortOrder": {

                "in": "query",
                "description": "Specify sorting  order.",
                "default": "desc",
            },
            "DraftName": {
                "in": "query",
                "description": "Filter resources by form name.",
                "type": "string",
            },
            "id": {
                "in": "query",
                "description": "Filter resources by id.",
                "type": "int",
            },
            "modifiedFrom": {
                "in": "query",
                "description": "Filter resources by modified from.",
                "type": "string",
            },
            "modifiedTo": {
                "in": "query",
                "description": "Filter resources by modified to.",
                "type": "string",
            },
        }
    )
    @API.response(200, "OK:- Successful request.", model=drafts)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get():
        """Retrieve drafts."""
        try:
            dict_data = DraftListSchema().load(request.args) or {}
            draft_list, count = DraftService.get_all_drafts(dict_data)
            application_count = ApplicationService.get_application_count(auth)
            result = {
                "drafts": draft_list,
                "totalCount": count,
                "applicationCount": application_count,
            }
            return (result, HTTPStatus.OK)

        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                "type": "Bad request error",
                "message": "Invalid submission request passed",
            }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @require_assurance_level("high")
    @API.doc(body=draft)
    @API.response(201, "CREATED:- Successful request.", model=draft_create_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        403,
        "Forbidden:- Request forbidden -- authorization will not help",
    )
    def post(**kwargs):
        """Create a new draft."""
        try:

            current_app.logger.debug("DRAFT - CREATE")

            application_json = request.get_json()
            application_schema = ApplicationSchema()
            application_dict_data = application_schema.load(application_json)
            draft_json = request.get_json()
            draft_schema = DraftSchema()
            draft_dict_data = draft_schema.load(draft_json)
            token = request.headers["Authorization"]

            current_app.logger.debug(f"application_dict_data: {application_dict_data}")
            current_app.logger.debug(f"draft_dict_data: {draft_dict_data}")

            service_id = None
            behalf_data = None
            target_person_identifier = None
            child_person_identifier = None
            is_admin = False

            if "data" not in draft_dict_data:
                raise Exception("'data' segment not found in the JSON response.")
            if "processingUser" in draft_dict_data["data"]:
                is_admin = True
            if is_admin:
                res = DraftService.create_new_draft(
                    application_dict_data, draft_dict_data, token
                )

                response = draft_schema.dump(res)
                return response, HTTPStatus.CREATED

            if "behalf" in draft_dict_data["data"]:
                behalf_data = draft_dict_data["data"]["behalf"]

            if "childPersonIdentifier" in draft_dict_data["data"]:
                child_person_identifier = draft_dict_data["data"]["childPersonIdentifier"]

            skip_check_for_child_for_other_person = False

            if (behalf_data == "otherPerson" or behalf_data == "myBehalf") and "otherPersonIdentifier" in \
                    draft_dict_data["data"]:
                target_person_identifier = draft_dict_data["data"]["otherPersonIdentifier"]

                if not target_person_identifier:
                    skip_check_for_child_for_other_person = True

            elif behalf_data == "child" and "property" in draft_dict_data["data"] and draft_dict_data["data"][
                "property"] == "anotherPersonProperty" and "otherPersonIdentifier" in draft_dict_data["data"]:
                target_person_identifier = draft_dict_data["data"]["otherPersonIdentifier"]

                if not target_person_identifier:
                    skip_check_for_child_for_other_person = True

            else:
                target_person_identifier = draft_dict_data["data"]["personIdentifier"]

            current_app.logger.warning(f"EGN: {target_person_identifier}")

            # Remove the EGN prefix
            target_person_identifier = target_person_identifier.lower().replace("pnobg-", "")
            current_app.logger.warning(f"Target Person: {target_person_identifier}; Behalf: {behalf_data}")

            if "serviceId" in draft_dict_data["data"]:
                service_id = draft_dict_data["data"]["serviceId"]
                # Check for existing, not completed applications for a child.

                # Пропускаме проверката за дете когато се пуска заявление от името на друго лице
                # защото на първа стъпка където се създава драфт още няма ЕГН на другото лице.
                if not skip_check_for_child_for_other_person:

                    # Skip the child check if we have joint custody when creating application for a child
                    if "trusteeIdentifier" not in draft_dict_data["data"]:

                        check_result, check_status = (
                            DraftService.check_existing_application_for_child(str(service_id),
                                                                              target_person_identifier,
                                                                              child_person_identifier,
                                                                              **kwargs))
                        if check_result is not None:
                            return check_result, check_status

                res = DraftService.create_new_draft(
                    application_dict_data, draft_dict_data, token
                )

                response = draft_schema.dump(res)
            else:
                raise Exception("Service ID is missing or provided value cannot be recognized.")

            return (response, HTTPStatus.CREATED)
        except BusinessException as err:
            current_app.logger.warning(err)
            response, status = err.error, err.status_code
            return response, status
        except Exception as draft_err:  # pylint: disable=broad-except
            response, status = {
                "type": "Bad request error",
                "message": "Invalid submission request passed",
            }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(draft_err)
            return response, status


@cors_preflight("GET,PUT,DELETE,OPTIONS")
@API.route("/<int:draft_id>", methods=["GET", "PUT", "DELETE", "OPTIONS"])
class DraftResourceById(Resource):
    """Resource for managing draft by id."""

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.", model=draft_response_by_id)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get(draft_id: str):
        """Get draft by id."""
        try:
            return DraftService.get_draft(draft_id), HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err)
            return err.error, err.status_code

    @staticmethod
    @auth.require
    @user_context
    @profiletime
    @API.doc(body=draft)
    @API.response(
        200,
        "OK:- Successful request. Returns ```str: success message```",
    )
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def put(draft_id: int, **kwargs):
        """Update draft details."""

        try:
            draft_json = request.get_json()
            draft_schema = DraftSchema()
            dict_data = draft_schema.load(draft_json)

            behalf_data = None
            target_person_identifier = None

            if "behalf" in dict_data["data"]:
                behalf_data = dict_data["data"]["behalf"]

            person_identifier = dict_data["data"]["personIdentifier"]
            person_identifier = person_identifier.lower().replace("pnobg-", "")

            if behalf_data == "otherPerson" and "otherPersonIdentifier" in dict_data["data"]:
                target_person_identifier = dict_data["data"]["otherPersonIdentifier"]
                target_person_identifier = target_person_identifier.lower().replace("pnobg-", "")

                if person_identifier == target_person_identifier:
                    return {
                        "error": "Other person's EGN must be different from the current user's EGN.",
                        "errorMessageTranslation": "app_other_person_same_egn"
                    }, HTTPStatus.UNPROCESSABLE_ENTITY

            DraftService.update_draft(draft_id=draft_id, data=dict_data)

            return (
                f"Updated {draft_id} successfully",
                HTTPStatus.OK,
            )
        except BusinessException as err:
            # exception from draft service
            current_app.logger.warning(err)
            error, status = err.error, err.status_code
            return error, status

        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)

            return response, status

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.", model=message)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def delete(draft_id: int):
        """Delete draft."""
        try:
            DraftService.delete_draft(draft_id)
            return {"message": "Draft deleted successfully"}, HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err)
            return err.error, err.status_code


@cors_preflight("PUT, OPTIONS")
@API.route("/<int:draft_id>/submit", methods=["PUT", "OPTIONS"])
class DraftSubmissionResource(Resource):
    """Converts the given draft entry to actual submission."""

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(body=submission)
    @API.response(200, "OK:- Successful request.", model=submission_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def put(draft_id: str):
        """Updates the application and draft entry to create a new submission."""
        try:
            payload = request.get_json()
            token = request.headers["Authorization"]
            application_schema = ApplicationSubmissionSchema()
            dict_data = application_schema.load(payload)
            dict_data["application_status"] = NEW_APPLICATION_STATUS
            response = DraftService.make_submission_from_draft(
                dict_data, draft_id, token
            )
            res = ApplicationSchema().dump(response)
            return res, HTTPStatus.OK

        except ValidationError as err:
            current_app.logger.warning(err)
            response, status = {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST
            return response, status

        except BusinessException as err:
            # exception from draft service
            current_app.logger.warning(err)
            error, status = err.error, err.status_code
            return error, status

        except Exception as unexpected_error:
            raise unexpected_error


@cors_preflight("POST, OPTIONS")
@API.route("/public/create", methods=["POST", "OPTIONS"])
class PublicDraftResource(Resource):
    """Public endpoints to support anonymous forms."""

    @staticmethod
    @profiletime
    @API.doc(body=draft)
    @API.response(201, "CREATED:- Successful request.", model=draft_create_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def post():
        """Create a new draft submission."""
        try:
            application_json = draft_json = request.get_json()
            application_schema = ApplicationSchema()
            draft_schema = DraftSchema()

            application_dict_data = application_schema.load(application_json)
            draft_dict_data = draft_schema.load(draft_json)
            res = DraftService.create_new_draft(application_dict_data, draft_dict_data)
            response = draft_schema.dump(res)
            return (response, HTTPStatus.CREATED)
        except BusinessException as err:
            current_app.logger.warning(err)
            response, status = err.error, err.status_code
            return response, status
        except BaseException as draft_err:  # pylint: disable=broad-except
            response, status = {
                "type": "Bad request error",
                "message": "Invalid submission request passed",
            }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(draft_err)
            return response, status


@cors_preflight("PUT, OPTIONS")
@API.route("/public/<int:draft_id>/submit", methods=["PUT", "OPTIONS"])
class PublicDraftResourceById(Resource):
    """Public endpoints for anonymous draft."""

    @staticmethod
    @profiletime
    @API.doc(body=submission)
    @API.response(200, "OK:- Successful request.", model=submission_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def put(draft_id: int):
        """Updates the application and draft entry to create a new submission."""
        try:
            payload = request.get_json()
            application_schema = ApplicationSubmissionSchema()
            dict_data = application_schema.load(payload)
            dict_data["application_status"] = NEW_APPLICATION_STATUS
            response = DraftService.make_submission_from_draft(dict_data, draft_id)
            res = ApplicationSchema().dump(response)
            return res, HTTPStatus.OK

        except ValidationError as err:
            current_app.logger.warning(err)
            response, status = {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST
            return response, status

        except BusinessException as err:
            # exception from draft service
            current_app.logger.warning(err)
            error, status = err.error, err.status_code
            return error, status

        except Exception as unexpected_error:
            raise unexpected_error


@cors_preflight("PUT, OPTIONS")
@API.route("/public/<int:draft_id>", methods=["PUT", "OPTIONS"])
class PublicDraftUpdateResourceById(Resource):
    """Resource for updating the anonymous draft."""

    @staticmethod
    @profiletime
    @API.doc(body=draft)
    @API.response(
        200,
        "OK:- Successful request. Returns ```str: success message```",
    )
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def put(draft_id: int):
        """Update draft details."""
        draft_json = request.get_json()
        try:
            draft_schema = DraftSchema()
            dict_data = draft_schema.load(draft_json)
            DraftService.update_draft(draft_id=draft_id, data=dict_data)
            return (
                f"Updated {draft_id} successfully",
                HTTPStatus.OK,
            )
        except BusinessException as err:
            # exception from draft service
            current_app.logger.warning(err)
            error, status = err.error, err.status_code
            return error, status

        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                "type": "Bad request error",
                "message": "Invalid request data",
            }, HTTPStatus.BAD_REQUEST

            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)

            return response, status


@cors_preflight("POST, OPTIONS")
@API.route("/<int:draft_id>/export/pdf", methods=["POST", "OPTIONS"])
class ExportPDFFromDraftResource(Resource):
    """Public endpoints to support anonymous forms."""

    @staticmethod
    @auth.require
    @user_context
    @profiletime
    @API.response(200, "OK:- Successful request.", model=draftPDF)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post(draft_id: int, **kwargs):
        try:
            response = DraftService.export_draft_to_pdf(draft_id, **kwargs)

            return {"url": response}, HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err.error)
            return err.error, err.status_code


@cors_preflight("POST,OPTIONS")
@API.route("/invalidate", methods=["POST", "OPTIONS"])
class DraftInvalidateFlag(Resource):

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post():
        current_app.logger.warning("NO NEED TO INVALIDATE REDIS FLAG. REMOVE THE CALLER!!!")


@cors_preflight("POST,OPTIONS")
@API.route("/exists", methods=["POST", "OPTIONS"])
class DraftCheckExistingForChild(Resource):

    @staticmethod
    @auth.require
    @profiletime
    @user_context
    @API.response(200, "OK:- Successful request.")
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def post(**kwargs):

        try:

            request_json = request.get_json()
            schema = DraftCheckForChildApp()
            data = schema.load(request_json)

            if not "service_id" in data:
                return {
                    "error": "serviceId is required in the body of the request"
                }, HTTPStatus.BAD_REQUEST

            if not "person_identifier" in data:
                return {
                    "error": "person_identifier is required in the body of the request"
                }, HTTPStatus.BAD_REQUEST

            service_id = data["service_id"]
            person_identifier = data["person_identifier"]

            if service_id is None:
                return {
                    "error": "serviceId is required in the body of the request"
                }, HTTPStatus.BAD_REQUEST

            if person_identifier is None:
                return {
                    "error": "personIdentifier is required in the body of the request"
                }, HTTPStatus.BAD_REQUEST

            # Check for existing, not completed applications for a child.
            check_result, check_status = DraftService.check_existing_application_for_child(service_id,
                                                                                           person_identifier, None,
                                                                                           **kwargs)

            return check_result, check_status

        except Exception as ex:
            current_app.logger.error(f"Error in DraftCheckExistingForChild: {ex}")
            return {
                "error": f"Error checking for application for child: {ex}"
            }, HTTPStatus.INTERNAL_SERVER_ERROR
