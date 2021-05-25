def property(imap, cursor):
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


def unit(imap, cursor):
    insert_sql = """
        INSERT INTO public.propertyunit(
            unitid, unitnumber, property_propertyid, otherknownaddress, notes, 
            rental)
        VALUES(
            DEFAULT, %(unitnumber)s, %(property_propertyid)s, NULL, 
            'robot-generated unit representing the primary habitable dwelling on a property', 
            FALSE)
        RETURNING unitid;
    """
    cursor.execute(insert_sql, imap)
    return cursor.fetchone()[0]  # unit_id


def cecase(imap, cursor):
    insert_sql = """INSERT INTO public.cecase(
        caseid, cecasepubliccc, property_propertyid, propertyunit_unitid,
        login_userid, casename, originationdate,
        closingdate, creationtimestamp, notes, paccenabled,
        allowuplinkaccess, propertyinfocase, personinfocase_personid, bobsource_sourceid,
        active
    )
    VALUES(
        DEFAULT, %(cecasepubliccc)s, %(property_propertyid)s, %(propertyunit_unitid)s,
        %(login_userid)s, %(casename)s, now(),
        now(), now(), %(notes)s, %(paccenabled)s,
        %(allowuplinkaccess)s, %(propertyinfocase)s, %(personinfocase_personid)s, %(bobsource_sourceid)s,
        %(active)s
    )
    RETURNING caseid"""
    cursor.execute(insert_sql, imap)
    return cursor.fetchone()[0]  # caseid


def person(record, cursor):
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


def taxstatus(tax_status, cursor):
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
    cursor.execute(
        insert_sql, tax_status._asdict()
    )  # Todo: For fun, learn speed of tuple -> dict
    return cursor.fetchone()[0]  # taxstatus_id


def propertyexternaldata(propextern_map, cursor):
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
