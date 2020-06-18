"""
Records an inquiry in external_data
If the data is different, creates writes an event to the event table
"""
# TODO: ADD OWNER MAILING TO PROPERTY EXTERNAL DATA

from collections import namedtuple

import requests

from Util.db_conn import get_db_and_cursor
from Util.parsing import (
    create_general_insertmap,
    create_building_insertmap,
    create_tax_insertmap
)


def main():
    with get_db_and_cursor() as db_cursor:
        rows = ask_database_for_parcelids(municode=828, db_cursor=db_cursor)   # Todo: Better naming? It's property and parcel id.
        for row in rows:

            html_dict = get_county_property_assessment(row.parcel_id)
            insert_map = create_insertmap_from_html(html_dict, row.property_id)
            written_data = write_data_to_propertyexternaldata(insert_map, db_cursor)
            did_the_data_change = check_if_data_is_different_than_previous(written_data)
            if did_the_data_change:
                create_event()

# MAGIC STRINGS
GENERALINFO = "GeneralInfo"
BUILDING = "Building"
TAX = "Tax"

def get_county_property_assessment(parcel_id):
    """
    Grabs the raw HTML of a property from the county's website

    Arguments:
        parcel_id: str

    Returns:
        dict[str]
            A dictionary where the key is the scraped page's name and the value is the raw html
    """
    COUNTY_REAL_ESTATE_URL = (
        "http://www2.county.allegheny.pa.us/RealEstate/"
    )
    pages = {
        GENERALINFO: None,
        BUILDING: None,
        TAX: None
    }
    URL_ENDING = ".aspx?"

    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }

    print("Scraping data from county: " + parcel_id)
    for page in pages:
        try:
            response = requests.get(
                (COUNTY_REAL_ESTATE_URL + page + URL_ENDING), params=search_parameters, timeout=5
            )
            pages[page] = response.text
        except requests.exceptions.Timeout:
            # TODO: log_error(). Perhaps logger should reside in its own auxiliary module?
            raise requests.exceptions.Timeout
    return pages


def ask_database_for_parcelids(municode=None, muniname=None, db_cursor=None):
    """
    Generator that yields parcel ids from the database

    Args:
        municode: int
        muniname: str
            Not implemented

    Yields:
        Row(int, str):
            A namedtuple containing a property id and the corresponding parcel id
    """
    Row = namedtuple('Row', ['property_id', 'parcel_id'])
    if municode:
        select_sql = "SELECT propertyid, parid FROM public.property WHERE municipality_municode = %s"
    elif muniname:
        # mild Todo: allow different user inputs so the script can be run easily by code enforcement officers
        raise NotImplementedError
    db_cursor.execute(select_sql, [municode])
    parcel_ids = db_cursor.fetchall()

    for row in parcel_ids:
        yield Row(*row)  # Casts each tuple returned by the cursor to the namedtuple 'Row'


def create_insertmap_from_html(html_dict, property_id):
    """
    Arguments:
        html_dict: str
            The raw HTML scraped from the county website
        property_id: int
            CodeConnect's primary key for property

    Returns:
        Dict
            A dictionary of information extracted from the HTML. Mostly corresponds to the table propertyexternaldata
    """
    imap = {}
    # If we were scraping more, I would iterate over these in a loop, and have the function
    general_map = create_general_insertmap(html_dict[GENERALINFO])
    building_map = create_building_insertmap(html_dict[BUILDING])
    tax_map = create_tax_insertmap(html_dict[TAX])

    imap.update(general_map)
    imap.update(building_map)
    imap.update(tax_map)

    imap["prop_id"] = property_id
    imap["notes"] = None
    print(imap)
    return imap


def write_data_to_propertyexternaldata(insert_map, db_cursor):
    """
    Writes a row to propertyexternaldata, logging every property so changes can be tracked over time.
    ownerphone cannot be found by scraping the county site and are excluded from the insert:
    """
    insert_sql = """
        INSERT INTO public.propertyexternaldata(
            extdataid,
            property_propertyid, ownername, address_street, address_citystatezip,
            address_city, address_state, address_zip, saleprice,
            saleyear, assessedlandvalue, assessedbuildingvalue, assessmentyear,
            usecode, livingarea, condition, taxstatus,
            taxstatusyear, notes, lastupdated
        )
        VALUES(
            DEFAULT,
            %(prop_id)s, %(ownername)s, %(street)s, %(citystatezip)s,
            %(city)s, %(state)s, %(zip)s, %(saleprice)s,
            %(saleyear)s, %(assessedlandvalue)s, %(assessedbuildingvalue)s, %(assessmentyear)s,
            %(usecode)s, %(livingarea)s, %(condition)s, %(taxstatus)s,
            %(taxstatusyear)s, %(notes)s, now()
        );
    """
    # Testcode to remove all commas. Todo: Make data adhere to schema before we get to this point
    for key in insert_map:
        try:
            insert_map[key] = insert_map[key].replace(",", "")
        except AttributeError: # 'list object has no attribute 'replace'
            try:
                for s in insert_map[key]:
                    s = s.replace(",", "")
            except TypeError: # Nonetype
                continue
        except TypeError: # Nonetype: (I don't think this code can be reached)
            continue

    db_cursor.execute(insert_sql, insert_map)
    db_cursor.commit()


def check_if_data_is_different_than_previous(written_data):
    pass


def create_event():
    pass


if __name__ == '__main__':
    main()
