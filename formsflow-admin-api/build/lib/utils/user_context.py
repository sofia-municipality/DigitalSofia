"""User Context to hold request scoped variables."""

import functools
from typing import Dict

from flask import g

from admin_api.constants import Role


def _get_context():
    """Return User context."""
    return UserContext()


class UserContext:  # pylint: disable=too-many-instance-attributes
    """Object to hold request scoped user context."""

    def __init__(self):
        """Return a User Context object."""
        token_info: Dict = _get_token_info()
        self._user_name: str = token_info.get('username', None) or token_info.get('preferred_username', None)
        self._first_name: str = token_info.get('firstname', None)
        # self._bearer_token: str = _get_token()
        self._roles: list = token_info.get('roles', None)
        self._sub: str = token_info.get('sub', None)
        self._tenant_key: str = token_info.get('tenantKey', None)
        self._name = token_info.get('name', None)
        self._email = token_info.get('email', None)

    @property
    def user_name(self) -> str:
        """Return the user_name."""
        return self._user_name.upper() if self._user_name else None

    @property
    def first_name(self) -> str:
        """Return the user_name."""
        return self._first_name

    # @property
    # def bearer_token(self) -> str:
    #     """Return the bearer_token."""
    #     return self._bearer_token

    @property
    def roles(self) -> list:
        """Return the roles."""
        return self._roles

    @property
    def sub(self) -> str:
        """Return the subject."""
        return self._sub

    @property
    def tenant_key(self) -> str:
        """Return the tenant_key."""
        return self._tenant_key

    def has_role(self, role_name: str) -> bool:
        """Return True if the user has the role."""
        return role_name in self._roles

    def is_admin(self) -> bool:
        """Return True if the user is admin user."""
        return Role.ADMIN.value in self._roles if self._roles else False

    def is_tenant_admin(self) -> bool:
        """Return True if the user is tenant admin user."""
        return Role.TENANT_ADMIN.value in self._roles if self._roles else False

    @property
    def name(self) -> str:
        """Return the name."""
        return self._name

    @property
    def email(self) -> str:
        """Return the email."""
        return self._email


def user_context(function):
    """Add user context object as an argument to function."""

    @functools.wraps(function)
    def wrapper(*func_args, **func_kwargs):
        context = _get_context()
        func_kwargs['user'] = context
        return function(*func_args, **func_kwargs)

    return wrapper


def _get_token_info() -> Dict:
    return g.jwt_oidc_token_info if g and 'jwt_oidc_token_info' in g else {}
