"""All of the configuration for the api is captured here.

All items are loaded, or have Constants defined here that
are loaded into the Flask configuration.
All modules and lookups get their configuration from the
Flask config, rather than reading environment variables directly
or by accessing this configuration directly.
"""

import os

from dotenv import find_dotenv, load_dotenv

# this will load all the envars from a .env file located in the project root (api)
load_dotenv(find_dotenv())

CONFIGURATION = {
    "development": "formsflow_api.config.DevConfig",
    "testing": "formsflow_api.config.TestConfig",
    "production": "formsflow_api.config.ProdConfig",
    "default": "formsflow_api.config.ProdConfig",
}


def get_named_config(config_name: str = "production"):
    """Return the configuration object based on the name.

    :raise: KeyError: if an unknown configuration is requested
    """
    if config_name in ["production", "staging", "default"]:
        config = ProdConfig()
    elif config_name == "testing":
        config = TestConfig()
    elif config_name == "development":
        config = DevConfig()
    else:
        raise KeyError(f"Unknown configuration '{config_name}'")
    return config


class _Config:  # pylint: disable=too-few-public-methods
    """Base class configuration.

    that should set reasonable defaults for all the other configurations.
    """

    PROJECT_ROOT = os.path.abspath(os.path.dirname(__file__))

    SECRET_KEY = "secret value"

    SQLALCHEMY_TRACK_MODIFICATIONS = False

    ALEMBIC_INI = "migrations/alembic.ini"

    # POSTGRESQL
    SQLALCHEMY_DATABASE_URI = os.getenv("DATABASE_URL", "")

    TESTING = False
    DEBUG = False

    # JWT_OIDC Settings
    JWT_OIDC_WELL_KNOWN_CONFIG = os.getenv("JWT_OIDC_WELL_KNOWN_CONFIG")
    JWT_OIDC_ALGORITHMS = os.getenv("JWT_OIDC_ALGORITHMS", "RS256")
    JWT_OIDC_JWKS_URI = os.getenv("JWT_OIDC_JWKS_URI")
    JWT_OIDC_ISSUER = os.getenv("JWT_OIDC_ISSUER")
    JWT_OIDC_AUDIENCE = os.getenv("JWT_OIDC_AUDIENCE")
    JWT_OIDC_CACHING_ENABLED = os.getenv("JWT_OIDC_CACHING_ENABLED")
    JWT_OIDC_JWKS_CACHE_TIMEOUT = 300

    # Keycloak Service for BPM Camunda
    BPM_TOKEN_API = os.getenv("BPM_TOKEN_API")
    BPM_CLIENT_ID = os.getenv("BPM_CLIENT_ID")
    BPM_CLIENT_SECRET = os.getenv("BPM_CLIENT_SECRET")
    BPM_GRANT_TYPE = os.getenv("BPM_GRANT_TYPE", "client_credentials")

    # BPM Camunda Details
    BPM_API_URL = os.getenv("BPM_API_URL")

    # API Base URL (Self)
    FORMSFLOW_API_URL = os.getenv("WEB_API_BASE_URL")
    # Analytics API End points
    ANALYTICS_API_URL = os.getenv("INSIGHT_API_URL")
    ANALYTICS_API_KEY = os.getenv("INSIGHT_API_KEY")

    # Keycloak Admin Service
    KEYCLOAK_URL = os.getenv("KEYCLOAK_URL")
    KEYCLOAK_URL_REALM = os.getenv("KEYCLOAK_URL_REALM")

    # Web url
    WEB_BASE_URL = os.getenv("WEB_BASE_URL")

    # eForm Integrations
    EFORM_INTEGRATIONS_URL = os.getenv("EFORM_INTEGRATIONS_URL")
    EDELIVERY_FILE_UPLOAD_ADDRESS = os.getenv("EDELIVERY_FILE_UPLOAD_ADDRESS", "upload/blobs")
    # We are using the profile id for the eDelivery file upload when the upload address is onBehalf -> upload/obo/blobs
    EDELIVERY_FILE_UPLOAD_PROFILE_ID = os.getenv("EDELIVERY_FILE_UPLOAD_PROFILE_ID", "000696327000001")

    # Formio url
    FORMIO_URL = os.getenv("FORMIO_URL")
    FORMIO_WEB_URL = os.getenv("FORMIO_WEB_URL")
    FORMIO_USERNAME = os.getenv("FORMIO_ROOT_EMAIL")
    FORMIO_PASSWORD = os.getenv("FORMIO_ROOT_PASSWORD")
    FORMIO_PROJECT_URL = os.getenv("FORMIO_PROJECT_URL")  # For form.io enterprise
    FORMIO_FILE_RESOURCE_PATH = os.getenv("FORMIO_FILE_RESOURCE_PATH")
    MATEUS_COMPANY_ID = os.getenv("MATEUS_COMPANY_ID", default=999)

    # Keycloak client authorization enabled flag
    KEYCLOAK_ENABLE_CLIENT_AUTH = (
            str(os.getenv("KEYCLOAK_ENABLE_CLIENT_AUTH", default="false")).lower() == "true"
    )
    MULTI_TENANCY_ENABLED = (
            str(os.getenv("MULTI_TENANCY_ENABLED", default="false")).lower() == "true"
    )

    # Formio JWT Secret
    FORMIO_JWT_SECRET = os.getenv("FORMIO_JWT_SECRET", "--- change me now ---")
    # Form embed JWT Secret for custom authentication
    FORM_EMBED_JWT_SECRET = os.getenv(
        "FORM_EMBED_JWT_SECRET", "f6a69a42-7f8a-11ed-a1eb-0242ac120002"
    )

    # Firebase Variables
    FIREBASE_CREDENTIALS_PATH = os.getenv("FIREBASE_CREDENTIALS_PATH", None)
    OTHER_FILE_DESTINATION = os.getenv("OTHER_FILE_DESTINATION", None)
    MOBILE_LOGS_FILE_DESTINATION = os.getenv("MOBILE_LOGS_FILE_DESTINATION", None)

    # CRON JOB VARIABLES
    CRON_DOCUMENT_TRANSACTION_TIMEOUT = os.getenv(
        "CRON_DOCUMENT_TRANSACTION_TIMEOUT", 30
    )
    EUROTRUST_EXPIRE_TIMEOUT = os.getenv("EUROTRUST_EXPIRE_TIMEOUT", 30)
    EVROTRUST_IDENTITY_REQUESTS_MAX_RETRIES = os.getenv("EVROTRUST_IDENTITY_REQUESTS_MAX_RETRIES", 15)
    EVROTRUST_IDENTITY_AUTHENTICATE_TIMEOUT = os.getenv("EVROTRUST_IDENTITY_AUTHENTICATE_TIMEOUT", 3)
    EUROTRUST_IDENTITY_TIMEOUT = os.getenv("EUROTRUST_IDENTITY_TIMEOUT", 30)

    FORMSFLOW_DOC_API_URL = os.getenv("FORMSFLOW_DOC_API_URL")
    SIGN_SERVICE_API_URL = os.getenv("SIGN_SERVICE_API_URL")

    # Hardcoded camunda process definitions
    CAMUNDA_CHANGE_ADDRESS_PROCESS = os.getenv("CAMUNDA_CHANGE_ADDRESS_PROCESS", "Process_sofiade")
    REDIS_CONNECTION = os.getenv("REDIS_CONNECTION")
    REDIS_KEY_TTL = os.getenv("REDIS_KEY_TTL", 3600)
    REDIS_PASSWORD = os.getenv("REDIS_PASSWORD")  #90azoHH3e8

    FLASK_ENV = os.getenv("FLASK_ENV", "production")

    # Message Bus Configurations
    MESSAGE_BUS_URL = os.getenv("MESSAGE_BUS_URL", "amqp://guest:guest@rabbitmq//")
    MESSAGE_BUS_QUEUE = os.getenv("MESSAGE_BUS_QUEUE", "email_queue")
    MESSAGE_BUS_EXCHANGE = os.getenv("MESSAGE_BUS_EXCHANGE", "email_exchange")
    MESSAGE_BUS_ROUTING_KEY = os.getenv("MESSAGE_BUS_ROUTING_KEY", "email.send")
    MESSAGE_BUS_TYPE = os.getenv("MESSAGE_BUS_TYPE", "topic")
    MESSAGE_BUS_RETRY_DELAY = os.getenv("MESSAGE_BUS_RETRY_DELAY", 10)
    MESSAGE_BUS_MAX_RETRIES = os.getenv("MESSAGE_BUS_MAX_RETRIES", 10)
    MESSAGE_BUS_DEAD_LETTER_KEY = os.getenv("MESSAGE_BUS_DEAD_LETTER_KEY", "email.dead")
    MESSAGE_BUS_DEAD_QUEUE = os.getenv("MESSAGE_BUS_DEAD_QUEUE", "email_dead")
    MESSAGE_BUS_DEAD_EXCHANGE = os.getenv("MESSAGE_BUS_DEAD_EXCHANGE", "email_dead")

    # Notification Configurations
    TEMPLATES_LOCATION = os.getenv("TEMPLATES_LOCATION", "/forms-flow-api/app/src/formsflow_api/static/data/templates")
    SERVICE_URL = os.getenv("SERVICE_URL",
                            "http://host.docker.internal:8002/integrations/notifications/post-mail-notification")
    SUPPORT_EMAILS = os.getenv("SUPPORT_EMAILS", "support1@support.com;support2@support.com")
    NOTIFICATION_SCHEMA = os.getenv("NOTIFICATION_SCHEMA", "300,3600,3600,3600,3600")

    # Templates Configurations
    PAYMENT_RESULT = os.getenv("PAYMENT_RESULT", "mdt_payment_status-body-${language}.mako")
    MATEUS_NOTIFICATION = os.getenv("MATEUS_NOTIFICATION", "mdt_mateus-notification-failed.mako")

    # Regions Configuration
    REGIONS_ENV = os.getenv("REGIONS_ENV", "stage")
    REGIONS_SECRET_KEY = os.getenv("REGIONS_SECRET_KEY")

    # Mateus Payment Configuration
    MATEUS_PAYMENT_CLIENT_ID = os.getenv("MATEUS_PAYMENT_CLIENT_ID", "epayments_ais_client_c7fdbfea-4c1f-47fb-a599-e41a94bb651c")
    MATEUS_PAYMENT_CLIENT_SECRET = os.getenv("MATEUS_PAYMENT_CLIENT_SECRET", "R111ZAN6ZZVWPBBHD38Q2JMXK2EWWNHU")

