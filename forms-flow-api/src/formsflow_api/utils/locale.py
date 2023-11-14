from flask import current_app, request
from sqlalchemy_utils import TranslationHybrid


def get_locale():
    current_header = request.headers.get("Accept-Language", "bg")
    current_app.logger.warning(f"Current locale is - {current_header}")
    return current_header

translation_hybrid = TranslationHybrid(
    current_locale=get_locale,
    default_locale='en',
    default_value=None
)