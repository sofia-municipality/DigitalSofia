"""This manages user request Schema."""

from marshmallow import EXCLUDE, Schema, fields


class UserlocaleReqSchema(Schema):
    """This is a general class for user locale request schema."""

    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        unknown = EXCLUDE

    locale = fields.Str(data_key="locale", required=True)


class UserPermissionUpdateSchema(Schema):
    """Schema for user role / group permissions."""

    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        fields = ("realm", "userId", "groupId", "name")
        unknown = EXCLUDE

    userId = fields.Str(data_key="userId", required=True)
    groupId = fields.Str(data_key="groupId", required=True)
    name = fields.Str(data_key="name", required=True)


class UsersListSchema(Schema):
    """Schema for user list."""

    class Meta:  # pylint: disable=too-few-public-methods
        """Exclude unknown fields in the deserialized output."""

        fields = ("firstName", "lastName", "email", "id", "username", "role")
        unknown = EXCLUDE


class UserStatusSchema(Schema):
    identificationNumber = fields.Str()
    country = fields.Str()
    firstName = fields.Str()
    middleName = fields.Str()
    lastName = fields.Str()
    firstNameLatin = fields.Str()
    middleNameLatin = fields.Str()
    lastNameLatin = fields.Str()
    phone = fields.Str()
    isIdentified = fields.Bool()
    isRejected = fields.Bool()
    isSupervised = fields.Bool()
    isReadyToSign = fields.Bool()
    rejectReason = fields.Int()


class UserStatusChangeTransactionSchema(Schema):
    identification_number = fields.Str()
