"""
Records an inquiry in external_data
If the data is different, creates writes an event to the event table
"""
# TODO: ADD OWNER MAILING TO PROPERTY EXTERNAL DATA

import requests
from collections import namedtuple


from Util.db_conn import get_db_and_cursor
from Util.parsing import (
    create_general_insertmap,
    create_building_insertmap,
    create_tax_insertmap
)


def main():
    with get_db_and_cursor() as db_cursor:
        rows = ask_database_for_parcelids(municode=828, db_cursor=db_cursor)
        for row in rows:

            html_dict = get_county_property_assessment(row.parcel_id)
            insert_map = create_insertmap_from_html(html_dict, row.property_id)
            write_data_to_propertyexternaldata(insert_map, db_cursor)
            did_the_data_change = check_if_data_is_different_than_previous(insert_map, db_cursor)
            if did_the_data_change:
                add_CodeViolationUpdate_event(row.property_id, db_cursor)
            db_cursor.commit()


# MAGIC STRINGS
GENERALINFO = "GeneralInfo"
BUILDING = "Building"
TAX = "Tax"

def get_county_property_assessment(parcel_id):
    """
    Grabs the raw HTML of a property from the county's website.
    Scrapes 3 pages: The GeneralInfo, Building, and Tax

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
        db_cursor: CursorAndDB_Conn

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
    # If we were scraping more, this would be written using map()
    general_map = create_general_insertmap(html_dict[GENERALINFO])
    building_map = create_building_insertmap(html_dict[BUILDING])
    tax_map = create_tax_insertmap(html_dict[TAX])

    imap.update(general_map)
    imap.update(building_map)
    imap.update(tax_map)

    imap["prop_id"] = property_id   # Todo: Update dict key naming convention to match column names
    imap["notes"] = None
    print(imap)
    return imap


def write_data_to_propertyexternaldata(insert_map, db_cursor):
    """
    Writes a row to propertyexternaldata, logging every property so changes can be tracked over time.

    Arguments:
        insert_map: dict
        db_cursor: CursorAndDB_Conn
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


def check_if_data_is_different_than_previous(insert_map, db_cursor):
    """

    If this a property's first entry into propertyexternaldata, create an event signifying it as such


    Arguments:
        insert_map: dict
        db_cursor: CursorAndDB_Conn

    Returns:
        bool
            True if there was a change
    """

    select_sql = """
        SELECT(
            property_propertyid, ownername, address_street, address_citystatezip,
            livingarea, condition, taxstatus
        )
            FROM public.propertyexternaldata
            WHERE property_propertyid = %(prop_id)s
            ORDER BY lastupdated DESC
            LIMIT 2;
    """

    db_cursor.execute(select_sql, insert_map)
    selection = db_cursor.fetchall()
    try:
        if selection[0] == selection[1]:
            return True
        return False
    except IndexError:  # If this is the first time the property_propertyid occurs in propertyexternaldata
        add_NewProperty_event(selection, db_cursor)
        return False


def add_NewProperty_event(selection, db_cursor):
    """
    Writes an event to the event table the first time a distinct property is added to propertyexternaldata.

    Arguments:
        selection: tuple(list)
            The selection returned
        db_cursor: CursorAndDB_Conn

    How it is currently implemented, this function runs only after update_script.py is run.
    This means a property can be created using insertPropDataFromCntyUsingParid.py but the New Property event
    isn't created.
    """
    row = selection[0][0]
    # row is a string, example '(205628,"Tony Monstero","123 Example Lane","PITTSBURGH PA 15221",1380,AVERAGE,PAID)'
    # Note that AVERAGE and PAID are not in quotes.
    # We could fix it so they are quoted so we could do ast.literal_eval(row), but it's easier to just get to use regex
    import re
    prop_id = re.search(r'\d+', row).group()

    print('new property created: ' + str(prop_id))
    insert_sql = """
            INSERT INTO public.event(
            category_catid, cecase_caseid, eventtimestamp,
            eventdescription, owner_userid, disclosetomunicipality, disclosetopublic,
            activeevent
        )
        VALUES(
            %(category_catid)s, %(cecase_caseid)s, now(),
            %(eventdescription)s, %(owner_userid)s, %(disclosetomunicipality)s, %(disclosetopublic)s,
            %(activeevent)s
        )
        """
    imap = {}
    imap["category_catid"] = 150    # New Property
    imap["eventdescription"] = "New Property!"
    imap["owner_userid"] = 99
    imap["disclosetomunicipality"] = False
    imap["disclosetopublic"] = False
    imap["activeevent"] = True


    caseid_sql = """
    SELECT caseid FROM cecase
        JOIN property ON cecase.property_propertyid = property.propertyid
        WHERE propertyid = %s;
    """
    db_cursor.execute(caseid_sql, [prop_id])
    imap["cecase_caseid"] = db_cursor.fetchone()[0]
    db_cursor.execute(insert_sql, imap)


def add_CodeViolationUpdate_event(property_id, db_cursor):
    """
    Writes an event to the event table whenever a property in propertyexternaldata changes.

    Arguments:
        property_id: int
            CodeConnect's primary key for property
        db_cursor: CursorAndDB_Conn

    Author's Note:
        If you need to create another function that inserts into event, create a helper function for your function,
        this function, and add_NewProperty_event.
    """
    # TODO: Create different category id's depending on what is different (name vs tax, etc)
    insert_sql = """
        INSERT INTO public.event(
            category_catid, cecase_caseid, eventtimestamp,
            eventdescription, owner_userid, disclosetomunicipality, disclosetopublic,
            activeevent
        )
        VALUES(
            %(category_catid)s, %(cecase_caseid)s, now(),
            %(eventdescription)s, %(owner_userid)s, %(disclosetomunicipality)s, %(disclosetopublic)s,
            %(activeevent)s
        )
    """
    imap = {}
    imap["category_catid"] = 117    # Code Violation Update
    imap["eventdescription"] = "Change in column"   # Todo: Write better description
    imap["owner_userid"] = 99
    imap["disclosetomunicipality"] = False
    imap["disclosetopublic"] = False
    imap["activeevent"] = True


    # Todo: This is not proper database notation. Ask Eric for help making a single query instead of 2
    caseid_sql = """
    SELECT caseid FROM cecase
        JOIN property ON cecase.property_propertyid = property.propertyid
        WHERE propertyid = %s;
    """
    db_cursor.execute(caseid_sql, [property_id])
    imap["cecase_caseid"] = db_cursor.fetchone()[0]
    db_cursor.execute(insert_sql, imap)


if __name__ == '__main__':
    main()
