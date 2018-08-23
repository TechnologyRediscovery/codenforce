import itertools

import psycopg2

import csv_utils

TABLES_TO_LOAD = (
    # 'municipality',
    # 'owner',
    # 'propertyAgent',
    # 'codeofficer',
    'property',
    # 'codeEnfCase',
    # 'codeEnfEvent',
    # 'inspecChecklist',
)

CSV_FILE_ENCODING = 'utf-8'
CSV_DELIMITER = '|'


def main():
    num_of_tables_to_load = len(TABLES_TO_LOAD)
    print 'Starting the loading process for %d tables' % num_of_tables_to_load
    db_conn = get_db_conn()
    for i,table in enumerate(TABLES_TO_LOAD, start=1):
        print '[%d/%d] Loading table %s' % (i, num_of_tables_to_load, table)
        load_table(table)
    db_conn.commit()
    db_conn.close()


def load_table(table):
    TABLE_FUNCTION_MAPPING = {
        'municipality': import_municipality,
        'codeofficer': import_codeofficer,
        'owner': import_owner,
        'propertyAgent': import_propertyagent,
        'property': import_property,
        'codeEnfCase': import_codeenfcase,
        'codeEnfEvent': import_codeenfevent,
        'inspecChecklist': import_inspecchecklist
    }
    if table not in TABLE_FUNCTION_MAPPING:
        print "SKIPPING %s: Don't know how to load table." % table
        return
    # Generate the file using the right function
    TABLE_FUNCTION_MAPPING[table]()


def import_municipality():
    table_name = 'municipality'
    csv_file = 'municipality.csv'
    simple_csv_import(table_name, csv_file)


def import_owner():
    table_name = 'propertyOwner'
    csv_file = 'propertyOwner.csv'
    simple_csv_import(table_name, csv_file)


def import_property():
    table_name = 'property'
    csv_file = 'propTest.csv'
    simple_csv_import(table_name, csv_file)


def import_codeofficer():
    csv_file = 'codeOfficer.csv'

    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    # Using a hard-coded role description
    sql_command = """
        INSERT INTO codeofficer
            (officerid, firstname, lastname, roledescription)
        VALUES (%s, %s, %s, 'Code enforcement officer');
    """
    # Read CSV file with original Access Data
    with open(csv_file, 'r') as infile:
        reader = csv_utils.UnicodeReader(infile, delimiter=CSV_DELIMITER)
        # Ignore header
        reader.next()
        for row in reader:
            # Get code officer ID
            officer_id = row[0]
            # Split officer's name into first and last name
            first_name, last_name = row[1].split(' ')
            # Insert the data to the Postgres table
            cursor.execute(sql_command, (officer_id, first_name, last_name))


def import_propertyagent():
    table_name = 'propertyAgent'
    csv_file = 'propertyAgent.csv'
    simple_csv_import(table_name, csv_file)


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
            %(letterText)s, %(codeOfficer_officerID)s, %(codeEnfCase_caseID)s, 1)
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

db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        database="cogdb",
        user="sylvia",
        password="c0d3",
        host="localhost"
    )
    return db_conn


if __name__ == '__main__':
    main()

