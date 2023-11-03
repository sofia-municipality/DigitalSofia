"""Application Specific Exceptions, to manage the business errors.

@log_error - a decorator to automatically log the exception to the logger provided

BusinessException - error, status_code - Business rules error
error - a description of the error {code / description: classname / full text}
status_code - where possible use HTTP Error Codes
"""
import json
from enum import Enum
from http import HTTPStatus

from flask import Response


class Error(Enum):
    """Error Codes."""

    DUPLICATE_TENANT_KEY = 'Tenant with key or name already exists', HTTPStatus.BAD_REQUEST
    INVALID_BPM_AUTHORIZATION = 'Invalid BPM Authorization', HTTPStatus.BAD_REQUEST
    INVALID_ANALYTICS_ORG = 'Invalid Analytics Organization', HTTPStatus.BAD_REQUEST
    INVALID_KEYCLOAK_REQUEST = 'Invalid Keycloak Request', HTTPStatus.BAD_REQUEST

    def __new__(cls, message, status):
        """Attributes for the enum."""
        obj = object.__new__(cls)
        obj.message = message
        obj.status = status
        return obj


class BusinessException(Exception):  # noqa
    """Exception that adds error code and error name, that can be used for i18n support."""

    def __init__(self, error: Error, *args, **kwargs):
        """Return a valid BusinessException."""
        super(BusinessException, self).__init__(*args, **kwargs)  # pylint:disable=super-with-arguments
        self.message = error.message
        self.status = error.status
        self.code = error.name

    def error(self):
        """Return problem+json of error message."""
        return {
            'code': self.code,
            'message': self.message
        }

    def response(self):
        """Response attributes."""
        return Response(response=json.dumps(self.error()), mimetype='application/json', status=self.status)
