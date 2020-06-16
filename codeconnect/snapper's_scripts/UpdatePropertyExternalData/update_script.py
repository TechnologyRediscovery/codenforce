"""
Records an inquiry in external_data
If the data is different, creates writes an event to the event table
"""

from collections import namedtuple

from Auxiliary.db_conn import get_cursor
from Auxiliary.scraping_and_parsing import (
    get_county_property_assessment,
    extract_owner_name,
)



def main():
    rows = ask_database_for_parcelids(municode=828)   # Todo: Better naming? It's property and parcel id.
    for row in rows:
        print(row)
        raw_html = get_county_property_assessment(row.parcel_id)
        parsed_data = extract_info_from_html(raw_html)
        written_data = write_data_to_propertyexternaldata(parsed_data)
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


def extract_info_from_html(raw_html):
    """
    Arguments:
          raw_html: str
          The raw HTML scraped from the county website

    Returns:
        Dict
            A dictionary of information extracted from the HTML
    """
    insertmap = {}
    insertmap['ownername'] = extract_owner_name(raw_html)
    return insertmap


def write_data_to_propertyexternaldata(parsed_data):
    pass


def check_if_data_is_different_than_previous(written_data):
    pass


def create_event():
    pass


if __name__ == '__main__':
    main()
