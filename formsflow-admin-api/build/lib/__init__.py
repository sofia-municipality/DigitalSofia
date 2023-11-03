"""The Reports API service.

This module is the API for the EAO Reports system.
"""

import os

from flask import Flask

from admin_api import config
from admin_api.config import _Config
from admin_api.models import db
from admin_api.utils.auth import jwt
from admin_api.utils.logging import setup_logging
from admin_api.utils.run_version import get_run_version

setup_logging(os.path.join(_Config.PROJECT_ROOT, 'logging.conf'))


def create_app(run_mode=os.getenv('FLASK_ENV', 'production')):
    """Return a configured Flask App using the Factory method."""
    app = Flask(__name__)
    app.config.from_object(config.CONFIGURATION[run_mode])

    db.init_app(app)

    # pylint: disable=import-outside-toplevel
    from admin_api.resources import API_BLUEPRINT, OPS_BLUEPRINT

    app.register_blueprint(API_BLUEPRINT)
    app.register_blueprint(OPS_BLUEPRINT)

    setup_jwt_manager(app, jwt)

    @app.after_request
    def add_version(response):  # pylint: disable=unused-variable
        version = get_run_version()
        response.headers['API'] = f'admin_api/{version}'
        return response

    register_shellcontext(app)

    return app


def setup_jwt_manager(app, jwt_manager):
    """Use flask app to configure the JWTManager to work for a particular Realm."""

    def get_roles(a_dict):
        return a_dict['realm_access']['roles']  # pragma: no cover

    app.config['JWT_ROLE_CALLBACK'] = get_roles

    jwt_manager.init_app(app)


def register_shellcontext(app):
    """Register shell context objects."""
    from admin_api import models  # pylint: disable=import-outside-toplevel

    def shell_context():
        """Shell context objects."""
        return {'app': app, 'jwt': jwt, 'db': db, 'models': models}  # pragma: no cover

    app.shell_context_processor(shell_context)
