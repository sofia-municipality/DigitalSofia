"""empty message

Revision ID: 339ddeb936bc
Revises: 391c19a6d782
Create Date: 2022-06-29 23:57:14.050296

"""
from alembic import op

# revision identifiers, used by Alembic.
revision = '339ddeb936bc'
down_revision = '391c19a6d782'
branch_labels = None
depends_on = None


def upgrade():
    op.execute("ALTER TYPE roletype ADD VALUE 'RESOURCE_ID'")


def downgrade():
    pass
