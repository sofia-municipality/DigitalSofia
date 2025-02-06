import os
import json
from flask import Blueprint, current_app
from formsflow_api.models import PageBlock, DocumentStatus, Region, AddressKRA, AddressKAD
from formsflow_api.models.db import db
import csv

SeedBlueprint = Blueprint('seed', __name__)


@SeedBlueprint.cli.command('document-statuses')
def seed_document_statuses():
    filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'document-statuses.json')

    f = open(filename)
    data = json.load(f)

    for item in data:
        status = DocumentStatus.query.filter_by(title=item['title']).first()
        if status is None:
            print("Adding status {}".format(item['title']))
            status_object = DocumentStatus(
                title=item['title'],
                eurotrust_status=item['eurotrust_status'],
                formio_status=item['formio_status']
            )
            db.session.add(status_object)
        else:
            print(f"Updating page block {item['title']}")
            status.eurotrust_status = item['eurotrust_status']
            status.formio_status = item['formio_status']

    print("Commiting document statuses")
    db.session.commit()
    print("Commited")
    # Closing file
    f.close()


@SeedBlueprint.cli.command('page-blocks')
def seed_page_blocks():
    filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'page-blocks.json')

    # Opening JSON file
    f = open(filename)

    # returns JSON object as 
    # a dictionary
    data = json.load(f)

    # Iterating through the json
    # list
    for item in data:
        block = PageBlock.query.filter_by(machine_name=item['machine-name']).first()
        if block is None:
            print("Adding page block {}".format(item['machine-name']))
            block = PageBlock.create_from_dict({
                "machine_name": item['machine-name'],
                "attributes_translations": item['attributes_translations'],
                "page": item['page']
            }
            )
            db.session.add(block)
        else:
            print("Updating page block {}".format(item['machine-name']))
            block.attributes_translations = item['attributes_translations']
            block.page = item['page']

    print("Committing page items")
    db.session.commit()
    print("Committed")
    # Closing file
    f.close()


@SeedBlueprint.cli.command('regions')
def seed_regions():
    Region.delete_all()

    regions_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'Regions_SO.csv')
    regions_additional_data_filename = os.path.join(current_app.static_folder, 'data', 'seeds',
                                                    f'regions_additional_data_{current_app.config.get("REGIONS_ENV")}.csv')
    regions_codes_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'Райони-код,реф.номера.csv')

    regions = {}

    def read_csv_to_dict(filename, key_index, value_indices):
        current_app.logger.info(f"Reading {filename} {key_index} {value_indices}")
        with open(filename, 'r') as file:
            csv_reader = csv.reader(file)
            next(csv_reader, None)  # skip the headers
            result = {}
            for row in csv_reader:
                if len(row) > max(key_index, *value_indices.values()):
                    result[int(row[key_index])] = {k: row[v] for k, v in value_indices.items()}
                else:
                    current_app.logger.error(f"Row does not have enough elements: {row}")
            return result

    regions_data = read_csv_to_dict(regions_filename, 2, {"name": 0, "code": 1, "city_are_code": 2})
    additional_data = read_csv_to_dict(regions_additional_data_filename, 1,
                                       {"ais_code": 3, "eik": 4, "id": 5, "title": 6, "client_id": 7, "secret_key": 8})
    codes_data = read_csv_to_dict(regions_codes_filename, 1, {"reference_number_code": 2})

    for city_are_code, region in regions_data.items():
        if city_are_code in additional_data:
            region.update(additional_data[city_are_code])
        if city_are_code in codes_data:
            region.update(codes_data[city_are_code])
        regions[city_are_code] = region

    for region_info in regions.values():
        current_app.logger.info(region_info)
        region = Region.create_from_dict(region_info)
        if region:
            current_app.logger.info(region)
            db.session.add(region)

    print("Committing regions")
    # db.session.commit()
    print("Committed")


@SeedBlueprint.cli.command('addresses-kad')
def seed_addresses_from_kad():
    AddressKAD.delete_all()
    address_kad_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'KAD-2024-10-02.csv')

    current_app.logger.debug(f"address_kad_filename: {address_kad_filename}")

    with open(address_kad_filename, 'r') as file:
        csv_reader = csv.reader(file)
        next(csv_reader, None)  # skip the headers
        for row in csv_reader:
            address = AddressKAD.create_from_dict({
                "code_nm_grao": row[0],
                "code_pa": row[1],
                "building_number": row[2],
                "entrance": row[3],
                "region_id": int(row[4]),
                "section": int(row[5]),
                "division": row[6],
                "post_code": row[7],
                "num_permanent_address": int(row[8]),
                "num_present_address": int(row[9]),
                "date_change": row[10],
                "status": int(row[11]),
            })
            if address:
                db.session.add(address)

    print("Committing addresses from KAD")
    db.session.commit()
    print("Committed")


@SeedBlueprint.cli.command('addresses-kra')
def seed_addresses():
    AddressKRA.delete_all()
    address_kra_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'KPA-2024-10-02.csv')

    current_app.logger.debug(f"address_kra_filename: {address_kra_filename}")

    with open(address_kra_filename, 'r') as file:
        csv_reader = csv.reader(file)
        next(csv_reader, None)  # skip the headers
        for row in csv_reader:
            address = AddressKRA.create_from_dict({
                "code_nm_grao": row[0],
                "code_pa": row[1],
                "name_pa": row[2],
                "vid_pa": row[3],
                "data_change": row[4],
                "status": int(row[5]),
            })
            if address:
                db.session.add(address)

    print("Committing addresses from KRA")
    db.session.commit()
    print("Committed")
