#   Project files imported without an underscore should be read as a statement.
#   Example: scrape.county_property_assessments scrapes county property assessments.
#
#   Otherwise, treat the name as the module name.
#   Example: _parse.strip_whitespace strips whitespace, _parse.OwnerName is a class

import json
import _create as create
import _fetch as fetch
import _insert as insert
import _scrape as scrape
import _events
import _parse
from _constants import Tally
from _constants import GENERALINFO, BUILDING, TAX, SALES
from _constants import DEFAULT_PROP_UNIT
from _constants import DASHES, MEDIUM_DASHES, SHORT_DASHES, SPACE


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


def write_property_to_db(imap, cursor):
    # Todo: Write function in a way so that we can reuse the insert sql for the alter sql
    insert_sql = """
        INSERT INTO property(
            propertyid, municipality_municode, parid, lotandblock,
            address, usegroup, constructiontype, countycode,
            notes, addr_city, addr_state, addr_zip,
            ownercode, propclass, lastupdated, lastupdatedby,
            locationdescription, bobsource_sourceid, creationts            
        )
        VALUES(
            DEFAULT, %(municipality_municode)s, %(parid)s, %(lotandblock)s,
            %(address)s, %(usegroup)s, %(constructiontype)s, %(countycode)s,
            %(notes)s, %(addr_city)s, %(addr_state)s, %(addr_zip)s,
            %(ownercode)s, %(propclass)s, now(), %(lastupdatedby)s, %(locationdescription)s,
            %(bobsource)s, now()
        )
        RETURNING propertyid;
    """
    cursor.execute(insert_sql, imap)
    return cursor.fetchone()[0]  # Returns the property_id


def update_property_in_db(propid, imap, cursor):
    # Todo: Write function in a way so that we can reuse the insert sql for the alter sql
    imap["propertyid"] = propid
    insert_sql = """
        UPDATE property SET(
            municipality_municode = %(municipality_municode)s,
            parid = %(parid)s,
            lotandblock = %(lotandblock)s,
            address = %(address)s,
            usegroup = %(usegroup)s,
            constructiontype = %(constructiontype)s,
            countycode = %(countycode)s,
            notes = %(notes)s,
            addr_city = %(addr_city)s,
            addr_state = %(addr_state)s,
            addr_zip = %(addr_zip)s,
            ownercode = %(ownercode)s,
            propclass = %(propclass)s,
            lastupdated = now(),
            lastupdatedby = %(lastupdatedby)s,
            locationdescription) = %(locationdescription)s,
            bobsource = %(bobsource)s
        )
        WHERE propertyid = %(propertyid)
    """
    cursor.execute(insert_sql, imap)
    return cursor.fetchone()[0]  # Returns the property_id


def write_person_to_db(record, cursor):
    insert_sql = """
        INSERT INTO public.person(
            persontype, muni_municode, fname, lname, 
            jobtitle, phonecell, phonehome, phonework, 
            email, address_street, address_city, address_state, 
            address_zip, notes, lastupdated, expirydate, 
            isactive, isunder18, humanverifiedby, rawname,
            cleanname, compositelname, multientity)
        VALUES(
            cast ( 'ownercntylookup' as persontype), %(muni_municode)s, %(fname)s, %(lname)s,
            %(jobtitle)s, %(phonecell)s, %(phonehome)s, %(phonework)s,
            %(email)s, %(address_street)s, %(address_city)s, %(address_state)s,
            %(address_zip)s, %(notes)s, now(), %(expirydate)s,
            %(isactive)s, %(isunder18)s, %(humanverifiedby)s, %(rawname)s,
            %(cleanname)s, %(compositelname)s, %(multientity)s
        )
        RETURNING personid;
    """
    cursor.execute(insert_sql, record)
    return cursor.fetchone()[0]


def connect_property_to_person(prop_id, person_id, cursor):
    propperson = {"prop_id": prop_id, "person_id": person_id}
    insert_sql = """
        INSERT INTO public.propertyperson(
            property_propertyid, person_personid    
        )
        VALUES(
            %(prop_id)s, %(person_id)s
        );
    """
    cursor.execute(insert_sql, propperson)


def compare(WPRDC_data, AlleghenyCountyData):
    if WPRDC_data != AlleghenyCountyData:
        raise ValueError(
            "The WPRDC's data does not match the data scraped from Allegheny County\n"
            f"\t WPRDC's: {WPRDC_data}\tCounty's: {AlleghenyCountyData}"
        )

# Todo: Validate more data
def validate_data(r, tax):                  #   Example data as applicable to explain transformations
                                            #   WPRDC               Allegheny County
    compare(r["TAXYEAR"], int(tax.year))    #   2020.0              2020


