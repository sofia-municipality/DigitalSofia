from marshmallow import EXCLUDE, Schema, fields, exceptions

class DelimitedListField(fields.List):
    def _deserialize(self, value, attr, data, **kwargs):
        try:
            return value.split(',')
        except AttributeError:
            raise exceptions.ValidationError(
                f"{attr} is not a delimited list it has a non strig value"
            )

class ServicesListSchema(Schema):
    page_no = fields.Int(data_key="pageNo", required=False, allow_none=True)
    limit = fields.Int(required=False, allow_none=True)
    formio_fields = DelimitedListField(fields.Str(), required=False, allow_none=True, data_key="formioFields")
    bpm_fields = DelimitedListField(fields.Str(), required=False, allow_none=True, data_key="bpmFields")
