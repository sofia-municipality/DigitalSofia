from .namespace import API
from .list import KeycloakUsersList
from .locale import KeycloakUserService
from .user_id.permission import UserPermission
from .delete import UserDelete
from .login_event import LoginEvent

API.add_resource(KeycloakUsersList)
API.add_resource(KeycloakUserService)
API.add_resource(UserPermission)
API.add_resource(UserDelete)
API.add_resource(LoginEvent)
