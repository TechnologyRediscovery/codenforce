"""

Important note: This Python script's work is not isolated.
Upon certain actions, the database will call various trigger functions.
Actions resulting from trigger functions are commented in the following syntax:
    # ~~ basic function description in pseudo code
"""

import json

import create
import events
from events import parcel_changed
import fetch
import insert
import _scrape_and_parse as snp
from _constants import GENERALINFO, BUILDING, TAX, SALES
from _constants import DASHES, MEDIUM_DASHES, SHORT_DASHES, SPACE
from _constants import DEFAULT_PROP_UNIT, BOT_ID


def parcel_not_in_db(parid, db_cursor):
    select_sql = """
        SELECT parid FROM property
        WHERE parid = %s"""
    # TODO: ALLOW DUPLICATE PAIRDS?
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


def create_insertmap_from_record(r):
    """

    Arguments:
        r: dict
            The dictionized JSON record for a parcel id provided by the WPRDC
    Returns:
        dict
    """
    # Todo: This is a mess. Go over with others
    imap = {}
    # imap["propertyid"] = None
    imap["municipality_municode"] = r["MUNICODE"]
    imap["parid"] = r["PARID"]
    # imap["lotandblock"] = extract_lotandblock_from_parid(imap["PARID"])
    imap["usegroup"] = r[
        "USEDESC"
    ]  # ? I THINK this is what we want? Example, MUNICIPAL GOVERMENT.
    imap["constructiontype"] = None  # ?
    imap["countycode"] = None  # ? 02
    imap["notes"] = "Data from the WPRDC API"

    imap["lotandblock"] = ""  # TODO: FIGURE OUT LOT AND BLOCK

    # TODO: MAKE SURE THIS IS THE CORRECT DATA
    imap["address"] = SPACE.join((r["PROPERTYHOUSENUM"], r["PROPERTYADDRESS"]))
    imap["address_extra"] = r["PROPERTYFRACTION"]  # TODO: Add column
    imap["addr_city"] = r["PROPERTYCITY"]
    imap["addr_state"] = r["PROPERTYSTATE"]
    imap["addr_zip"] = r["PROPERTYZIP"]
    imap["ownercode"] = r[
        "OWNERDESC"
    ]  # Todo: Verify there is an ownercode to ownerdesc table
    imap["propclass"] = r["CLASS"]  # Todo: Verify
    imap["lastupdatedby"] = BOT_ID
    imap["locationdescription"] = None
    imap["bobsource"] = None
    return imap


def create_owner_insertmap(name, r):
    imap = {}
    imap["muni_municode"] = r["MUNICODE"]

    imap["jobtitle"] = None
    imap["phonecell"] = None
    imap["phonehome"] = None
    imap["phonework"] = None
    imap["email"] = None
    # TODO: Change our database to match theirs

    imap["mailing1"] = r["CHANGENOTICEADDRESS1"]
    imap["mailing2"] = r["CHANGENOTICEADDRESS2"]
    imap["mailing3"] = r["CHANGENOTICEADDRESS3"]
    imap["mailing4"] = r["CHANGENOTICEADDRESS4"]

    # Todo: Deprecate
    imap["address_street"] = r["CHANGENOTICEADDRESS1"]
    imap["address_city"] = r["CHANGENOTICEADDRESS3"].rstrip(" PA")
    imap["address_state"] = "PA"
    imap["address_zip"] = r["CHANGENOTICEADDRESS4"]
    imap[
        "notes"
    ] = "In case of confusion, check automated record entry with raw text from the county database."
    imap["expirydate"] = None
    imap["isactive"] = True
    imap["isunder18"] = None
    imap["humanverifiedby"] = None
    imap["rawname"] = name.raw
    imap["cleanname"] = name.clean
    imap["fname"] = name.first
    imap["lname"] = name.last
    imap["multientity"] = name.multientity
    imap["compositelname"] = name.compositelname
    return imap


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


def validate_data(r, owner, tax):
    # Todo: Validate more data
    compare(r["TAXYEAR"], int(snp.strip_whitespace(tax.year)))


