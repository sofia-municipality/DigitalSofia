"""Bring in the common JWT Manager."""
from flask_jwt_oidc import JwtManager

# lower case name as used by convention in most Flask apps
jwt = JwtManager()  # pylint: disable=invalid-name
