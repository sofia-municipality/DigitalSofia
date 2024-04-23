"""Unit test for /tenants."""
from random import randrange

from tests.utilities.base_test import get_token, post_tenant_payload


def test_create_tenant(app, client, session, jwt):
    """Assert tenants can be created."""
    tenant_key = f"test-{randrange(100)}"
    token = get_token(jwt, tenant_key=tenant_key)
    headers = {"Authorization": f"Bearer {token}", "content-type": "application/json"}
    response = client.post("/api/v1/tenants", json=post_tenant_payload(tenant_key=tenant_key), headers=headers)
    assert response.status_code == 200

    # Now get the tenant with a token from the tenant instance.
    response = client.get(f"/api/v1/tenants/{tenant_key}", headers=headers)

    assert response.status_code == 200
    assert response.json["key"] == tenant_key


def test_create_tenant_duplicate_key(app, client, session, jwt):
    """Assert tenants cannot be created."""
    token = get_token(jwt)
    headers = {"Authorization": f"Bearer {token}", "content-type": "application/json"}
    tenant_key = "duplicate"
    client.post("/api/v1/tenants", json=post_tenant_payload(tenant_key=tenant_key), headers=headers)
    # POST again to fail
    response = client.post("/api/v1/tenants", json=post_tenant_payload(tenant_key=tenant_key), headers=headers)
    assert response.status_code == 400


def test_update_tenant(app, client, session, jwt):
    """Assert tenant update."""
    tenant_key = "test-tenant"
    token = get_token(jwt, tenant_key=tenant_key)
    headers = {"Authorization": f"Bearer {token}", "content-type": "application/json"}
    response = client.post("/api/v1/tenants", json=post_tenant_payload(tenant_key=tenant_key), headers=headers)
    assert response.status_code == 200
    update_payload = {
        "details": {
            "applicationTitle": "Test Tenant",
            "customLogo": {
                "type": "url",
                "logo": "http://test.svg"
            }
        }
    }
    response = client.put(f"/api/v1/tenants/{tenant_key}", json=update_payload, headers=headers)
    assert response.status_code == 200
    assert response.json["details"]["applicationTitle"] == "Test Tenant"
