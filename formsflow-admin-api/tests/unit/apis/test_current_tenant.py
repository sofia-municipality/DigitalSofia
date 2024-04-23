"""Unit test for /tenant."""
import datetime
from random import randrange

import jwt as pyjwt

from tests.utilities.base_test import get_token, post_tenant_payload


def test_get_current_tenant(app, client, session, jwt):
    """Assert tenants can be created and retrieved using token."""
    tenant_key = f"test-{randrange(100)}"
    token = get_token(jwt, tenant_key=tenant_key)
    headers = {"Authorization": f"Bearer {token}", "content-type": "application/json"}
    response = client.post("/api/v1/tenants", json=post_tenant_payload(tenant_key=tenant_key), headers=headers)
    assert response.status_code == 200

    # Now get the tenant with a token from the tenant instance.
    response = client.get("/api/v1/tenant", headers=headers)
    assert response.status_code == 200
    assert response.json["key"] == tenant_key

    assert response.headers["x-jwt-token"]
    decoded_token = pyjwt.decode(response.headers["x-jwt-token"], algorithms="HS256",
                                 key=app.config["FORMIO_JWT_SECRET"])
    assert decoded_token["user"]["_id"] == "formsflow-reviewer@example.com"


def test_get_expired_tenant(app, client, session, jwt, freezer):
    """Assert expired tenants are returning 403."""
    tenant_key = f"test-{randrange(100)}"
    token = get_token(jwt, tenant_key=tenant_key)
    headers = {"Authorization": f"Bearer {token}", "content-type": "application/json"}
    response = client.post("/api/v1/tenants",
                           json=post_tenant_payload(tenant_key=tenant_key, trial=True), headers=headers)
    assert response.status_code == 200

    # Move time to future date
    freezer.move_to((datetime.datetime.now() + datetime.timedelta(days=app.config.get('TRIAL_PERIOD') + 1)).date())

    # Now get the tenant with a token from the tenant instance.
    response = client.get("/api/v1/tenant", headers=headers)
    assert response.status_code == 403
