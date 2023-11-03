
"""Centralized setup of logging for the service."""
import logging.config
import sys
from os import path


def setup_logging(conf):
    """Create the services logger.

    TODO should be reworked to load in the proper loggers and remove others
    """
    # log_file_path = path.join(path.abspath(path.dirname(__file__)), conf)

    if conf and path.isfile(conf):
        logging.config.fileConfig(conf)
        print(f'Configure logging, from conf:{conf}', file=sys.stdout)
    else:
        print(f'Unable to configure logging, attempted conf:{conf}', file=sys.stderr)
