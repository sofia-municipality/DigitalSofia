"""Meta information about the service.

Currently this only provides API versioning information
"""
from flask import jsonify
from flask_restx import Namespace, Resource

from admin_api.utils.run_version import get_run_version


API = Namespace('Meta', description='Metadata')


@API.route('/info')
class Info(Resource):
    """Meta information about the overall service."""

    @staticmethod
    def get():
        """Return a JSON object with meta information about the Service."""
        version = get_run_version()
        return jsonify(API=f'admin_api/{version}')
