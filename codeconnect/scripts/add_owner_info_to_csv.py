#!/usr/bin/env python3

# encoding: utf-8

import csv
import random
import re
import time
 
import requests
import bs4

CSV_FILE_ENCODING = 'utf-8'


def main():
    INPUT_FILE = 'workspace/pitcairnparids.csv'
    # Read CSV file with property data
    properties = read_property_file(INPUT_FILE)
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


if __name__ == '__main__':
    main()
