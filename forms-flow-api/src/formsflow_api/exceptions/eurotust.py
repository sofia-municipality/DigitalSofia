from formsflow_api_utils.exceptions import BusinessException

class EurotrustException(BusinessException):

    
    def __init__(self, error, status_code, data = None, *args, **kwargs):
        """Return a valid BusinessException."""
        super().__init__(error=error, status_code=status_code, *args, **kwargs)
        self.data = data
