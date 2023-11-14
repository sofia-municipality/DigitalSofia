"""All of the configuration for the service is captured here.

All items are loaded,
or have Constants defined here that are loaded into the Flask configuration.
All modules and lookups get their configuration from the Flask config,
rather than reading environment variables directly or by accessing this configuration directly.
"""

import os
import sys

from dotenv import find_dotenv, load_dotenv


# this will load all the envars from a .env file located in the project root (api)
load_dotenv(find_dotenv())

CONFIGURATION = {
    'development': 'admin_api.config.DevConfig',
    'testing': 'admin_api.config.TestConfig',
    'production': 'admin_api.config.ProdConfig',
    'default': 'admin_api.config.ProdConfig'
}


def get_named_config(config_name: str = 'production'):
    """Return the configuration object based on the name.

    :raise: KeyError: if an unknown configuration is requested
    """
    if config_name in ['production', 'staging', 'default']:
        config = ProdConfig()
    elif config_name == 'testing':
        config = TestConfig()
    elif config_name == 'development':
        config = DevConfig()
    elif config_name == 'migration':
        config = MigrationConfig()
    else:
        raise KeyError(f"Unknown configuration '{config_name}'")
    return config


def _get_config(config_key: str, **kwargs):
    """Get the config from environment, and throw error if there are no default values and if the value is None."""
    if 'default' in kwargs:
        value = os.getenv(config_key, kwargs.get('default'))
    else:
        value = os.getenv(config_key)
        # assert value TODO Un-comment once we find a solution to run pre-hook without initializing app
    return value


class _Config():  # pylint: disable=too-few-public-methods
    """Base class configuration that should set reasonable defaults for all the other configurations."""

    PROJECT_ROOT = os.path.abspath(os.path.dirname(__file__))

    SECRET_KEY = 'a secret'

    SQLALCHEMY_TRACK_MODIFICATIONS = False

    ALEMBIC_INI = 'migrations/alembic.ini'

    # POSTGRESQL
    DB_USER = _get_config('DATABASE_USERNAME')
    DB_PASSWORD = _get_config('DATABASE_PASSWORD')
    DB_NAME = _get_config('DATABASE_NAME')
    DB_HOST = _get_config('DATABASE_HOST')
    DB_PORT = _get_config('DATABASE_PORT', default='5432')
    SQLALCHEMY_DATABASE_URI = _get_config(
        'DATABASE_URL', default=f'postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{int(DB_PORT)}/{DB_NAME}')
    SQLALCHEMY_ECHO = _get_config('SQLALCHEMY_ECHO', default='False').lower() == 'true'

    # JWT_OIDC Settings
    JWT_OIDC_WELL_KNOWN_CONFIG = _get_config('JWT_OIDC_WELL_KNOWN_CONFIG')
    JWT_OIDC_ALGORITHMS = _get_config('JWT_OIDC_ALGORITHMS', default='RS256')
    JWT_OIDC_JWKS_URI = _get_config('JWT_OIDC_JWKS_URI', default=None)
    JWT_OIDC_ISSUER = _get_config('JWT_OIDC_ISSUER')
    JWT_OIDC_AUDIENCE = _get_config('JWT_OIDC_AUDIENCE')
    JWT_OIDC_CLIENT_SECRET = _get_config('JWT_OIDC_CLIENT_SECRET', default=None)
    JWT_OIDC_CACHING_ENABLED = _get_config('JWT_OIDC_CACHING_ENABLED', default=False)
    JWT_OIDC_JWKS_CACHE_TIMEOUT = int(_get_config('JWT_OIDC_JWKS_CACHE_TIMEOUT', default=300))

    # Keycloak Admin Service
    KEYCLOAK_URL = os.getenv('KEYCLOAK_URL')
    KEYCLOAK_URL_REALM = os.getenv('KEYCLOAK_URL_REALM')
    KEYCLOAK_ADMIN_CLIENT = os.getenv('KEYCLOAK_ADMIN_CLIENT')
    KEYCLOAK_ADMIN_SECRET = os.getenv('KEYCLOAK_ADMIN_SECRET')
    BPM_CLIENT_SECRET = os.getenv("BPM_CLIENT_SECRET")

    FORMSFLOW_WEB_URL = os.getenv('FORMSFLOW_WEB_URL')
    FORMSFLOW_BPM_URL = os.getenv('BPM_API_URL')
    FORMSFLOW_INSIGHTS_URL = os.getenv('INSIGHT_API_URL')
    FORMSFLOW_INSIGHTS_API_KEY = os.getenv('INSIGHT_API_KEY')

    TEMP_PASSWORD = os.getenv('TEMP_PASSWORD', 'formsflow')

    # Formio url
    FORMIO_URL = os.getenv("FORMIO_URL")
    FORMIO_PROJECT_URL = os.getenv("FORMIO_PROJECT_URL")  # for form.io enterprise
    FORMIO_USERNAME = os.getenv("FORMIO_ROOT_EMAIL")
    FORMIO_PASSWORD = os.getenv("FORMIO_ROOT_PASSWORD")
    FORMIO_JWT_SECRET = os.getenv("FORMIO_JWT_SECRET", "---- change me now ---")

    # Trial account period in days
    TRIAL_PERIOD = int(os.getenv("TRIAL_PERIOD", "30"))

    TESTING = False
    DEBUG = True


