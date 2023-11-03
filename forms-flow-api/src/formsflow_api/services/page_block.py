from formsflow_api_utils.utils.user_context import UserContext, user_context
from flask import current_app
from formsflow_api_utils.exceptions import BusinessException
from http import HTTPStatus
from formsflow_api.models import PageBlock
from formsflow_api.schemas import PageBlockSchema
from formsflow_api.utils import get_locale

class PageBlockService:
    @staticmethod
    @user_context
    def get_all_page_blocks(query_params, **kwargs):
        page = query_params.get('page')
        current_app.logger.info(page)
        query = PageBlock.query
        query = query.filter(PageBlock.attributes_translations[get_locale()] != None)

        if page:
            query = query.filter_by(page=query_params.get('page'))

        page_blocks = query.all()
        count = query.count()
        schema = PageBlockSchema()
        return schema.dump(page_blocks, many=True), count
    
    @staticmethod
    @user_context
    def get_page_block(page_block_id: int, **kwargs):
        
        block = PageBlock.query.filter_by(id=page_block_id).first()
        if block:
            schema = PageBlockSchema()
            return schema.dump(block)
    
        response, status = {
            "type": "Bad request error",
            "message": f"Invalid request data - page_block id {page_block_id} does not exist",
        }, HTTPStatus.BAD_REQUEST
        raise BusinessException(response, status)
        
    @staticmethod
    @user_context
    def update_page_block(page_block_id: int, data, **kwargs):
        """Update draft."""
        # user: UserContext = kwargs["user"]
        # user_id: str = user.user_name or ANONYMOUS_USER
        block = PageBlock.query.filter_by(id=page_block_id).first()

        if block:
            block.update_from_dict(data.keys(), data)
            block.commit()
            return

        response, status = {
                "type": "Bad request error",
                "message": f"Invalid request data - block id {page_block_id} does not exist",
            }, HTTPStatus.BAD_REQUEST
        raise BusinessException(response, status)