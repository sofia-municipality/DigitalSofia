from http import HTTPStatus
from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils import (
    NEW_APPLICATION_STATUS,
    auth,
    cors_preflight,
    profiletime,
)
from formsflow_api_utils.utils.constants import PAGE_ADMIN_GROUP

from formsflow_api.services import FAQService
from formsflow_api.schemas import FAQSchema, FAQListSchema

API = Namespace("FAQ", description="Manage FAQ")

faq = API.model(
    "FAQ",
    {
        # "id": fields.Integer(),
        "title": fields.String(),
        "content": fields.String(),
        "isFavoured": fields.Boolean()
    }
)

faq_response = API.inherit(
    "FAQ Response",
    faq,
    {
        "created": fields.String(),
        "id": fields.Integer(),
        "modified": fields.String(),
    }
)

faqs = API.model(
    "FAQ List",
    {
        "faqs": fields.List(
            fields.Nested(faq_response, description="List of FAQ")
        ),
        "pages": fields.Integer(),
        "total": fields.Integer(),
    },
)

message = API.model("Message", {"message": fields.String()})


@cors_preflight("GET,POST,OPTIONS")
@API.route("", methods=["GET", "POST", "OPTIONS"])
class FAQResource(Resource):
    """Resource for managing drafts."""

    @staticmethod
    @profiletime
    @API.doc(
        params={
            "Accepted-Language": {
                "in": "header",
                "description": "The respective language",
                "default": "bg"
            },
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
            "isFavoured": {
                "in": "query",
                "description": "Specify sorting  order.",
                "type": "boolean",
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
    @API.response(200, "OK:- Successful request.", model=faqs)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        401,
        "UNAUTHORIZED:- Authorization header not provided or an invalid token passed.",
    )
    def get():
        """Retrieve faqs."""
        try:
            query_data = FAQListSchema().load(request.args) or {}
            faq_list, page_count, total = FAQService.get_faqs(query_data)
            current_app.logger.warning(query_data)

            result = {
                'faqs': faq_list,
                'pages': page_count,
                'total': total
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
    @API.doc(
        body=faq,
        params={
            "Accepted-Language": {
                "in": "header",
                "description": "The respective language",
                "default": "bg"
            },
        }
    )
    @API.response(201, "CREATED:- Successful request.", model=faq_response)
    @API.response(
        403,
        "FORBIDDEN:- Permission denied",
    )
    def post():
        """Create a faq item."""
        if not auth.has_role([PAGE_ADMIN_GROUP]):
            return (
                f"FORBIDDEN:- Permission denied",
                HTTPStatus.FORBIDDEN,
            )
        try:
            faq_json = request.get_json()
            faq_schema = FAQSchema()
            faq_dict_data = faq_schema.load(faq_json)

            faq_object = FAQService.create_faq(faq_dict_data)

            result = faq_schema.dump(faq_object)
            return result, HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err)
            response, status = err.error, err.status_code
            return response, status
        except BaseException as exception:
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(exception)
            return response, status


@cors_preflight("GET,PUT,OPTIONS,DELETE")
@API.route("/<int:faq_id>", methods=["GET", "PUT", "DELETE", "OPTIONS"])
class FAQResourceById(Resource):
    """ Resource for managing faqs by id. """

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(
        params={
            "Accepted-Language": {
                "in": "header",
                "description": "The respective language",
                "default": "bg"
            }
        }
    )
    @API.response(200, "OK:- Successful request.", model=faq_response)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get(faq_id: int):
        """ Retrieving a FAQ item """
        try:
            schema = FAQSchema()
            faq = FAQService.get(faq_id)
            return schema.dump(faq), HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err)
            response, status = err.error, err.status_code
            return response, status

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(
        body=faq,
        params={
            "Accepted-Language": {
                "in": "header",
                "description": "The respective language",
                "default": "bg"
            }
        }
    )
    @API.response(
        200,
        "OK:- Successful request. Returns ```str: success message```",
    )
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    @API.response(
        403,
        "FORBIDDEN:- Permission denied",
    )
    @API.response(
        404,
        "NOT_FOUND:- Invalid request.",
    )
    def put(faq_id: int):
        if not auth.has_role([PAGE_ADMIN_GROUP]):
            return (
                f"FORBIDDEN:- Permission denied",
                HTTPStatus.FORBIDDEN,
            )
        """ Update a FAQ item, with the corresponding language set """
        body = request.get_json()
        try:
            schema = FAQSchema()
            data = schema.load(body)
            FAQService.update(faq_id=faq_id, data=data)
            return (
                {
                    "type": "Success",
                    "message": f"Updated FAQ {faq_id} successfully",
                },
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
    @API.response(
        403,
        "FORBIDDEN:- Permission denied",
    )
    def delete(faq_id: int):
        if not auth.has_role([PAGE_ADMIN_GROUP]):
            return (
                f"FORBIDDEN:- Permission denied",
                HTTPStatus.FORBIDDEN,
            )
        """ Delete a FAQ item """
        try:
            FAQService.delete(faq_id)
            return {"type": "Success", "message": "Draft deleted successfully"}, HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err)
            return err.error, err.status_code
