"""Super class to handle all operations related to base model."""

from .db import db


class BaseModel(db.Model):
    """This class manages all of the base model functions."""

    __abstract__ = True

    @staticmethod
    def commit():
        """Commit the session."""
        db.session.commit()

    def flush(self):
        """Save and flush."""
        db.session.add(self)
        db.session.flush()
        return self

    def save(self):
        """Save and commit."""
        db.session.add(self)
        db.session.commit()
        return self

    @staticmethod
    def rollback():
        """RollBack."""
        db.session.rollback()

    @classmethod
    def find_by_id(cls, identifier: int):
        """Return model by id."""
        return cls.query.get(identifier)

    @classmethod
    def find_all(cls):
        """Return all entries."""
        rows = cls.query.all()  # pylint: disable=no-member
        return rows

    def update(self, payload: dict):
        """Update and commit."""
        for key, value in payload.items():
            setattr(self, key, value)
        self.commit()
        return self

    def delete(self):
        """Delete and commit."""
        db.session.delete(self)
        db.session.commit()

    def as_dict(self, recursive=True):
        """Return JSON Representation."""
        mapper = self.__mapper__
        columns = mapper.columns
        result = {c: dict(getattr(self, c)) if isinstance(getattr(self, c), dict) else str(getattr(self, c)) for c in
                  dict(columns).keys()}
        if recursive:
            for rel in mapper.relationships:
                relationship = rel.key
                result[relationship] = getattr(self, relationship).as_dict()
        return result
