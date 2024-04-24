echo 'starting application.'
echo 'starting database migration.'
export FLASK_APP=manage.py
flask db upgrade
echo 'database migration completed.'

# Without debugging
gunicorn -b :5000 'formsflow_api:create_app()' --timeout 120 --worker-class=gthread --workers=5 --threads=10

# For debugging inside the container
# python -m debugpy --listen 0.0.0.0:5678 -m gunicorn -b :5000 'formsflow_api:create_app()' --timeout 120 --worker-class=gthread --workers=5 --threads=10