from formsflow_api.models.user_status_transaction import UserStatusTransaction


class UserStatusTransactionService:
    @staticmethod
    def create_user_status_transaction(user_identifier: str) -> UserStatusTransaction:
        user_status_transaction = UserStatusTransaction.insert(user_identifier)
        return user_status_transaction
    
    @staticmethod
    def delete_user_status_transaction(user_identifier: str) -> UserStatusTransaction:
        user_status_transaction = UserStatusTransaction.select_by_user_identifier(user_identifier)
        if not user_status_transaction:
            raise Exception("User status transaction not found!")
        
        user_status_transaction.delete()

    @staticmethod
    def select_all_transactions() -> list[UserStatusTransaction]:
        user_status_transactions = UserStatusTransaction.select_all()
        return user_status_transactions
