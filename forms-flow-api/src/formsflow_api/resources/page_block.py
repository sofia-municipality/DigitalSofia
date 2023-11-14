from http import HTTPStatus
from flask import current_app, request
from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.exceptions import BusinessException
from formsflow_api_utils.utils.constants import PAGE_ADMIN_GROUP

from formsflow_api.schemas import PageBlockSchema, PageBlockListSchema
from formsflow_api.services import PageBlockService
from formsflow_api_utils.utils import (
    NEW_APPLICATION_STATUS,
    auth,
    cors_preflight,
    profiletime,
)

API = Namespace("Page Blocks", description="Manage Page blocks")

page_block = API.model(
    "Page Block",
    {
        "id": fields.Integer(),
        "machine-name": fields.String(),
        "page": fields.String(),
        "attributes": fields.Raw(),
    },
)


page_blocks = API.model(
    "List of Page Block",
    {
        "page-blocks": fields.List(fields.Nested(page_block))
    }
)

@cors_preflight("GET,POST,OPTIONS")
@API.route("", methods=["GET", "POST"])
class PageBlocksResource(Resource):
    """ Resource """

    @staticmethod
    @profiletime
    @API.doc(
        params={
            "page": {
                "in": "query",
                "description": "Filter page blocks for a specific page",
                "default": "home"
            }
        }
    )
    @API.response(200, "OK:- Successful request.", model=page_blocks)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get():
        """ Listing page blocks """
        try:
            # token = request.header[""]
            dict_data = PageBlockListSchema().load(request.args) or {}
            page_block_list,count = PageBlockService.get_all_page_blocks(dict_data)
            result = {
                "page_blocks": page_block_list,
                "count": count
            }

            return result, HTTPStatus.OK
        except BaseException as submission_err:
            response, status = {
                "type": "Bad request error",
                "message": "Invalid submission request passed",
            }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status
        
        
@cors_preflight("GET,PUT,DELETE,OPTIONS")
@API.route("/<int:page_block_id>", methods=["GET", "PUT", "DELETE", "OPTIONS"])
class PageBlockResourceById(Resource):
    """Resource for managing page blocks by id."""

    @staticmethod
    @auth.require
    @profiletime
    @API.response(200, "OK:- Successful request.", model=page_block)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get(page_block_id: int):
        """Get draft by id."""
        try:
            return PageBlockService.get_page_block(page_block_id), HTTPStatus.OK
        except BusinessException as err:
            current_app.logger.warning(err)
            return err.error, err.status_code

    @staticmethod
    @auth.require
    @profiletime
    @API.doc(body=page_block)
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
    def put(page_block_id: int):
        """Update page block details."""
        request_json = request.get_json()
        if not auth.has_role([PAGE_ADMIN_GROUP]):
            return (
                f"FORBIDDEN:- Permission denied",
                HTTPStatus.FORBIDDEN,
            )
        try:
            schema = PageBlockSchema()
            data = schema.load(request_json)
            PageBlockService.update_page_block(page_block_id=page_block_id, data=data)
            return (
                f"Updated {page_block_id} successfully",
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