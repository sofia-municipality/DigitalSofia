
"""CORS pre-flight decorator.

A simple decorator to add the options method to a Request Class.
"""


def cors_preflight(methods: str = 'GET'):
    """Render an option method on the class."""

    def wrapper(f):
        def options(self, *args, **kwargs):  # pylint: disable=unused-argument
            return {'Allow': methods}, 200, \
                   {'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': methods,
                    'Access-Control-Allow-Headers': 'Authorization, Content-Type, registries-trace-id, Account-Id'}

        setattr(f, 'options', options)
        return f

    return wrapper