class DevConfig(_Config):  # pylint: disable=too-few-public-methods
    """Dev config."""

    TESTING = False
    DEBUG = True


class TestConfig(_Config):  # pylint: disable=too-few-public-methods
    """In support of testing only used by the py.test suite."""

    DEBUG = True
    TESTING = True

    # POSTGRESQL
    SQLALCHEMY_DATABASE_URI = os.getenv("DATABASE_URL_TEST")

    JWT_OIDC_TEST_MODE = True

    # JWT_OIDC Settings
    JWT_OIDC_TEST_AUDIENCE = os.getenv("JWT_OIDC_AUDIENCE")
    JWT_OIDC_TEST_ISSUER = os.getenv("JWT_OIDC_ISSUER")
    JWT_OIDC_TEST_WELL_KNOWN_CONFIG = os.getenv("JWT_OIDC_WELL_KNOWN_CONFIG")
    JWT_OIDC_TEST_ALGORITHMS = "RS256"
    JWT_OIDC_TEST_JWKS_URI = os.getenv("JWT_OIDC_JWKS_URI")
    JWT_OIDC_TEST_JWKS_CACHE_TIMEOUT = 6000

    # Keycloak Service for BPM Camunda
    KEYCLOAK_URL_REALM = os.getenv("KEYCLOAK_URL_REALM", default="forms-flow-ai")
    KEYCLOAK_URL = os.getenv("KEYCLOAK_URL", default="http://localhost:8081")

    # Use docker to spin up mocks
    USE_DOCKER_MOCK = os.getenv("USE_DOCKER_MOCK", "False").lower() == "true"

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

    JWT_OIDC_TEST_PRIVATE_KEY_PEM = """-----BEGIN RSA PRIVATE KEY-----\nMIICXQIBAAKBgQDfn1nKQshOSj8xw44oC2klFWSNLmK3Bn
    HONCJ1bZfq0EQ5gIfgtlvB+Px8Ya+VS3OnK7Cdi4iU1fxO9ktN6c6TjmmmFevk8wIwqLthmCSF3r+3+h4eddj7hucMsXWv05QUrCPoL6YUUz7Cgpz7
    ra24rpAmK5z7lsV+f3BEvXkrUQIDAQABAoGAC0G3QGI6OQ6tvbCNYGCqq043YI/8MiBl7C5dqbGZmx1ewdJBhMNJPStuckhskURaDwk4+8VBW9Slvc
    fSJJrnZhgFMjOYSSsBtPGBIMIdM5eSKbenCCjO8Tg0BUh/xa3CHST1W4RQ5rFXadZ9AeNtaGcWj2acmXNO3DVETXAX3x0CQQD13LrBTEDR44eilQ/4
    TlCMPO5bytd1pAxHnrqgMnWovSIPSShAAH1feFugH7ZGu7RoBO7pYNb6N3iaC1idc7yjAkEA6Nfc6c8meTRkVRAHCF24LB5GLfsjoMB0tOeEO9w9Ou
    s1a4o+D24bAePMUImAp3woFoNDRfWtlNktOqLel5PjewJBAN9kBoA5o6/Rl9zeqdsIdWFmv4DB5lEqlEnC7HlAP+3oo3jWFO9KQqArQL1V8w2D4aCd
    0uJULiC9pCP7aTHvBhcCQQDbW0mOp436T6ZaELBfbFNulNLOzLLi5YzNRPLppfG1SRNZjbIrvTIKVL4N/YxLvQbTNrQw+2OdQACBJiEHsdZzAkBcsT
    k7frTH4yGx0VfHxXDPjfTj4wmD6gZIlcIr9lZg4H8UZcVFN95vEKxJiLRjAmj6g273pu9kK4ymXNEjWWJn\n-----END RSA PRIVATE KEY-----"""


class ProdConfig(_Config):  # pylint: disable=too-few-public-methods
    """Production environment configuration."""

    SECRET_KEY = _get_config('SECRET_KEY', default=None)

    if not SECRET_KEY:
        SECRET_KEY = os.urandom(24)
        print('WARNING: SECRET_KEY being set as a one-shot', file=sys.stderr)

    TESTING = False
    DEBUG = False


class MigrationConfig():  # pylint: disable=too-few-public-methods
    """Config for db migration."""

    TESTING = False
    DEBUG = True

    # POSTGRESQL
    DB_USER = _get_config('DATABASE_USERNAME')
    DB_PASSWORD = _get_config('DATABASE_PASSWORD')
    DB_NAME = _get_config('DATABASE_NAME')
    DB_HOST = _get_config('DATABASE_HOST')
    DB_PORT = _get_config('DATABASE_PORT', default='5432')
    SQLALCHEMY_DATABASE_URI = f'postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{int(DB_PORT)}/{DB_NAME}'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
