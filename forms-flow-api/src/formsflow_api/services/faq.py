from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api_utils.exceptions import BusinessException
from flask import current_app
from http import HTTPStatus
from formsflow_api.models import FAQ, db
from formsflow_api.schemas import FAQSchema

class FAQService:

    @classmethod
    def get_faqs(cls, query_params, **kwargs):
        page_number = query_params.get("page_no")
        limit = query_params.get("limit")
        sort_by = query_params.get("order_by", "id")
        sort_order = query_params.get("sort_order", "desc")
        is_favoured=query_params.get("is_favoured")

        faqs, page_count, total = FAQ.get_all(
            page_number,
            limit,
            sort_by,
            sort_order,
            is_favoured=is_favoured
        )

        schema = FAQSchema()
        return schema.dump(faqs, many=True), page_count, total

    @classmethod
    def create_faq(cls, payload, **kwargs):
        """Creates a new faq entry."""
        # TODO: Additional check for user context
        # user: UserContext = kwargs["user"]
        # user_id: str = user.user_name or ANONYMOUS_USER
        return FAQ.create_from_dict(payload)


    @classmethod
    def get(cls, faq_id:int) -> FAQ:
        faq = FAQ.query.filter_by(id=faq_id).first()
        if faq:
            return faq
        
        response, status = {
            "type": "Bad request error",
            "message": f"Invalid request data - page_block id {faq_id} does not exist",
        }, HTTPStatus.NOT_FOUND
        raise BusinessException(response, status)
    
    @classmethod
    def update(cls, faq_id:int, data: dict):
        faq = FAQ.query.filter_by(id=faq_id).first()
        if faq:
            
            faq.update(data)
            # db.session.commit(faq)
            # current_app.logger.warning(faq.title)
            # current_app.logger.warning(faq.content)
            # faq.title+ = data["title"]
            # faq.content = data["content"]
            # faq.is_favoured = data["is_favoured"]
            # faq.commit()



        else:
            response, status = {
                "type": "Bad request error",
                "message": f"Invalid request data - draft id {faq_id} does not exist",
            }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)

    @classmethod
    def delete(cls, faq_id):
        faq = FAQ.query.filter_by(id=faq_id)
        if faq:
            faq.delete()
            db.session.commit()
        else:
            response, status = {
                "type": "Bad request error",
                "message": f"Invalid request data - draft id {faq_id} does not exist",
            }, HTTPStatus.BAD_REQUEST
            raise BusinessException(response, status)