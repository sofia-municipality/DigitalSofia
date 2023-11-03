"""API endpoints for managing languages API resource."""
from flask import current_app, request
from http import HTTPStatus

from flask_restx import Namespace, Resource, fields
from formsflow_api_utils.utils import cors_preflight, profiletime

from flask import current_app

from formsflow_api.models import Region
from formsflow_api.schemas.address_kad import AddressKADListSchema
from formsflow_api.schemas.address_kra import AddressKRAListSchema
from formsflow_api.schemas.region import RegionSchema
from formsflow_api.services.address import AddressService

API = Namespace("Addresses", description="Addresses")
region = API.model(
    "Region",
    {
        "code": fields.String(),
        "name": fields.String(),
        "modified": fields.String(),
        "created": fields.String(),
        "city_are_code": fields.Integer(),
        "reference_number_code": fields.String(),
    },
)

regions = API.model(
    "Regions",
    {
        "regions": fields.List(
            fields.Nested(region, description="List of regions")
        ),
    },
)

address_kra = API.model(
    "AddressKRA",
    {
        "name_pa": fields.String(),
    },
)

addresses_kra = API.model(
    "AddressesKRA",
    {
        "streets": fields.List(
            fields.Nested(address_kra, description="List of streets")
        ),
        "pages": fields.Integer(),
        "total": fields.Integer(),
    },
)

address_kad = API.model(
    "AddressKAD",
    {
        "building_number": fields.String(),
        "region_id": fields.Integer(),
    },
)

addresses_kad = API.model(
    "AddressesKAD",
    {
        "addresses": fields.List(
            fields.Nested(address_kad, description="List of addresses from KAD")
        ),
        "pages": fields.Integer(),
        "total": fields.Integer(),
    },
)


@API.route("/regions", methods=["GET", "OPTIONS"])
class RegionsResource(Resource):
    """Resource for getting regions in Sofia"""

    @staticmethod
    @profiletime
    @API.response(200, "OK:- Successful request.", model=regions)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get():
        """Retrieve regions"""
        schema = RegionSchema()
        return schema.dump(Region.get_all(), many=True)


@API.route("/kra", methods=["GET", "OPTIONS"])
class FAQResource(Resource):
    """Resource for getting streets in Sofia"""

    @staticmethod
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
                "default": "name",
            },
            "sortOrder": {
                "in": "query",
                "description": "Specify sorting  order.",
                "default": "desc",
            },
            "name": {
                "in": "query",
                "description": "Filter addresses by str. name.",
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
    @API.response(200, "OK:- Successful request.", model=addresses_kra)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get():
        """Retrieve street from KPA."""
        try:
            query_data = AddressKRAListSchema().load(request.args) or {}
            current_app.logger.warning(query_data)
            address_list, page_count, total = AddressService.get_addresses_from_kra(query_data)

            result = {
                'streets': address_list,
                'pages': page_count,
                'total': total
            }

            return result, HTTPStatus.OK

        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status


@API.route("/kad", methods=["GET", "OPTIONS"])
class FAQResource(Resource):
    """Resource for getting Building number and region in Sofia."""

    @staticmethod
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
                "default": "name",
            },
            "sortOrder": {
                "in": "query",
                "description": "Specify sorting  order.",
                "default": "desc",
            },
            "name": {
                "in": "query",
                "description": "Filter addresses by str. name.",
                "type": "boolean",
            },
            "buildingNumber": {
                "in": "query",
                "description": "Filter addresses by building number.",
                "type": "boolean",
            },
            "region_id": {
                "in": "query",
                "description": "Filter addresses by region.",
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
    @API.response(200, "OK:- Successful request.", model=addresses_kad)
    @API.response(
        400,
        "BAD_REQUEST:- Invalid request.",
    )
    def get():
        """Retrieve building and region from KAD ."""
        try:
            query_data = AddressKADListSchema().load(request.args) or {}
            address_list, page_count, total = AddressService.get_addresses_from_kad(query_data)

            result = {
                'addresses': address_list,
                'pages': page_count,
                'total': total
            }

            return result, HTTPStatus.OK
        except BaseException as submission_err:  # pylint: disable=broad-except
            response, status = {
                                   "type": "Bad request error",
                                   "message": "Invalid submission request passed",
                               }, HTTPStatus.BAD_REQUEST
            current_app.logger.warning(response)
            current_app.logger.warning(submission_err)
            return response, status
