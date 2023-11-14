"""Helper Util."""

from admin_api.models import db


def find_model_from_table_name(table_name: str):
    """Util to find model class from table name."""
    for model_class in db.Model._decl_class_registry.values():  # pylint:disable=protected-access
        if getattr(model_class, '__tablename__', None) == table_name:
            return model_class
    return None
