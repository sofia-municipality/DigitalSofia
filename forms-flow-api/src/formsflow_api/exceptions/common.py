from http import HTTPStatus


class CommonException(Exception):
    def __init__(self, message: str = "Something went wrong", data: object = None, key: str = None, code: HTTPStatus = HTTPStatus.BAD_REQUEST, *args: object) -> None:
        self.code = code
        self.message = message
        self.data = data
        self.key = key
        super().__init__(*args)

    def to_dict(self):
        return {
            "code": self.code,
            "message": self.message,
            "data": self.data,
            "key": self.key
        }
