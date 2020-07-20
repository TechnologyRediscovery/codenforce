# Todo: Move functions from _update_muni to insert


def unit(imap, db_cursor):
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
    db_cursor.execute(insert_sql, imap)
    return db_cursor.fetchone()[0]  # unit_id


def cecase(imap, db_cursor):
    insert_sql = """INSERT INTO public.cecase(
        caseid, cecasepubliccc, property_propertyid, propertyunit_unitid,
        login_userid, casename, casephase, originationdate,
        closingdate, creationtimestamp, notes, paccenabled,
        allowuplinkaccess, propertyinfocase, personinfocase_personid, bobsource_sourceid,
        active
    )
    VALUES(
        DEFAULT, %(cecasepubliccc)s, %(property_propertyid)s, %(propertyunit_unitid)s,
        %(login_userid)s, %(casename)s, cast ('Closed' as casephase), now(),
        now(), now(), %(notes)s, %(paccenabled)s,
        %(allowuplinkaccess)s, %(propertyinfocase)s, %(personinfocase_personid)s, %(bobsource_sourceid)s,
        %(active)s
    )
    RETURNING caseid"""
    db_cursor.execute(insert_sql, imap)
    return db_cursor.fetchone()[0]  # caseid
