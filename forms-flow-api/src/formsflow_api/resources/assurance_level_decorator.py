from http import HTTPStatus
from flask import current_app
import inspect

def require_assurance_level(requiredLevel):
    def check_assurance_level_int(func):
        def wrapper(*args, **kwargs):

            if "user" not in kwargs:
                return "User not found in arguments", HTTPStatus.BAD_REQUEST
            
            user = kwargs.get('user')

            if not user:
                return "User not found in arguments", HTTPStatus.BAD_REQUEST

            token_info = user.token_info
            current_app.logger.debug(token_info)

            assuranceLevel = ""
            auth_provider = ""

            if "assurance_level" in token_info:
                assuranceLevel = token_info.get("assurance_level")

            if "auth_provider" in token_info:
                auth_provider = token_info.get("auth_provider")

            if not assuranceLevel:
                if not auth_provider:
                    return "Missing or empty 'auth_provider' and 'assurance_level' in the token", HTTPStatus.BAD_REQUEST 
                else:
                   if auth_provider.strip().upper() == "DIGITALSOFIA":
                       return func(*args, **kwargs)
                   else:
                       return f"Invalid value for 'auth_provider' in the token. Expected 'digitalSofia' but found '{auth_provider}'. 'assurance_level' not found in the token", HTTPStatus.FORBIDDEN
            else:
                if any(check.strip().upper() == assuranceLevel.strip().upper() for check in requiredLevel.split(",")):
                    return func(*args, **kwargs)
                else:
                    if auth_provider.strip().upper() == "DIGITALSOFIA":
                       return func(*args, **kwargs)
                    else:
                       return f"Invalid value for 'auth_provider' in the token. Expected 'digitalSofia' but found '{auth_provider}'. Invalid 'assurance_level' in the token: '{assuranceLevel}'", HTTPStatus.FORBIDDEN
           
        
        return wrapper  
    return check_assurance_level_int


    