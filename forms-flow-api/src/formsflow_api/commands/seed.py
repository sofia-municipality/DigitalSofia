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
    regions_codes_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'Райони-код,реф.номера.csv')
    regions = []

    with open(regions_filename, 'r') as file:
        csv_reader = csv.reader(file)
        next(csv_reader, None)  # skip the headers
        for row in csv_reader:
            regions.append({
                "name": row[0].rstrip(),
                "code": row[1],
                "city_are_code": int(row[2]),
            })

    with open(regions_codes_filename, 'r') as file:
        csv_reader = csv.reader(file)
        next(csv_reader, None)  # skip the headers
        for row in csv_reader:
            index = [elem["city_are_code"] for elem in regions].index(int(row[1]))
            if regions[index] is not None:
                regions[index]["reference_number_code"] = row[2]
                region = Region.create_from_dict(regions[index])
                if region:
                    current_app.logger.info(region)
                    db.session.add(region)
        print("Committing regions")
        db.session.commit()
        print("Committed")


@SeedBlueprint.cli.command('addresses-kad')
def seed_addresses_from_kad():
    AddressKAD.delete_all()
    address_kad_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'KAD-2023-01-10.csv')

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
    address_kra_filename = os.path.join(current_app.static_folder, 'data', 'seeds', 'KPA-2023-01-10.csv')

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
