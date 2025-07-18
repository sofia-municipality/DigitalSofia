"""This manages Submission Database Models."""


from __future__ import annotations

import uuid
from datetime import datetime
import re
from flask import current_app

from formsflow_api_utils.utils import (
    DRAFT_APPLICATION_STATUS,
    FILTER_MAPS,
    validate_sort_order_and_order_by,
)
from formsflow_api.exceptions import CommonException
from formsflow_api_utils.utils.enums import DraftStatus
from formsflow_api_utils.utils.user_context import UserContext, user_context
from formsflow_api.schemas.region import RegionSchema
from sqlalchemy import and_, update
from sqlalchemy.dialects.postgresql import JSON, UUID
from sqlalchemy.sql.expression import text

from .application import Application
from .audit_mixin import AuditDateTimeMixin
from .base_model import BaseModel
from .db import db
from .form_process_mapper import FormProcessMapper
from .region import Region



class Draft(AuditDateTimeMixin, BaseModel, db.Model):
    """This class manages submission information."""

    __tablename__ = "draft"
    id = db.Column(db.Integer, primary_key=True)
    _id = db.Column(UUID(as_uuid=True), unique=True, default=uuid.uuid4, nullable=False)
    data = db.Column(JSON, nullable=False)
    status = db.Column(db.String(10), nullable=True)
    application_id = db.Column(
        db.Integer, db.ForeignKey("application.id"), nullable=False
    )

    @classmethod
    def create_draft_from_dict(cls, draft_info: dict) -> Draft:
        """Create new application."""
        if draft_info:
            draft = Draft()
            draft.status = DraftStatus.ACTIVE.value
            draft.application_id = draft_info["application_id"]
            draft.data = draft_info["data"]

            # ['caseDataSource']['data']['serviceId']
            # system_dict["serviceSupplierId"]
            # system_dict["serviceSupplierName"]

            draft.save()
            return draft
        return None

    def update(self, draft_info: dict):
        """Update draft."""
        self.update_from_dict(
            ["data", "status"],
            draft_info,
        )
        self.commit()

    @classmethod
    def get_by_id(cls, draft_id: str, user_id: str) -> Draft:
        """Retrieves the draft entry by id."""
        result = (
            cls.query.join(Application, Application.id == cls.application_id)
            .join(
                FormProcessMapper,
                FormProcessMapper.id == Application.form_process_mapper_id,
            )
            .filter(
                and_(
                    cls.status == str(DraftStatus.ACTIVE.value),
                    Application.created_by == user_id,
                    cls.id == draft_id,
                )
            )
        )
        return FormProcessMapper.tenant_authorization(result).first()

    @classmethod
    def find_by_id(cls, draft_id: int, user_id: str) -> Draft:
        """Find draft that matches the provided id."""
        result = (
            cls.query.with_entities(
                FormProcessMapper.form_name,
                FormProcessMapper.process_name,
                Application.created_by,
                FormProcessMapper.form_id,
                FormProcessMapper.process_key,
                FormProcessMapper.process_name,
                cls.id,
                cls.application_id,
                cls.created,
                cls.modified,
                cls.data,
            )
            .join(Application, Application.id == cls.application_id)
            .join(
                FormProcessMapper,
                FormProcessMapper.id == Application.form_process_mapper_id,
            )
            .filter(
                and_(
                    cls.status == str(DraftStatus.ACTIVE.value),
                    Application.created_by == user_id,
                    cls.id == draft_id,
                )
            )
        )
        return FormProcessMapper.tenant_authorization(result).first()

    @classmethod
    def find_all_active(  # pylint: disable=too-many-arguments
        cls,
        user_name: str,
        page_number=None,
        limit=None,
        sort_by="id",
        sort_order="desc",
        **filters,
    ):
        """Fetch all active drafts."""
        result = (
            cls.filter_conditions(**filters)
            .with_entities(
                FormProcessMapper.form_name,
                FormProcessMapper.process_name,
                Application.created_by,
                FormProcessMapper.form_id,
                cls.id,
                cls.application_id,
                cls.created,
                cls.modified,
                cls.data,
            )
            .join(Application, Application.id == cls.application_id)
            .join(
                FormProcessMapper,
                Application.form_process_mapper_id == FormProcessMapper.id,
            )
            .filter(
                and_(
                    cls.status == str(DraftStatus.ACTIVE.value),
                    Application.created_by == user_name,
                )
            )
        )
        sort_by, sort_order = validate_sort_order_and_order_by(sort_by, sort_order)
        model_name = "form_process_mapper" if sort_by == "form_name" else "draft"
        if sort_by and sort_order:
            result = result.order_by(text(f"{model_name}.{sort_by} {sort_order}"))
        result = FormProcessMapper.tenant_authorization(result)
        total_count = result.count()
        limit = total_count if limit is None else limit
        result = result.paginate(page=page_number, per_page=limit, error_out=False)
        return result.items, total_count

    @classmethod
    def make_submission(cls, draft_id, data, user_id):
        """Activates the application from the draft entry."""
        # draft = cls.query.get(draft_id)
        draft = cls.get_by_id(draft_id, user_id)
        if not draft:
            return None
        stmt = (
            update(Application)
            .where(Application.id == draft.application_id)
            .values(
                application_status=data["application_status"],
                submission_id=data["submission_id"],
            )
        )
        cls.execute(stmt)
        # The update statement will be commited by the following update
        draft.update({"status": DraftStatus.INACTIVE.value, "data": {}})
        return draft

    @classmethod
    def filter_conditions(cls, **filters):
        """This method creates dynamic filter conditions based on the input param."""
        filter_conditions = []
        for key, value in filters.items():
            if value:
                filter_map = FILTER_MAPS[key]
                model_name = (
                    Draft
                    if not filter_map["field"] == "form_name"
                    else FormProcessMapper
                )
                condition = cls.create_filter_condition(
                    model=model_name,
                    column_name=filter_map["field"],
                    operator=filter_map["operator"],
                    value=value,
                )
                filter_conditions.append(condition)
        query = cls.query.filter(*filter_conditions) if filter_conditions else cls.query
        return query

    @classmethod
    @user_context
    def get_draft_count(cls, **kwargs):
        """Get active draft count."""
        user: UserContext = kwargs["user"]
        user_id: str = user.user_name
        query = cls.query.join(Application, cls.application_id == Application.id)
        query = query.filter(Application.created_by == user_id)
        query = query.filter(
            and_(
                Application.application_status == DRAFT_APPLICATION_STATUS,
                Draft.status == str(DraftStatus.ACTIVE.value),
            )
        )
        query = FormProcessMapper.tenant_authorization(
            query=query.join(
                FormProcessMapper,
                Application.form_process_mapper_id == FormProcessMapper.id,
            )
        )
        draft_count = query.count()
        return draft_count

    @classmethod
    def get_draft_by_parent_form_id(cls, parent_form_id: str) -> Draft:
        """Get all draft against one form id."""
        get_all_mapper_id = (
            db.session.query(FormProcessMapper.id)
            .filter(FormProcessMapper.parent_form_id == parent_form_id)
            .all()
        )
        result = cls.query.join(
            Application, Application.id == cls.application_id
        ).filter(
            and_(
                Application.form_process_mapper_id.in_(
                    [id for id, in get_all_mapper_id]
                ),
                Application.application_status == DRAFT_APPLICATION_STATUS,
            )
        )
        return FormProcessMapper.tenant_authorization(result).all()

    def get_application(self) -> Application:
        return Application.query.get(self.application_id)
    
    def generate_application_json_submission_dict(self, requestorName: str, service: dict) -> dict:
        current_app.logger.info("Draft@generate_application_json_submission_dict")
        to_return = self.data

        current_app.logger.debug("Do we have an already supplied caseDataSource")
        if to_return.get("caseDataSource"):
            return to_return

        data = {}

        region = self.data.get("region")
        region_schema = RegionSchema()
        region_entity = Region.get_by_city_area_code(region.get("city_are_code"))
        additional_region_data = region_schema.dump(region_entity, many=False)
        
        current_app.logger.debug(f"Setup region - {additional_region_data}")
        if additional_region_data:
            data["serviceSupplierId"] = additional_region_data.get("id")
            data["serviceSupplierName"] = additional_region_data.get("title")

        
        application = self.get_application()
        formio_path_name = application.form_process_mapper.form_path
        matches = re.match(r"(\w+)-(.+)",formio_path_name)
        relevant_path_name = matches[2]
        current_app.logger.debug(f"Set serviceId and serviceName - {relevant_path_name}")
        data["serviceId"] = service["serviceId"]
        data["serviceName"] = service["serviceName"]
        data["requestorName"] = requestorName

        ### Set process Instance ID
        current_app.logger.debug(f"Generation Source - digitallSofia")
        data["generationSource"] = "digitalSofia"
        current_app.logger.debug(f"Process Key - {application.form_process_mapper.process_key}")
        data["processKey"] = application.form_process_mapper.process_key

        current_app.logger.debug(f"Application ID - {application.id}")

        # Check is trusteeSignForm
        if application.form_process_mapper.form_name == "trusteeSignForm":
            current_app.logger.debug("Using the application id of the iniator!")
            initiator_application_id = self.data.get("applicationId")
            current_app.logger.debug(initiator_application_id)

            # Add guard if for some reason initiator application id is missing
            data["applicationId"] = initiator_application_id if initiator_application_id else application.id
        else:
            data["applicationId"] = application.id

        current_app.logger.debug(f"Process Instance ID - {application.process_instance_id}")
        data["processInstanceId"] = application.process_instance_id

        to_return["caseDataSource"] = {
            "data": data
        }

        return to_return

    def generate_metadata_dict(self, user: UserContext = None) -> dict:
        current_app.logger.info("Draft@generate_metadata_dict")
        ### Generated values
        current_app.logger.debug(f"issueDateCaseIdentifier - {datetime.now()}")
        system_dict = {
            "issueDateCaseIdentifier": datetime.now()
        }

        ### Values based on region
        region = self.data.get("region")
        current_app.logger.debug(f"Region - {region}")
        if region and region.get("city_are_code"):
            region_schema = RegionSchema()
            region_data = Region.get_by_city_area_code(region.get("city_are_code"))
            additional_region_data = region_schema.dump(region_data, many=False)
            current_app.logger.debug(additional_region_data)
            if additional_region_data:
                system_dict["serviceSupplierId"] = additional_region_data.get("id")
                system_dict["serviceSupplierName"] = additional_region_data.get("title")
                current_app.logger.debug(f'serviceSupplierId - {additional_region_data.get("id")}')
                current_app.logger.debug(f'serviceSupplierName - {additional_region_data.get("title")}')


        ### Values based on form_path
        application = self.get_application()
        formio_path_name = application.form_process_mapper.form_path
        matches = re.match(r"(\w+)-(.+)",formio_path_name)
        relevant_path_name = matches[2]
        current_app.logger.debug(f"Matches - {matches}")
        current_app.logger.debug(f"relevant_path_name - {relevant_path_name}")
        if relevant_path_name.startswith("changeofpernamentaddress"):
            system_dict["serviceId"] = 2079
            system_dict["serviceName"] = "Издаване на удостоверение за постоянен адрес след подаване на заявление за промяна на постоянен адрес"
        elif relevant_path_name.startswith("changeofcurrentaddress"):
            system_dict["serviceId"] = 2107
            system_dict["serviceName"] = "Издаване на удостоверение за настоящ адрес след подаване на адресна карта за заявяване или за промяна на настоящ адрес"


        ### User values
        user_dict = {}
        if user:
            user_dict["email"] = user.user_name
            user_dict["fullNameLatin"] = user.token_info.get("name", None)
            current_app.logger.debug(f"email - {user.user_name}")
            current_app.logger.debug(f'fullNameLatin - {user.token_info.get("name", None)}')
            if user_dict["fullNameLatin"]:
                split_name_array = user_dict["fullNameLatin"].split(" ")
                if len(split_name_array) == 1:
                    user_dict["applicantPhysicalFirstNameLatin"] = split_name_array[0]
                elif len(split_name_array) == 2:
                    user_dict["applicantPhysicalFirstNameLatin"] = split_name_array[0]
                    user_dict["applicantPhysicalFamilyNameLatin"] = split_name_array[1]
                elif len(split_name_array) == 3:
                    user_dict["applicantPhysicalFirstNameLatin"] = split_name_array[0]
                    user_dict["applicantPhysicalMiddleNameLatin"] = split_name_array[1]
                    user_dict["applicantPhysicalFamilyNameLatin"] = split_name_array[2]


            user_dict["fullName"] = f"{self.data.get('firstName')} {self.data.get('middleName')} {self.data.get('lastName')}"
            current_app.logger.debug(f"fullName - {self.data.get('firstName')} {self.data.get('middleName')} {self.data.get('lastName')}")

        return {
            "data": self.data,
            "system": system_dict,
            "user": user_dict
        }

    @classmethod
    def find_app_for_child(self, form_path: str, created_by: str, tenant: str, process: str):
        query = text("""SELECT f.form_path, a.id, a.latest_form_id, d.data FROM public.application a
                        join public.draft d on a.id = d.application_id
                        join public.form_process_mapper f on f.form_id = a.latest_form_id
                        where (a.application_status = 'Draft' or a.application_status = 'waitingForThirdPartySigniture')
                        and f.form_path = :form_path
                        and a.created_by = :created_by
                        and f.tenant = :tenant
                        and f.status = 'active'
                        and f.process_key = :process
                        ORDER BY a.id DESC  """)

        cursor = db.session.execute(query, {"form_path": form_path,
                                            "created_by": created_by,
                                            "tenant": tenant,
                                            "process": process
                                            }).fetchall()

        return cursor
    
    @classmethod
    def get_by_application_id(cls, application_id: str) -> Draft:
        query = cls.query.filter_by(application_id=application_id)
        draft = query.one_or_none()
        return draft
