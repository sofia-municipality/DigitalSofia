class EFormIntegrationException(Exception):
    def __init__(self, error_code, message, data: str = None, *args: object) -> None:
        self.error_code = error_code
        self.message = message
        self.data = data
        super().__init__(*args)