class DevConfig(_Config):  # pylint: disable=too-few-public-methods
    """Development environment configuration."""

    TESTING = False
    DEBUG = True


class TestConfig(_Config):  # pylint: disable=too-few-public-methods
    """In support of testing only used by the py.test suite."""

    DEBUG = True
    TESTING = True

    FORMSFLOW_API_URL = os.getenv("WEB_API_BASE_URL")
    # POSTGRESQL
    SQLALCHEMY_DATABASE_URI = os.getenv("DATABASE_URL_TEST")

    JWT_OIDC_TEST_MODE = True
    # USE_TEST_KEYCLOAK_DOCKER = os.getenv("USE_TEST_KEYCLOAK_DOCKER")
    USE_DOCKER_MOCK = os.getenv("USE_DOCKER_MOCK", default=None)

    # JWT_OIDC Settings
    JWT_OIDC_TEST_AUDIENCE = os.getenv("JWT_OIDC_AUDIENCE")
    JWT_OIDC_TEST_ISSUER = os.getenv("JWT_OIDC_ISSUER")
    JWT_OIDC_TEST_WELL_KNOWN_CONFIG = os.getenv("JWT_OIDC_WELL_KNOWN_CONFIG")
    JWT_OIDC_TEST_ALGORITHMS = "RS256"
    JWT_OIDC_TEST_JWKS_URI = os.getenv("JWT_OIDC_JWKS_URI")
    JWT_OIDC_TEST_JWKS_CACHE_TIMEOUT = 6000

    # Keycloak Service for BPM Camunda
    KEYCLOAK_URL_REALM = os.getenv("KEYCLOAK_URL_REALM", default="eServices")
    KEYCLOAK_URL = os.getenv("KEYCLOAK_URL", default="http://localhost:8081")

    # Use docker to spin up mocks
    USE_DOCKER_MOCK = os.getenv("USE_DOCKER_MOCK", "False").lower() == "true"
    TEST_FORM_EMBED_JWT_SECRET = os.getenv(
        "TEST_FORM_EMBED_JWT_SECRET", "f6a69a42-7f8a-11ed-a1eb-0242ac120002"
    )

    JWT_OIDC_TEST_KEYS = {
        "keys": [
            {
                "kid": JWT_OIDC_TEST_AUDIENCE,
                "kty": "RSA",
                "alg": "RS256",
                "use": "sig",
                "n": "AN-fWcpCyE5KPzHDjigLaSUVZI0uYrcGcc40InVtl-rQRDmAh-C2W8H4_Hxhr5VLc6crsJ2LiJTV_E72S03pzpOOaaYV6-"
                     "TzAjCou2GYJIXev7f6Hh512PuG5wyxda_TlBSsI-gvphRTPsKCnPutrbiukCYrnPuWxX5_cES9eStR",
                "e": "AQAB",
            }
        ]
    }

    JWT_OIDC_TEST_PRIVATE_KEY_JWKS = {
        "keys": [
            {
                "kid": JWT_OIDC_TEST_AUDIENCE,
                "kty": "RSA",
                "alg": "RS256",
                "use": "sig",
                "n": "AN-fWcpCyE5KPzHDjigLaSUVZI0uYrcGcc40InVtl-rQRDmAh-C2W8H4_Hxhr5VLc6crsJ2LiJTV_E72S03pzpOOaaYV6-"
                     "TzAjCou2GYJIXev7f6Hh512PuG5wyxda_TlBSsI-gvphRTPsKCnPutrbiukCYrnPuWxX5_cES9eStR",
                "e": "AQAB",
                "d": "C0G3QGI6OQ6tvbCNYGCqq043YI_8MiBl7C5dqbGZmx1ewdJBhMNJPStuckhskURaDwk4-"
                     "8VBW9SlvcfSJJrnZhgFMjOYSSsBtPGBIMIdM5eSKbenCCjO8Tg0BUh_"
                     "xa3CHST1W4RQ5rFXadZ9AeNtaGcWj2acmXNO3DVETXAX3x0",
                "p": "APXcusFMQNHjh6KVD_hOUIw87lvK13WkDEeeuqAydai9Ig9JKEAAfV94W6Aftka7tGgE7ulg1vo3eJoLWJ1zvKM",
                "q": "AOjX3OnPJnk0ZFUQBwhduCweRi37I6DAdLTnhDvcPTrrNWuKPg9uGwHjzFCJgKd8KBaDQ0X1rZTZLTqi3peT43s",
                "dp": "AN9kBoA5o6_Rl9zeqdsIdWFmv4DB5lEqlEnC7HlAP-3oo3jWFO9KQqArQL1V8w2D4aCd0uJULiC9pCP7aTHvBhc",
                "dq": "ANtbSY6njfpPploQsF9sU26U0s7MsuLljM1E8uml8bVJE1mNsiu9MgpUvg39jEu9BtM2tDD7Y51AAIEmIQex1nM",
                "qi": "XLE5O360x-MhsdFXx8Vwz4304-MJg-oGSJXCK_ZWYOB_FGXFRTfebxCsSYi0YwJo-oNu96bvZCuMplzRI1liZw",
            }
        ]
    }

    JWT_OIDC_TEST_PRIVATE_KEY_PEM = """
-----BEGIN RSA PRIVATE KEY-----
MIICXQIBAAKBgQDfn1nKQshOSj8xw44oC2klFWSNLmK3BnHONCJ1bZfq0EQ5gIfg
tlvB+Px8Ya+VS3OnK7Cdi4iU1fxO9ktN6c6TjmmmFevk8wIwqLthmCSF3r+3+h4e
ddj7hucMsXWv05QUrCPoL6YUUz7Cgpz7ra24rpAmK5z7lsV+f3BEvXkrUQIDAQAB
AoGAC0G3QGI6OQ6tvbCNYGCqq043YI/8MiBl7C5dqbGZmx1ewdJBhMNJPStuckhs
kURaDwk4+8VBW9SlvcfSJJrnZhgFMjOYSSsBtPGBIMIdM5eSKbenCCjO8Tg0BUh/
xa3CHST1W4RQ5rFXadZ9AeNtaGcWj2acmXNO3DVETXAX3x0CQQD13LrBTEDR44ei
lQ/4TlCMPO5bytd1pAxHnrqgMnWovSIPSShAAH1feFugH7ZGu7RoBO7pYNb6N3ia
C1idc7yjAkEA6Nfc6c8meTRkVRAHCF24LB5GLfsjoMB0tOeEO9w9Ous1a4o+D24b
AePMUImAp3woFoNDRfWtlNktOqLel5PjewJBAN9kBoA5o6/Rl9zeqdsIdWFmv4DB
5lEqlEnC7HlAP+3oo3jWFO9KQqArQL1V8w2D4aCd0uJULiC9pCP7aTHvBhcCQQDb
W0mOp436T6ZaELBfbFNulNLOzLLi5YzNRPLppfG1SRNZjbIrvTIKVL4N/YxLvQbT
NrQw+2OdQACBJiEHsdZzAkBcsTk7frTH4yGx0VfHxXDPjfTj4wmD6gZIlcIr9lZg
4H8UZcVFN95vEKxJiLRjAmj6g273pu9kK4ymXNEjWWJn
-----END RSA PRIVATE KEY-----
"""


class ProdConfig(_Config):  # pylint: disable=too-few-public-methods
    """Production environment configuration."""

    SECRET_KEY = os.getenv("SECRET_KEY", None)
    SQLALCHEMY_ENGINE_OPTIONS = {
        "pool_pre_ping": True,
        "pool_recycle": 300,
    }

    if not SECRET_KEY:
        SECRET_KEY = os.urandom(24)
        # print("WARNING: SECRET_KEY being set as a one-shot", file=sys.stderr)

    TESTING = False
    DEBUG = False
