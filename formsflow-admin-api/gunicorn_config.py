"""The configuration for gunicorn, which picks up the
   runtime options from environment variables
"""

import os


workers = int(os.environ.get('GUNICORN_PROCESSES', '3'))
worker_class = os.environ.get('GUNICORN_WORKER_CLASS', 'gevent')
worker_connections = int(os.environ.get('GUNICORN_WORKER_CONNECIONS', '1000'))
threads = int(os.environ.get('GUNICORN_THREADS', '1'))
timeout = int(os.environ.get('GUNICORN_TIMEOUT', '60'))
keepalive = int(os.environ.get('GUNICORN_KEEPALIVE', '2'))


forwarded_allow_ips = '*'  # pylint: disable=invalid-name
secure_scheme_headers = {'X-Forwarded-Proto': 'https'}  # pylint: disable=invalid-name
