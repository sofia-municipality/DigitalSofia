from flask import current_app

from formsflow_api.models import AddressKRA, AddressKAD
from formsflow_api.schemas import AddressKRASchema, AddressKADSchema


class AddressService:

    @classmethod
    def get_addresses_from_kra(cls, query_params, **kwargs):
        page_number = query_params.get("page_no", 1)
        limit = query_params.get("limit", 10)
        sort_by = query_params.get("order_by", 'name_pa')
        sort_order = query_params.get("sort_order", 'asc')
        name_pa = query_params.get("name_pa", None)
        addresses, page_count, total = AddressKRA.get_all(
            page_number,
            limit,
            sort_by,
            sort_order,
            name_pa=name_pa
        )

        schema = AddressKRASchema()
        addresses_with_regions = AddressKAD.get_regions_for_streets(schema.dump(addresses, many=True))
        current_app.logger.info(addresses_with_regions)
        return schema.dump(addresses_with_regions, many=True), page_count, total

    @classmethod
    def get_addresses_from_kad(cls, query_params, **kwargs):
        page_number = query_params.get("page_no") or 1
        limit = query_params.get("limit") or 10
        sort_by = query_params.get("order_by") or 'name_pa'
        sort_order = query_params.get("sort_order") or 'desc'
        name_pa = query_params.get("name_pa")
        building_number = query_params.get("building_number") or None
        region_id = query_params.get("region_id")
        addresses, page_count, total = AddressKAD.get_all(
            page_number,
            limit,
            sort_by,
            sort_order,
            name_pa=name_pa,
            building_number=building_number,
            region_id=region_id
        )
        schema = AddressKADSchema()
        return schema.dump(addresses, many=True), page_count, total
