#   Project files imported without an underscore should be read as a statement.
#   Example: scrape.county_property_assessments scrapes county property assessments.
#
#   Otherwise, treat the name as the module name.
#   Example: _parse.strip_whitespace strips whitespace, _parse.OwnerName is a class

import json
import create as create
import fetch as fetch
from fetch import valid_json
import write as write
import scrape as scrape
import events
import parse
from common import Tally
from common import DEFAULT_PROP_UNIT
from common import DASHES, MEDIUM_DASHES, SHORT_DASHES, SPACE


def download_and_read_records_from_Wprdc(muni):
    print("Updating {} ({})".format(muni.name, muni.municode))
    print(MEDIUM_DASHES)
    filename = fetch.muni_data_and_write_to_file(muni)
    if not valid_json(filename):
        print(DASHES)
        return

    with open(filename, "r") as f:
        file = json.load(f)
        records = file["result"]["records"]
    return records


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
def validate_data(r, tax):
    #                                       #   WPRDC               Allegheny County
    compare(r["TAXYEAR"], int(tax.year))  # #   2020.0              2020


def update_database(record, conn, cursor, commit):
    """
    """
    parid = record["PARID"]
    html = scrape.county_property_assessment(parid)
    soup = parse.soupify_html(html)
    owner_name = parse.OwnerName.from_soup(soup)
    tax_status = parse.parse_tax_from_soup(soup)

    if parcel_not_in_db(parid, cursor):
        new_parcel = True
        imap = create.property_insertmap(record)
        prop_id = write.property(imap, cursor)
        #
        if record["PROPERTYUNIT"] == " ":
            unit_num = DEFAULT_PROP_UNIT
        else:
            unit_num = record["PROPERTYUNIT"]
        unit_id = write.unit(
            {"unitnumber": unit_num, "property_propertyid": prop_id}, cursor
        )
        #
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
        cecase_id = fetch.cecase_id(prop_id, cursor)

    validate_data(record, tax_status)
    tax_status_id = write.taxstatus(tax_status, cursor)
    propextern_map = create.propertyexternaldata_imap(
        prop_id, owner_name.raw, record, tax_status_id
    )
    # Property external data is a misnomer. It's just a log of the data from every time stuff
    write.propertyexternaldata(propextern_map, cursor)

    if events.query_propertyexternaldata_for_changes_and_write_events(
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

    # from utils import pickler
    # pickler(record, "record", to_type="json", incr=False)
    # pickler(html, "html", to_type="html", incr=True)
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


def create_events_for_parcels_in_db_but_not_in_records(
    records, municdode, db_conn, cursor, commit
):
    """
    Writes an event to the database for every parcel in a municipality that appears in the database but was not in the WPRDC's data.
    If a property doesn't have an associated unit and cecase, one is created.
    """
    # Get parcels in the database but not in the WPRDC record
    db_parcels = fetch.all_parids_in_muni(municdode, cursor)
    wprdc_parcels = [r["PARID"] for r in records]
    extra_parcels = set(db_parcels) - set(wprdc_parcels)
    for parcel_id in extra_parcels:
        prop_id = fetch.prop_id(parcel_id, cursor)
        cecase_id = fetch.cecase_id(prop_id, cursor)
        details = events.EventDetails(parcel_id, prop_id, cecase_id, cursor)
        details.old = municdode
        # Creates DifferentMunicode or NotInRealEstatePortal
        # If DifferentMunicode, supplies the new muni
        event = events.parcel_not_in_wprdc_data(details)
        event.write_to_db()
        Tally.diff_count += 1
    if commit:
        # db_conn.execute()
        db_conn.commit()
