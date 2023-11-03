"""Updating enum types on authorizations

Revision ID: 8d3c275fc1df
Revises: cd34aee6015a
Create Date: 2023-09-12 09:45:51.404106

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '8d3c275fc1df'
down_revision = 'cd34aee6015a'
branch_labels = None
depends_on = None


def upgrade():
    with op.get_context().autocommit_block():
        op.execute("ALTER TYPE authtype ADD VALUE 'DESIGNER'")
        op.execute("ALTER TYPE authtype ADD VALUE 'APPLICATION'")


def downgrade():
    op.execute("ALTER TYPE authtype RENAME TO authtype_old")
    op.execute("CREATE TYPE authtype AS ENUM('DASHBOARD','FORM','FILTER')")
    op.execute((
        "ALTER TABLE authorization ALTER COLUMN authtype TYPE authtype USING "
        "authtype::text::authtype"
    ))
    op.execute("DROP TYPE authtype_old")
    # ### end Alembic commands ###