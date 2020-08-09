#   Project files imported without an underscore should be read as a statement.
#   Example: scrape.county_property_assessments scrapes county property assessments.
#
#   Otherwise, treat the name as the module name.
#   Example: _parse.strip_whitespace strips whitespace, _parse.OwnerName is a class
import copy
import json
import _create as create
import _fetch as fetch
from _fetch import valid_json
import _write as write
import _scrape as scrape
import _events
import _parse
from _constants import Tally
from _constants import DEFAULT_PROP_UNIT
from _constants import DASHES, MEDIUM_DASHES, SHORT_DASHES, SPACE

from _events import ParcelNotInRecentRecords


def parcel_not_in_db(parid, cursor):
    select_sql = """
        SELECT parid FROM property
        WHERE parid = %s"""
    cursor.execute(select_sql, [parid])
    row = cursor.fetchone()
    if row is None:
        print("Parcel {} not in properties.".format(parid))
        return True
    return False


def compare(WPRDC_data, AlleghenyCountyData):
    if WPRDC_data != AlleghenyCountyData:
        raise ValueError(
            "The WPRDC's data does not match the data scraped from Allegheny County\n"
            f"\t WPRDC's: {WPRDC_data}\tCounty's: {AlleghenyCountyData}"
        )


# Todo: Validate more data
def validate_data(r, tax):  #   Example data as applicable to explain transformations
    #   WPRDC               Allegheny County
    compare(r["TAXYEAR"], int(tax.year))  #   2020.0              2020


def insert_and_update_database(record, conn, cursor, commit):
    """
    """
    parid = record["PARID"]
    html = scrape.county_property_assessments(parid)
    soup = _parse.soupify_html(html)
    owner_name = _parse.OwnerName.get_Owner_from_soup(soup)
    tax_status = _parse.parse_tax_from_soup(soup)

    if parcel_not_in_db(parid, cursor):
        new_parcel = True
        imap = create.property_insertmap(record)
        prop_id = write.property(imap, cursor)
        if record["PROPERTYUNIT"] == " ":
            unit_id = write.unit(
                {"unitnumber": DEFAULT_PROP_UNIT, "property_propertyid": prop_id},
                cursor,
            )
        else:
            print(record["PROPERTYUNIT"])
            unit_id = write.unit(
                {"unitnumber": record["PROPERTYUNIT"], "property_propertyid": prop_id,},
                cursor,
            )
        cecase_map = create.cecase_imap(prop_id, unit_id)
        cecase_id = write.cecase(cecase_map, cursor)
        #
        owner_map = create.owner_imap(owner_name, record)
        person_id = write.person(owner_map, cursor)
        #
        write.connect_property_to_person(prop_id, person_id, cursor)
        Tally.inserted += 1
    else:  # If the parcel was already in the database
        new_parcel = False
        prop_id = fetch.prop_id(parid, cursor)
        # If a property doesn't have an associated unit and cecase, one is created.
        unit_id = fetch.unit_id(prop_id, cursor)
        cecase_id = fetch.cecase_id(unit_id, cursor)

    validate_data(record, tax_status)
    tax_status_id = write.taxstatus(tax_status, cursor)
    propextern_map = create.propertyexternaldata_imap(
        prop_id, owner_name.raw, record, tax_status_id
    )
    # Property external data is a misnomer. It's just a log of the data from every time stuff
    write.propertyexternaldata(propextern_map, cursor)

    if _events.query_propertyexternaldata_for_changes_and_write_events(
        parid, prop_id, cecase_id, new_parcel, cursor
    ):
        Tally.updated += 1

    if commit:
        conn.commit()
    else:
        # A check to make sure variables weren't forgotten to be assigned. Maybe move to testing suite?
        assert [
            attr is not None
            for attr in [parid, new_parcel, prop_id, unit_id, cecase_id]
        ]

    # from _utils import pickler
    # pickler(html, "html", incr=False)
    # pickler(owner_name, "own", incr=False)
    # pickler(soup, "soup", incr=False)
    # pickler(imap, "prop_imap", incr=False)
    # pickler(cecase_map, "cecase_imap", incr=False)
    # pickler(owner_map, "owner_imap", incr=False)
    # pickler(propextern_map, "propext_imap", incr=True)

    Tally.total += 1
    print("Record count:", Tally.total, sep="\t")
    print("Inserted count:", Tally.inserted, sep="\t")
    print("Updated count:", Tally.updated, sep="\t")
    print(SHORT_DASHES)


def create_events_for_parcels_which_did_not_appear_in_records(
    record, municdode, db_conn, cursor, commit
):
    """
    Writes an event to the database for every parcel in a municipality that appears in the database but was not in the WPRDC's data.
    If a property doesn't have an associated unit and cecase, one is created.
    """
    all_parcels = fetch.all_parids_in_muni(municdode, cursor)
    remaining_parcels = copy.copy(all_parcels)
    for i, parcel in enumerate(all_parcels):
        for r in record:
            if parcel == r["PARID"]:
                remaining_parcels.pop(i)
                continue
    # At this point, `parcels` contains a list of muni's parcels that appeared in the database but not in the most recent record from the WPRDC.
    for parcel_id in remaining_parcels:
        prop_id = fetch.prop_id(parcel_id, cursor)
        cecase_id = fetch.cecase_id(prop_id, cursor)
        details = _events.EventDetails(parcel_id, prop_id, cecase_id, cursor)

        ParcelNotInRecentRecords(details).write_to_db()
    if commit:
        db_conn.execute()


def update_muni(muni, db_conn, commit=True):
    """
    The core functionality of the script.
    """
    print("Updating {} ({})".format(muni.name, muni.municode))
    print(MEDIUM_DASHES)
    filename = fetch.muni_data_and_write_to_file(muni)
    if not valid_json(filename):
        print(DASHES)
        return

    with open(filename, "r") as f:
        file = json.load(f)
        records = file["result"]["records"]

    with db_conn.cursor() as cursor:
        for record in records:
            insert_and_update_database(record, db_conn, cursor, commit)
        print(DASHES)

        create_events_for_parcels_which_did_not_appear_in_records(
            records, muni.municode, db_conn, cursor, commit
        )
        print(DASHES)
