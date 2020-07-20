"""

Important note: This Python script's work is not isolated.
Upon certain actions, the database will call various trigger functions.
Actions resulting from trigger functions are commented in the following syntax:
    # ~~ basic function description in pseudo code
"""

import json

import _create as create
import _events as events
from _events import parcel_changed
import _fetch as fetch
import _insert
import _scrape_and_parse as snp
from _constants import GENERALINFO, BUILDING, TAX, SALES
from _constants import DASHES, MEDIUM_DASHES, SHORT_DASHES, SPACE
from _constants import DEFAULT_PROP_UNIT


def parcel_not_in_db(parid, db_cursor):
    select_sql = """
        SELECT parid FROM property
        WHERE parid = %s"""
    db_cursor.execute(select_sql, [parid])
    row = db_cursor.fetchone()
    if row is None:
        print("Parcel {} not in properties.".format(parid))
        return True
    return False


def write_property_to_db(imap, db_cursor):
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
    db_cursor.execute(insert_sql, imap)
    return db_cursor.fetchone()[0]  # Returns the property_id


def update_property_in_db(propid, imap, db_cursor):
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
    db_cursor.execute(insert_sql, imap)
    return db_cursor.fetchone()[0]  # Returns the property_id


def write_person_to_db(record, db_cursor):
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
    db_cursor.execute(insert_sql, record)
    return db_cursor.fetchone()[0]


def connect_property_to_person(prop_id, person_id, db_cursor):
    propperson = {"prop_id": prop_id, "person_id": person_id}
    insert_sql = """
        INSERT INTO public.propertyperson(
            property_propertyid, person_personid    
        )
        VALUES(
            %(prop_id)s, %(person_id)s
        );
    """
    db_cursor.execute(insert_sql, propperson)


def compare(WPRDC_data, AlleghenyCountyData):
    if WPRDC_data != AlleghenyCountyData:
        raise ValueError(
            "The WPRDC's data does not match the data scraped from Allegheny County"
        )


def validate_data(r, tax):
    # Todo: Validate more data
    compare(r["TAXYEAR"], int(snp.strip_whitespace(tax.year)))


def write_propertyexternaldata(propextern_map, db_cursor):
    insert_sql = """
        INSERT INTO public.propertyexternaldata(
            extdataid,
            property_propertyid, ownername, address_street, address_citystatezip,
            address_city, address_state, address_zip, saleprice,
            saleyear, assessedlandvalue, assessedbuildingvalue, assessmentyear,
            usecode, livingarea, condition, taxstatus,
            taxstatusyear, notes, lastupdated, tax,
            taxcode, taxsubcode
        )
        VALUES(
            DEFAULT,
            %(property_propertyid)s, %(ownername)s, %(address_street)s, %(address_citystatezip)s,
            %(address_city)s, %(address_state)s, %(address_zip)s, %(saleprice)s,
            %(saleyear)s, %(assessedlandvalue)s, %(assessedbuildingvalue)s, %(assessmentyear)s,
            %(usecode)s, %(livingarea)s, %(condition)s, %(taxstatus)s,
            %(taxstatusyear)s, %(notes)s, now(), %(tax)s,
            %(taxcode)s, %(taxsubcode)s
        )
        RETURNING property_propertyid;
    """
    db_cursor.execute(insert_sql, propextern_map)
    return db_cursor.fetchone()[0]  # property_id


def update_muni(muni, db_cursor, commit=True):
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

    # Debugging tools
    record_count = 0
    inserted_count = 0
    updated_count = 0

    for record in records:
        parcel_flags = events.Property
        parid = record["PARID"]

        data = snp.scrape_county_property_assessments(parid, pages=[TAX])
        for page in data:
            data[page] = snp.soupify_html(data[page])
        owner_name = snp.OwnerName.get_Owner_from_soup(data[TAX])
        tax_status = snp.parse_tax_from_soup(data[TAX])

        if parcel_not_in_db(parid, db_cursor):
            parcel_flags.new_parcel = True
            imap = create.insertmap_from_record(record)
            prop_id = write_property_to_db(imap, db_cursor)

            if record["PROPERTYUNIT"] == " ":
                unit_id = _insert.unit(
                    {"unitnumber": DEFAULT_PROP_UNIT, "property_propertyid": prop_id},
                    db_cursor,
                )
            else:
                print(record["PROPERTYUNIT"])
                unit_id = _insert.unit(
                    {
                        "unitnumber": record["PROPERTYUNIT"],
                        "property_propertyid": prop_id,
                    },
                    db_cursor,
                )
            cecase_map = create.cecase_imap(prop_id, unit_id)
            _insert.cecase(cecase_map, db_cursor)

            owner_map = create.owner_imap(owner_name, record)
            person_id = write_person_to_db(owner_map, db_cursor)

            # ~~ Update Spelling (Not implemented)

            connect_property_to_person(prop_id, person_id, db_cursor)
            inserted_count += 1
            inserted_flag = True
        else:
            prop_id = fetch.propid(parid, db_cursor)
            # We have to scrape this again to see if it changed

        validate_data(record, tax_status)
        propextern_map = create.propertyexternaldata_imap(
            prop_id, owner_name.raw, record, tax_status
        )
        # Property external data is a misnomer. It's just a log of the data from every time stuff
        write_propertyexternaldata(propextern_map, db_cursor)

        if flags := parcel_changed(prop_id, parcel_flags, db_cursor):
            if flags.new_parcel:
                event = events.NewParcelidEvent(parid, prop_id, db_cursor)
                event.write_to_db(
                    db_cursor
                )
            else:
                if flags.ownername:
                    event = events.DifferentOwnerEvent(
                        parid, prop_id, flags.ownername, db_cursor
                    )
                    event.write_to_db(db_cursor)
                if flags.street:
                    event = events.DifferentStreetEvent(
                        parid, prop_id, flags.street, db_cursor
                    )
                    event.write_to_db(db_cursor)
                if flags.citystatezip:
                    event = events.DifferentCityStateZip(
                        parid, prop_id, flags.citystatezip, db_cursor
                    )
                    event.write_to_db(db_cursor)
                if flags.livingarea:
                    event = events.DifferentLivingArea(
                        parid, prop_id, flags.livingarea, db_cursor
                    )
                    event.write_to_db(db_cursor)
                if flags.condition:
                    event = events.DifferentCondition(
                        parid, prop_id, flags.condition, db_cursor
                    )
                    event.write_to_db(db_cursor)
                if flags.taxstatus:
                    event = events.DifferentTaxStatus(
                        parid, prop_id, flags.taxstatus, db_cursor
                    )
                    event.write_to_db(db_cursor)
                if flags.taxcode:
                    event = events.DifferentTaxCode(
                        parid, prop_id, flags.taxcode, db_cursor
                    )
                    event.write_to_db(db_cursor)
                updated_count += 1

        if commit:
            db_cursor.commit()

        record_count += 1
        print("Record count:\t", record_count, sep="")
        print("Inserted count:\t", inserted_count, sep="")
        print("Updated count:\t", updated_count, sep="")
        print(SHORT_DASHES)
    print(DASHES)
