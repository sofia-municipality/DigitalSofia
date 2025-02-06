from datetime import datetime, timedelta

from formsflow_api.models.login_event import LoginEventModel


class LoginEventService:
    @staticmethod
    def add_user_login_event(user_identifier: str) -> None:
        user_login_event = LoginEventModel.get_by_user_identifier(user_identifier)
        if not user_login_event:
            LoginEventModel.insert_default_with_user_identifier(user_identifier)
            return

        now = datetime.now()
        user_login_event.update_login_event(now)

    @staticmethod
    def select_events_of_unactive_users() -> list[LoginEventModel]:
        expiration_time = timedelta(days=30)
        now = datetime.now()
        expiration_datetime = now - expiration_time
        login_events = LoginEventModel.select_events_for_unactive_users_by_criteria(expiration_datetime)
        return login_events
    
    @staticmethod
    def delete_user_login_event(user_identifier: str) -> None:
        LoginEventModel.delete_by_user_identifier(user_identifier)

    @staticmethod
    def add_user_have_service(user_identifier: str) -> None:
        LoginEventModel.set_have_service_by_identifier(user_identifier)

    @staticmethod
    def add_user_is_official(user_identifier: str) -> None:
        LoginEventModel.set_is_official_by_identifier(user_identifier)
