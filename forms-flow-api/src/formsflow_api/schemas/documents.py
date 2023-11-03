from marshmallow import EXCLUDE, Schema, fields


class DocumentsListSchema(Schema):
    class Meta:
        unknown = EXCLUDE

    status = fields.Str(required=False,allow_null=True)
    # type = fields.Str(required=False,allow_null=True)
    cursor = fields.Str(required=False,allow_null=True)
    created_after = fields.Str(data_key="createdAfter", required=False, allow_none=True)
    # page_no = fields.Int(data_key="pageNo", required=False, allow_none=True)
    # size = fields.Int(data_key="size", required=False, allow_none=True)

class DocumentSignRequest(Schema):
    """This class manages document sign schema."""

    class Meta:
        """Exclude unknown fields in the deserialized output."""
        unknown = EXCLUDE

    application_id = fields.Str(data_key="applicationId", required=False, default=None)
    formio_id = fields.Str(data_key="formioId", required=True)
    content = fields.Str(required=True)
    content_type = fields.Str(data_key="contentType", required=True)
    file_name = fields.Str(data_key="fileName", required=True)

class DocumentSignCallback(Schema):
    transaction_id = fields.Str(data_key="transactionID")
    status = fields.Int()
    reject_reason = fields.Str(data_key="rejectReason", allow_none=True)
