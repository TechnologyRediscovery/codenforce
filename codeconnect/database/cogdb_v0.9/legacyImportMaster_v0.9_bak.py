#!/usr/bin/env python2

import itertools
import psycopg2
import csv_utils
import re
import csv
import codecs

    # 'code_violation',
    # 'owner',
    # 'codeofficer',
    # 'property',
    # 'codeEnfCase',
    # 'codeEnfEvent',
    # 'inspecChecklist',

TABLES_TO_LOAD = (
    'propertyAgent',
)

CSV_FILE_ENCODING = 'utf-8'
CSV_DELIMITER = '|'
GEN_LOG_FILE = 'output/genlog.txt'
# oldID:newID
OFFICERID_MAP = {
    
}


def main():
    global VIOLATION_LOG_FILE
    VIOLATION_LOG_FILE = 'output/violationerrors.txt'
    num_of_tables_to_load = len(TABLES_TO_LOAD)
    print 'TEST Starting the loading process for %d tables' % num_of_tables_to_load
    db_conn = get_db_conn()
    for i,table in enumerate(TABLES_TO_LOAD, start=1):
        print '[%d/%d] Loading table %s' % (i, num_of_tables_to_load, table)
        load_table(table)
    db_conn.commit()
    db_conn.close()

def test_propid_lookup():


def load_table(table):
    TABLE_FUNCTION_MAPPING = {
        'owner': import_owner,
        'propertyAgent': import_propertyagent,
        'property': import_property,
        'codeEnfCase': import_codeenfcase,
        'codeEnfEvent': import_codeenfevent,
        'inspecChecklist': import_inspecchecklist,
        'codeElement':import_code_elements,
    }
    if table not in TABLE_FUNCTION_MAPPING:
        print "SKIPPING %s: Don't know how to load table." % table
        return
    # Generate the file using the right function
    TABLE_FUNCTION_MAPPING[table]()

