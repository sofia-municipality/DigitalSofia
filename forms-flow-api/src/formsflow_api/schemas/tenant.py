from marshmallow import EXCLUDE, Schema, fields, validate

class TenantSchema(Schema):

    class Meta:
        unknown = EXCLUDE

    tenant_key = fields.Str(data_key="X-Tenant-Key", required=True, allow_none=False)