def write_propertyexternaldata(propextern_map, cursor):
    insert_sql = """
        INSERT INTO public.propertyexternaldata(
            extdataid,
            property_propertyid, ownername, address_street, address_citystatezip,
            address_city, address_state, address_zip, saleprice,
            saleyear, assessedlandvalue, assessedbuildingvalue, assessmentyear,
            usecode, livingarea, condition, 
            notes, lastupdated, taxstatus_taxstatusid
        )
        VALUES(
            DEFAULT,
            %(property_propertyid)s, %(ownername)s, %(address_street)s, %(address_citystatezip)s,
            %(address_city)s, %(address_state)s, %(address_zip)s, %(saleprice)s,
            %(saleyear)s, %(assessedlandvalue)s, %(assessedbuildingvalue)s, %(assessmentyear)s,
            %(usecode)s, %(livingarea)s, %(condition)s,
            %(notes)s, now(), %(taxstatus_taxstatusid)s
        )
        RETURNING property_propertyid;
    """
    cursor.execute(insert_sql, propextern_map)
    return cursor.fetchone()[0]  # property_id


def write_taxstatus(tax_status, cursor):
    insert_sql = """
        INSERT INTO taxstatus(
            year, paidstatus, tax, penalty,
            interest, total, datepaid
        )
        VALUES(
            %(year)s, %(paidstatus)s, %(tax)s, %(penalty)s,
            %(interest)s, %(total)s, %(date_paid)s
        )
        returning taxstatusid;
    """
    cursor.execute(insert_sql, tax_status._asdict())    # Todo: For fun, learn speed of tuple -> dict
    return cursor.fetchone()[0] # taxstatus_id



def update_muni(muni, db_conn, commit=True):
    """
    The core functionality of the script.
    """

    print("Updating {} ({})".format(muni.name, muni.municode))
    print(MEDIUM_DASHES)
    # We COULD not save the file and work only in JSON,
    # but saving the file is better for understanding what happened
    filename = fetch.muni_data_and_write_to_file(muni)
    if not fetch.validate_muni_json(filename):
        print(DASHES)
        return

    with open(filename, "r") as f:
        file = json.load(f)
        records = file["result"]["records"]

    with db_conn.cursor() as cursor:
        for record in records:
            parid = record["PARID"]

            data = scrape.county_property_assessments(parid, pages=[TAX])
            for page in data:
                data[page] = _parse.soupify_html(data[page])

            owner_name = _parse.OwnerName.get_Owner_from_soup(data[TAX])        ## TODO: UNIT TESTS START HERE ^
            tax_status = _parse.parse_tax_from_soup(data[TAX])

            # This block of code initializes the following:
            #   Variables:  prop_id, unit_id, cecase_id
            #   Flags:      new_parcel
            if parcel_not_in_db(parid, cursor):
                new_parcel = True
                imap = create.property_insertmap(record)
                prop_id = write_property_to_db(imap, cursor)
                if record["PROPERTYUNIT"] == " ":
                    unit_id = insert.unit(
                        {"unitnumber": DEFAULT_PROP_UNIT, "property_propertyid": prop_id},
                        cursor,
                    )
                else:
                    print(record["PROPERTYUNIT"])
                    unit_id = insert.unit(
                        {
                            "unitnumber": record["PROPERTYUNIT"],
                            "property_propertyid": prop_id,
                        },
                        cursor,
                    )
                cecase_map = create.cecase_imap(prop_id, unit_id)
                cecase_id = insert.cecase(cecase_map, cursor)
                #
                owner_map = create.owner_imap(owner_name, record)
                person_id = write_person_to_db(owner_map, cursor)
                #
                connect_property_to_person(prop_id, person_id, cursor)
                Tally.inserted += 1
            else:
                new_parcel = False
                prop_id = fetch.prop_id(parid, cursor)
                unit_id = fetch.unit_id(prop_id, cursor)
                if not unit_id:
                    unit_id = insert.unit(
                        {"unitnumber": DEFAULT_PROP_UNIT, "property_propertyid": prop_id},
                        cursor,
                    )
                # TODO: ERROR: Property exists without property unit
                cecase_id = fetch.cecase_id(unit_id, cursor)
                if not cecase_id:
                    cecase_map = create.cecase_imap(prop_id, unit_id)
                    cecase_id = insert.cecase(cecase_map, cursor)
                    # TODO: ERROR: Property exists without cecase

            validate_data(record, tax_status)
            tax_status_id = write_taxstatus(tax_status, cursor)
            propextern_map = create.propertyexternaldata_imap(
                prop_id, owner_name.raw, record, tax_status_id
            )
            # Property external data is a misnomer. It's just a log of the data from every time stuff
            write_propertyexternaldata(propextern_map, cursor)

            if _events.query_propertyexternaldata_for_changes_and_write_events(
                parid, prop_id, cecase_id, new_parcel, cursor
            ):
                Tally.updated +=1


            if commit:
                db_conn.commit()
            else:
                # A check to make sure variables weren't forgotten to be assigned. Maybe move to testing suite?
                assert [attr is not None for attr in [parid, new_parcel, prop_id, unit_id, cecase_id]]

            Tally.total += 1
            print("Record count:", Tally.total, sep="\t")
            print("Inserted count:", Tally.inserted, sep="\t")
            print("Updated count:", Tally.updated, sep="\t")
            print(SHORT_DASHES)
        print(DASHES)
