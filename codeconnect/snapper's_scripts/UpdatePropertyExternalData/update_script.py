"""
Records an inquiry in external_data
If the data is different, creates writes an event to the event table
"""

from Auxiliary.db_conn import get_cursor


def main():
    parcel_ids = ask_database_for_parcelids(municode=828)
    for parid in parcel_ids:
        print(parid)
        raw_html = scrape_county_prop_assesment()
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
        int:
            The property id associated with each parcel
    """
    with get_cursor() as cursor:
        if municode:
            select_sql = "SELECT propertyid FROM public.property WHERE municipality_municode = %s"
        elif muniname:
            # mild Todo: allow different user inputs so the script can be run easily by code enforcement officers
            raise NotImplementedError
        cursor.execute(select_sql, [municode])
        parcel_ids = cursor.fetchall()

    for row in parcel_ids:
        yield row[0]  # Each row is a tuple containing a single property id. We only want the property id.


def scrape_county_prop_assesment():
    pass


def extract_info_from_html(raw_html):
    pass


def write_data_to_propertyexternaldata(parsed_data):
    pass


def check_if_data_is_different_than_previous(written_data):
    pass


def create_event():
    pass


if __name__ == '__main__':
    main()