def create_propertyexternaldata_map(prop_id, name, r, tax_status):
    # Yes, this is basically duplicate code.
    # However, explicitly restating what record data maps to insert data makes the code easier to both read and write.

    imap = {}
    imap["property_propertyid"] = prop_id
    imap["ownername"] = name
    imap["address_street"] = SPACE.join((r["PROPERTYHOUSENUM"], r["PROPERTYADDRESS"]))
    imap["address_city"] = r["PROPERTYCITY"]
    imap["address_state"] = "PA"
    imap["address_zip"] = r["PROPERTYZIP"]
    imap["address_citystatezip"] = SPACE.join(
        (imap["address_city"], imap["address_state"], imap["address_zip"])
    )
    imap["saleprice"] = r["SALEPRICE"]
    imap["saledate"] = r["SALEDATE"]  # Todo: Add column to databse
    try:
        imap["saleyear"] = r["SALEDATE"][-4:]
    except TypeError:
        imap["saleyear"] = None
    imap["assessedlandvalue"] = r["COUNTYLAND"]
    imap["assessedbuildingvalue"] = r["COUNTYBUILDING"]
    imap["assessmentyear"] = r[
        "TAXYEAR"
    ]  # BIG TODO: IMPORTANT: Scrape assessment year from county
    imap["usecode"] = r["USECODE"]
    imap["livingarea"] = r["FINISHEDLIVINGAREA"]
    imap["condition"] = r["CONDITION"]  # Todo: Condition to condition desc table
    imap["taxcode"] = r["TAXCODE"]
    # Applies to only to Public Utility Realty Tax Act
    #   If taxes are paid, they are paid into a state fund rather than to local taxing bodies
    imap["taxsubcode"] = r["TAXSUBCODE"]
    imap["taxstatus"] = tax_status.status

    #   The WPRDC pads their year with a nonbreaking space
    imap["taxstatusyear"] = r["TAXYEAR"]

    imap["tax"] = tax_status.tax

    imap["notes"] = SPACE.join(("Scraped by bot", BOT_ID))

    return imap
    # imap["lastupdated"]


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





def create_unit_map(prop_id, unit_id):
    imap = {}
    imap["property_propertyid"] = prop_id
    imap["unitnumber"] = unit_id
    return imap


def create_PropertyInfoChange_imap(cecase_id):
    imap = {}
    imap["cecase_caseid"] = cecase_id
    imap["category_catid"] = 300  # Property Info Update
    imap["eventdescription"] = "Change in column"  # Todo: Write better description
    imap["creator_userid"] = BOT_ID
    imap["lastupdatedby_userid"] = BOT_ID
    return imap


def writePropertyInfoChangeEvent(cvu_map, db_cursor):
    # TODO: Create different category id's depending on what is different (name vs tax, etc)
    insert_sql = """
        INSERT INTO public.event(
            category_catid, cecase_caseid, creationts,
            eventdescription, creator_userid, 
            timestart, timeend, lastupdatedby_userid, lastupdatedts
        )
        VALUES(
            %(category_catid)s, %(cecase_caseid)s, now(),
            %(eventdescription)s, %(creator_userid)s,
            now(), now(), %(lastupdatedby_userid)s, now()
        )"""
    db_cursor.execute(insert_sql, cvu_map)





def update_muni(muni, db_cursor, commit=True):
    """
    The core functionality of the script.
    """

    print("Updating {} ({})".format(muni.name, muni.municode))
    print(MEDIUM_DASHES)
    # We COULD not save the file and work only in JSON,
    # but saving the file is better for understanding what happened
    filename = fetch.fetch_muni_data_and_write_to_file(muni)
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
        parcel_flags = events.ParcelFlags
        parid = record["PARID"]

        data = snp.scrape_county_property_assessments(parid, pages=[TAX])
        for page in data:
            data[page] = snp.soupify_html(data[page])
        owner_name = snp.OwnerName.get_Owner_from_soup(data[TAX])
        tax_status = snp.parse_tax_from_soup(data[TAX])

        if parcel_not_in_db(parid, db_cursor):
            parcel_flags.new_parcel = True
            imap = create_insertmap_from_record(record)
            prop_id = write_property_to_db(imap, db_cursor)

            # TODO: Put code block in function
            if record["PROPERTYUNIT"] == " ":
                unit_id = insert.unit(
                    {"unitnumber": DEFAULT_PROP_UNIT, "property_propertyid": prop_id},
                    db_cursor,
                )
            else:
                print(record["PROPERTYUNIT"])
                unit_id = insert.unit(
                    {
                        "unitnumber": record["PROPERTYUNIT"],
                        "property_propertyid": prop_id,
                    },
                    db_cursor,
                )
            cecase_map = create.cecase_imap(prop_id, unit_id)
            insert.cecase(cecase_map, db_cursor)

            owner_map = create_owner_insertmap(owner_name, record)
            person_id = write_person_to_db(owner_map, db_cursor)

            # ~~ Update Spelling (Not implemented)

            connect_property_to_person(prop_id, person_id, db_cursor)
            inserted_count += 1
            inserted_flag = True
        else:
            prop_id = fetch.get_propid(parid, db_cursor)
            # We have to scrape this again to see if it changed

        validate_data(record, owner_name, tax_status)
        propextern_map = create_propertyexternaldata_map(
            prop_id, owner_name.raw, record, tax_status
        )
        # Property external data is a misnomer. It's just a log of the data from every time stuff
        write_propertyexternaldata(propextern_map, db_cursor)

        if flags := parcel_changed(prop_id, parcel_flags, db_cursor):
            if flags.new_parcel:
                event = events.NewParcelidEvent(parid, prop_id, db_cursor)
                event.write_to_db(
                    db_cursor
                )  # Todo: Should these two lines be a single line?

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
                    event.write_to_db()
                updated_count += 1

        if commit:
            db_cursor.commit()

        record_count += 1
        print("Record count:\t", record_count, sep="")
        print("Inserted count:\t", inserted_count, sep="")
        print("Updated count:\t", updated_count, sep="")
        print(SHORT_DASHES)
    print(DASHES)
