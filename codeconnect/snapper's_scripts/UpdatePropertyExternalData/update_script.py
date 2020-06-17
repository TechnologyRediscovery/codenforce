"""
Records an inquiry in external_data
If the data is different, creates writes an event to the event table
"""
# TODO: ADD OWNER MAILING TO PROPERTY EXTERNAL DATA

from collections import namedtuple

from Util.db_conn import get_cursor
from Util.scraping_and_parsing import (
    get_county_property_assessment,
    soupify_html,
    extract_info_from_soup,
    extract_address_parts,
)

from Exceptions._exceptions import MalformedDataError


def main():
    rows = ask_database_for_parcelids(municode=828)   # Todo: Better naming? It's property and parcel id.
    for row in rows:

        raw_html = get_county_property_assessment(row.parcel_id)
        insert_map = create_insertmap_from_html(raw_html)
        written_data = write_data_to_propertyexternaldata(insert_map)
        did_the_data_change = check_if_data_is_different_than_previous(written_data)
        if did_the_data_change:
            create_event()


def ask_database_for_parcelids(municode=None, muniname=None):
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
    with get_cursor() as cursor:
        if municode:
            select_sql = "SELECT propertyid, parid FROM public.property WHERE municipality_municode = %s"
        elif muniname:
            # mild Todo: allow different user inputs so the script can be run easily by code enforcement officers
            raise NotImplementedError
        cursor.execute(select_sql, [municode])
        parcel_ids = cursor.fetchall()

    for row in parcel_ids:
        yield Row(*row)  # Casts each tuple returned by the cursor to the namedtuple 'Row'


db_to_span= {
    # Todo: Document and find proper place in code to put this
    # Consider not using magic strings?
    "ownername": "BasicInfo1_lblOwner",
    "fulladdress": "BasicInfo1_lblAddress",
    "ownermailing": "lblChangeMail",
    "saleprice": "lblSalePrice",
    "saledate": "lblSaleDate",      # -> saleyear
    "assessedlandvalue": "lblCountyLand",
    "assessedbuildingvalue": "lblCountyBuild",
    "assessmentyear": "LCounty", # re.numbers
    "usecode": "lblUse",
}


def create_insertmap_from_html(raw_html):
    """
    Arguments:
          raw_html: str
          The raw HTML scraped from the county website

    Returns:
        Dict
            A dictionary of information extracted from the HTML
    """
    imap = {}   # insertmap
    soup = soupify_html(raw_html)
    for key in db_to_span:
        # NOTE: ownername may be a string or a list
        imap[key] = extract_info_from_soup(soup, db_to_span[key])

    # TODO: IMPORTANT: Add Try/Except MalformedDataError
    address_map = extract_address_parts(imap['fulladdress'])
    imap.update(address_map)

    for key in [
        "saleprice", "saledate", "assessedlandvalue", "assessedbuildingvalue",
    ]:
        imap[key] = imap[key].lstrip('$')
    imap["saleyear"] = imap["saledate"][-4:]
    imap["assessmentyear"] = imap["assessmentyear"][:4]
    print(imap)
    return imap


def write_data_to_propertyexternaldata(insert_map):
    """

    The columns ownerphone and yearbuilt cannot be found by scraping the county site
    """
    pass


def check_if_data_is_different_than_previous(written_data):
    pass


def create_event():
    pass


if __name__ == '__main__':
    main()
