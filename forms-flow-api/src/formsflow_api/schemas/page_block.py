"""This manages draft Response Schema."""
from marshmallow import EXCLUDE, Schema, fields
from pprint import pprint
import json

class JSON(fields.Field):
    def _deserialize(self, value, attr, data, **kwargs):
        if value:
            if type(value) is dict:
                return value
            
            try:
                return json.loads(value)
            except ValueError:
                return None
            
        return None


class PageBlockSchema(Schema):
    """This class manages submission request and response schema."""
    
    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        unknown = EXCLUDE

    id = fields.Int(data_key="id")
    machine_name = fields.Str(data_key="machine_name", dump_only=True)
    attributes = JSON()
    created = fields.Str()
    modified = fields.Str()


class PageBlockListSchema(Schema):
    """This class manages submission request and response schema."""
    
    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        unknown = EXCLUDE

    page = fields.Str()