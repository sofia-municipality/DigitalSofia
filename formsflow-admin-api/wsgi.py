"""Provides the WSGI entry point for running the application
"""
from admin_api import create_app


# Openshift s2i expects a lower case name of application
application = create_app()  # pylint: disable=invalid-name

if __name__ == "__main__":
    application.run()
