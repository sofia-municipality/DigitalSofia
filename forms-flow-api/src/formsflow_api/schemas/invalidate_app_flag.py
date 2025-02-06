from marshmallow import EXCLUDE, Schema, fields

class InvalidateAppFlagSchema(Schema):

    person_identifier = fields.Str(data_key="personIdentifier")
    tenant = fields.Str(data_key="tenant")
    process_definition_id = fields.Str(data_key="processDefinitionId")
    address_change_type = fields.Str(data_key= "addressChangeType")