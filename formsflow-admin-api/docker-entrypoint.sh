#!/bin/sh

echo 'starting application'
export FLASK_APP=manage.py
flask db upgrade
gunicorn --bind 0.0.0.0:5000 --timeout 300 --workers 3  wsgi:application
