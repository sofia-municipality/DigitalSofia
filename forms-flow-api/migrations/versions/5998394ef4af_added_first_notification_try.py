"""Added first_notification_try

Revision ID: 5998394ef4af
Revises: 5200f53a85dc
Create Date: 2024-06-19 14:39:08.075664

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '5998394ef4af'
down_revision = '398ae7cc37ed'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.add_column('mateus_payment_group', sa.Column('first_notification_try', sa.DateTime(), nullable=True))
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_column('mateus_payment_group', 'first_notification_try')
    # ### end Alembic commands ###
