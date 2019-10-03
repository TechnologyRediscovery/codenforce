#!/usr/bin/env python3

# encoding: utf-8

import csv
import random
import re
import time
import psycopg2
import itertools
import csv_utils
import requests
import bs4

CSV_FILE_ENCODING = 'utf-8'
global MUNI_MAP
global CURRENT_MUNI_NAME

def main():
    INPUT_FILE = 'workspace/pitcairnparids.csv'
    # Read CSV file with property data
    properties = read_property_file(INPUT_FILE)
    munimap = {'chalfant':814,'churchhill':816, 'pitcairn':847}
    CURRENT_MUNI_NAME = 'pitcairn'
    # Add owner data to properties
    properties_with_owner_info = add_owner_info(properties)
    # Save new CSV file with property plus owner data
    OUTPUT_FILE = 'output/testPropOut.txt'
    save_properties_as_csv(properties_with_owner_info, OUTPUT_FILE)


def read_property_file(input_file):
    # Read the CSV file and iterate through every property
    with open(input_file, 'r', encoding=CSV_FILE_ENCODING) as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            yield row


def add_owner_info(properties):
    PARCEL_ID_FIELD = 'parcelid'
    OWNER_NAME_FIELD = 'OwnerName'
    OWNER_ADDRESS_FIELD = 'OwnerAddress'
    for prop in properties:
        assert prop[PARCEL_ID_FIELD]  # Every property must have a parcel id
        parcel_id = prop[PARCEL_ID_FIELD]
        print('Processing parcel', parcel_id)

        # Get the html from the county's website
        property_html = get_county_page_for(parcel_id)

        # Wait between request, just to be nice with the county's site
        sleep_time = random.random() * 3  # Sleep at most 3 seconds
        time.sleep(sleep_time)
        print('sleeping for {:.2f} seconds'.format(sleep_time))

        # Add owner name and address to the property
        prop[OWNER_NAME_FIELD] = extract_owner_name(parcel_id, property_html)
        prop[OWNER_ADDRESS_FIELD] = extract_owner_address(
                parcel_id, property_html)
        print(prop[OWNER_NAME_FIELD], prop[OWNER_ADDRESS_FIELD])

        yield prop


county_info_cache = {}
def get_county_page_for(parcel_id):
    if parcel_id in county_info_cache:
        return county_info_cache[parcel_id]
    COUNTY_REAL_ESTATE_URL = ('http://www2.county.allegheny.pa.us/'
                              'RealEstate/GeneralInfo.aspx?')
    search_parameters = {
        'ParcelID': parcel_id,
        'SearchType': 3,
        'SearchParcel': parcel_id}
    try:
        response = requests.get(
                COUNTY_REAL_ESTATE_URL,
                params=search_parameters,
                timeout=5)
    except requests.exceptions.Timeout:
        # Wait 10 secs and try one more time
        time.sleep(10)
        response = requests.get(
                COUNTY_REAL_ESTATE_URL,
                params=search_parameters,
                timeout=5)
    county_info_cache[parcel_id] = response.text
    return response.text


def extract_owner_name(parcel_id, property_html):
    OWNER_NAME_SPAN_ID = 'BasicInfo1_lblOwner'
    soup = bs4.BeautifulSoup(property_html)
    owner_name = soup.find('span', id=OWNER_NAME_SPAN_ID).text
    # Remove extra spaces from owner's name
    owner_name = re.sub('\s+', ' ', owner_name.strip())
    return owner_name

def extact_owner_name_and_mailing(parcel_id, property_html):

    OWNER_ADDRESS_SPAN_ID = 'lblChangeMail'


    soup = bs4.BeautifulSoup(property_html)
    owner_address = soup.find('span', id=OWNER_ADDRESS_SPAN_ID).text
    # Remove extra spaces
    owner_address = re.sub('\s+', ' ', owner_address.strip())
    # Remove leading spaces before commas
    owner_address = re.sub('\s+,', ',', owner_address)
    return owner_address



def extract_owner_address(parcel_id, property_html):
    OWNER_ADDRESS_SPAN_ID = 'lblChangeMail'
    soup = bs4.BeautifulSoup(property_html)
    owner_address = soup.find('span', id=OWNER_ADDRESS_SPAN_ID).text
    # Remove extra spaces
    owner_address = re.sub('\s+', ' ', owner_address.strip())
    # Remove leading spaces before commas
    owner_address = re.sub('\s+,', ',', owner_address)
    return owner_address


def save_properties_as_csv(properties_with_owner_info, output_file):
    fieldnames = None
    with open(output_file, 'w', encoding=CSV_FILE_ENCODING) as outfile:
        writer = csv.writer(outfile)
        for prop in properties_with_owner_info:
            if fieldnames is None:
                fieldnames = sorted(prop.keys())
                writer.writerow(fieldnames)
            writer.writerow([prop[k] for k in fieldnames])

def store_owner_as_property():
    db_conn = get_db_conn()
    cursor = db_ conn.cursor()
    # Using hard-coded event type ID: 18, for code enforcement letter type
    sql_command = """

        INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18, "humanVerifiedby")
        VALUES (DEFAULT, CAST( 'ownercntylookup' AS persontype), %s, ?, ?, ?, 
                ?, ?, ?, ?, ?, ?, 
                ?, ?, ?, ?, ?, ?, 
                ?, ?);

        INSERT INTO codeenfevent
            (eventID, eventDate, eventDescription, letterText,
            codeOfficer_officerID, codeEnfCase_caseID, EventTyp_codeEnfEventTypeID)
        VALUES (%(eventID)s, %(eventDate)s, %(eventDescription)s,
            %(letterText)s, %(codeOfficer_officerID)s, %(codeEnfCase_caseID)s, 18)
    """
    # Read CSV file with original Access Data
    with open(csv_file, 'r') as infile:
        reader = csv_utils.UnicodeReader(infile, delimiter=CSV_DELIMITER)
        # Get header
        header = reader.next()
        # Sequence of eventid
        get_event_id = itertools.count(start=1)
        for row in reader:
            # Build a record dict from row and header
            record = dict(zip(header, row))
            # Assign eventid
            record['eventID'] = get_event_id.next()
            # Insert the data to the Postgres table
            cursor.execute(sql_command, record)


db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        database="cogdb",
        user="sylvia",
        password="c0d3",
        host="localhost"
    )
    return db_conn


if __name__ == '__main__':
    main()