def fetch_pgpropertyid(parcelid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    # Using hard-coded event type ID: 18, for code enforcement letter type
    sql_command = """
        SELECT propertyid FROM property WHERE parid = %s;
    """
    cursor.execute(sql_command, parcelid)
    try:
        pgpropid = cursor.fetchone()
    except Exception:
        print('Unable to find property in PG with parcel id: %s' %(parcelid))
        loggeneralerror('unable to locate PG table for property with parcel id: %s' %(parcelid))
        raise Exception

def connect_person_to_property(propertyid, personid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    # Using hard-coded event type ID: 18, for code enforcement letter type
    sql_command = """
        INSERT INTO public.propertyperson(
            property_propertyid, person_personid)
        VALUES (%s, %s);
    """
    cursor.execute(sql_command, (propertyid, personid))

def loggeneralerror(msg):
    with open(GEN_LOG_FILE, 'ab') as outfile:
        writer = csv_utils.UnicodeWriter(outfile, delimiter="|", encoding=CSV_FILE_ENCODING)
        writer.writerow(msg)


def logerrorviolation(malformedrow):
    with open(VIOLATION_LOG_FILE, 'ab') as outfile:
        writer = csv_utils.UnicodeWriter(outfile, delimiter="|", encoding=CSV_FILE_ENCODING)
        writer.writerow(malformedrow)



def import_owner():
    table_name = 'propertyOwner'
    csv_file = 'propertyOwner.csv'
    simple_csv_import(table_name, csv_file)
 

def import_property():
    table_name = 'property'
    csv_file = 'property.csv'
    simple_csv_import(table_name, csv_file)


def import_propertyagent():
    table_name = 'propertyAgent'
    csv_file = 'propertyAgent_test.csv'
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    # Using hard-coded event type ID: 18, for code enforcement letter type
    sql_command = """
        INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18, humanverifiedby)
        VALUES (%(personid)s, CAST('LegacyAgent' AS persontype), %(municode)s, %(fname)s, %(lname)s, NULL, 
                NULL, NULL, %(phonework)s, %(email)s, %(address_street)s, %(address_city)s, 
                %(address_state)s, %(address_zip)s, %(notes)s, now(), NULL, TRUE, 
                FALSE, NULL);

    """
    # Read CSV file with original Access Data
    with open(csv_file, 'r') as infile:
        reader = csv_utils.UnicodeReader(infile, delimiter=CSV_DELIMITER)
        # Get header
        header = reader.next()
        # Sequence of eventid
        get_personid = itertools.count(start=200)
        for row in reader:
            # Build a record dict from row and header
            record = dict(zip(header, row))
            if record['fname'] == None:
                record['fname'] == 'none'
            if record['lname'] == None:
                record['lname'] == 'none'
            record['personid'] = get_personid.next()
            record['notes'] = 'person created from legacy table tblAgent with origin agentid of %s connected to property id %s' \
            % (record['agentid'], record['propertyid'])
            cursor.execute(sql_command, record)
            try:
                pgprop = fetch_pgpropertyid(record['parcelid'])
            except Exception:
                print('skipping person linking for %s' % (record['fname']))
                continue
            connect_person_to_property(pgprop, record['personid']) 

def import_codeenfcase():
    table_name = 'codeenfcase'
    csv_file = 'codeEnfCase.csv'
    simple_csv_import(table_name, csv_file)



def import_codeenfevent():
    csv_file = 'codeEnfEvent.csv'


    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    # Using hard-coded event type ID: 18, for code enforcement letter type
    sql_command = """
        INSERT INTO codeenfevent
            (eventID, eventDate, eventDescription, letterText,
            codeOfficer_officerID, codeEnfCase_caseID, EventTyp_codeEnfEventTypeID)
        VALUES (%(eventID)s, %(eventDate)s, %(eventDescription)s,
            %(letterText)s, %(codeOfficer_officerID)s, %(codeEnfCase_caseID)s, 18)
    """
    # Read CSV file with original Access Data
    with open(csv_file, 'r') as infile:
        reader = csv_utils.UnicodeReader(infile, delimiter=CSV_DELIMITER)
        # Get header
        header = reader.next()
        # Sequence of eventid
        get_event_id = itertools.count(start=1)
        for row in reader:
            # Build a record dict from row and header
            record = dict(zip(header, row))
            # Assign eventid
            record['eventID'] = get_event_id.next()
            # Insert the data to the Postgres table
            cursor.execute(sql_command, record)


def import_inspecchecklist():
    table_name = 'inspecchecklist'
    csv_file = 'inspecChecklist.csv'
    simple_csv_import(table_name, csv_file)


def simple_csv_import(table_name, csv_file):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    with open(csv_file, 'rb') as infile:
        header = infile.readline().strip().split(CSV_DELIMITER)
        cursor.copy_from(
            infile, table_name, sep=CSV_DELIMITER, null='None', columns=header)

def import_code_elements():
    print 'importing code violations'
    csv_file = 'code_violation_cleaned.csv'
    # csv_file = 'testviolations.csv' 
    #816 is churchill
    currentmuni = 953
    codesource = 26
    defaultch = 0
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    # Using a hard-coded role description
    sql_command = """
        INSERT INTO public.codeelement(
        tb
            elementid, codesource_sourceid, ordchapterno, ordchaptertitle, 
            ordsecnum, ordsectitle, ordsubsecnum, ordsubsectitle, ordtechnicaltext, 
            ordhumanfriendlytext, isactive, resourceurl, datecreated, guideentryid, notes, legacyid)
    VALUES (DEFAULT, %(codesource_sourceid)s, %(ordchapterno)s, NULL, 
            %(ordsecnum)s, NULL, %(ordsubsecnum)s, %(ordsubsectitle)s, %(ordtechnicaltext)s, 
            NULL, CAST (%(isactive)s AS boolean), NULL, now(), NULL, %(notes)s, CAST(%(legacyid)s AS integer));

    """
    rowdict = {}
    # Read CSV file with original Access Data
    with open(csv_file, 'r') as infile:
        reader = csv_utils.UnicodeReader(infile, delimiter=CSV_DELIMITER)
        # Ignore header
        reader.next()
        for row in reader:
            muni = int(row[1])
            if muni == currentmuni:
                print '****************'
                print row[0]
                # rowdict['elementid'] = int(row[0]) + 1000
                rowdict['codesource_sourceid'] = codesource
                rowdict['ordchapterno'] = defaultch
                rawcodenum = row[2]
                exp = re.compile(r'(\d+)[\.-](\d+[\.-]?\w*[\.]?)\s+(.*)')
                # this exp is specific for separating out churchill specific ordinances which only use a - delimiter
                # exp = re.compile(r'(\d+).(\d+[\.-]?\w*[\.]?)\s+(.*)')
                m = re.search(exp,rawcodenum)
                if m:
                    rowdict['ordsecnum'] = m.group(1)
                    print rowdict['ordsecnum']
                    rowdict['ordsubsecnum'] = m.group(2)
                    print rowdict['ordsubsecnum']
                    rowdict['ordsubsectitle'] = m.group(3)
                    print rowdict['ordsubsectitle'] 
                    rowdict['ordtechnicaltext'] = row[3]
                    rowdict['isactive'] = row[4].upper()
                    rowdict['notes'] = 'pulled from legacy system on 16JUL18'
                    rowdict['legacyid'] = row[0]

                    # Insert the data to the Postgres table
                    cursor.execute(sql_command, rowdict)
                    db_conn.commit()
                else:
                    logerrorviolation(row)
                    print "no match"
                    rowdict['ordsecnum'] = 'NULL'
                    rowdict['ordsubsecnum'] = 'NULL'
                    rowdict['ordsubsectitle'] = 'NULL'

db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        database="cogdb",
        user="sylvia",
        password="c0d3",
        host="localhost",
        port=5432
    )
    return db_conn


if __name__ == '__main__':
    main()

