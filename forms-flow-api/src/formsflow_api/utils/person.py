from http import HTTPStatus
from formsflow_api_utils.exceptions import BusinessException

import re


def validate_person_identifier(person_identifier):
    match = re.findall(r"PNOBG-(\d{10})$", person_identifier)

    if not match:
        raise BusinessException(
            "Invalid Personal Identifier bound to user.", HTTPStatus.BAD_REQUEST
        )

    return match[0]
