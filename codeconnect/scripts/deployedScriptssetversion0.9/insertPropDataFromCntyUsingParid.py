#!/usr/bin/env python3

# encoding: utf-8

import csv
import random
import re
import time
import psycopg2
import itertools
# import csv_utils
import requests
import bs4

CSV_FILE_ENCODING = 'utf-8'
# global municodemap
# global current_muni
# global MUNI_PARCELID_LIST_FILE
# global muni_idbase_map
# global ID_BASE
# global TEST_PARID_FILE

def main():
    # for accessing a list of all parcelIDs by with one muni's list per file
    # global INPUT_FILE
    # INPUT_FILE = 'workspace/pitcairnparids.csv'

    # parcels whose base property data is not written are written here before moving on
    global LOG_FILE
    LOG_FILE = 'output/swissvaleerror.csv'

    # this log file is used for storing parcels whose person and propertyperson inserts fail
    global LOG_FILE_AUX
    LOG_FILE_AUX = 'output/swissvaleerror_aux.csv'

    # ID number of the system user connected to these original inserts
    # user 99 is sylvia, our COG robot
    global UPDATING_USER_ID
    UPDATING_USER_ID = 99

    global PARID_FILE
    # PARID_FILE = 'parcelidlists/wilmerdingparids.csv'
    # PARID_FILE = 'parcelidlists/parcelidstest.csv'
    # PARID_FILE = 'parcelidlists/pitcairnparids_correct.csv'
    # PARID_FILE = 'parcelidlists/eastmckeesportparids.csv'
    # PARID_FILE = 'parcelidlists/wilkinsparcelids.csv'
    # PARID_FILE = 'parcelidlists/chalfantparcelids.csv'
    PARID_FILE = 'parcelidlists/swissvaleparcelids.csv'
    
    # used as the access key for muni codes and ID bases in the dictionaries below
    global current_muni
    current_muni = 'swissvale'

    # use as floor value for all new propertyIDs
    global PROP_ID_BASE
    PROP_ID_BASE = 100000

    # floor value for new personIDs
    global PERSON_ID_BASE
    PERSON_ID_BASE = 10000

    # used for sliding starting ID up to accommodate a parcelid list adjustment due to errors on pesky parcels
    # must be manually set if there's an error: start one above the most recently issued ID
    # this gets added to the base ID in the ID range generation methods
    global BUMP_UP
    BUMP_UP = 0

    global municodemap
    municodemap = {'chalfant':814,'churchhill':816, 'eastmckeesport':821, 'pitcairn':847, 'wilmerding':867, 'wilkins':953, 'cogland':999, 'swissvale':111}

    # add these base amounts to the universal base to get starting IDs
    global muni_idbase_map
    muni_idbase_map = {'chalfant':10000,'churchhill':20000, 'eastmckeesport':30000, 'pitcairn':40000, 'wilmerding':0, 'wilkins':50000, 'cogland':60000, 'swissvale':70000}

    # add these base amounts to the universal base to get starting IDs
    global person_idbase_map
    person_idbase_map = {'chalfant':10000,'churchhill':20000, 'eastmckeesport':30000, 'pitcairn':40000, 'wilmerding':0, 'wilkins':50000, 'cogland':60000, 'swissvale':70000}

    # jump into the actual work here
    insert_property_basetableinfo()
    
    # Add owner data to properties
    # properties_with_owner_info = add_prop_info(properties)
    # Save new CSV file with property plus owner data
    # OUTPUT_FILE = 'output/testPropOut.txt'
    # save_properties_as_csv(properties_with_owner_info, OUTPUT_FILE)

def get_nextpropertyid(munioffset):
    # consider a range multiplier by municipality to generate starting 
    # at, say 110000 and the next at 120000 which allows for non-overlapping
    #ids for munis with a property count of up to 10000
    for i in list(range(PROP_ID_BASE + munioffset + BUMP_UP, PROP_ID_BASE + munioffset + 9999)):
        yield i;

def get_nextpersonid(munioffset):
    for i in list(range(PERSON_ID_BASE + munioffset + BUMP_UP, PERSON_ID_BASE + munioffset + 9999)):
        yield i;



def get_nextparcelid(input_file):
    # Read the CSV file and iterate through every property
    with open(input_file, 'r', encoding=CSV_FILE_ENCODING) as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            parid = str(row['parcelid']).strip()
            yield parid



def insert_property_basetableinfo():
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    parcelgenerator = get_nextparcelid(PARID_FILE)
    propertyidgenerator = get_nextpropertyid(muni_idbase_map[current_muni])
    personidgenerator = get_nextpersonid(person_idbase_map[current_muni])

    # user 99 is the cog robot, Sylvia
    insert_sql = """
        INSERT INTO public.property(
            propertyid, municipality_municode, parid, address, 
            notes, addr_city, addr_state, addr_zip,
            lotandblock, propclass, propertyusetype, ownercode,
            lastupdated, lastupdatedby)
        VALUES (%(propid)s, %(muni)s, %(parcelid)s, %(addr)s, 
                %(notes)s, %(city)s, %(state)s, %(zipcode)s,
                %(lotandblock)s, %(propclass)s, %(propertyusetype)s, %(ownercode)s,
                now(), %(updatinguser)s);

    """
    insertmap = {}
    propertycount = 0
    personcount = 0
    # the main delegator loop
    for parid in parcelgenerator:
        # go get raw HTML
        rawhtml = get_county_page_for(parid)
        
        # load up vars for use in SQL from each of the parse methods
        insertmap['propid'] = next(propertyidgenerator)
        propid = insertmap['propid']
        print("newid:"+ str(propid))
        # parid comes from the iterated item variable parid
        insertmap['parcelid'] = parid
        insertmap['muni'] = municodemap[current_muni]
        try:
            addrmap = extract_propertyaddress(parid, rawhtml)
        except Exception:
            print("ERR malformed address at:" + parid)
            logerror(parid)
            continue
        insertmap['addr'] = addrmap['street']
        insertmap['notes'] = 'core data scraped from county site'
        insertmap['city'] = addrmap['city']
        insertmap['state'] = addrmap['state']
        insertmap['zipcode'] = addrmap['zipc']   
        insertmap['lotandblock'] = extract_lotandblock_fromparid(parid)
        # print('lob:' + insertmap['lotandblock'])

        insertmap['propclass'] = str(extract_class(rawhtml))
        # print('class:' + insertmap['propclass'])

        insertmap['propertyusetype'] = extract_propertyusetype(rawhtml)
        # print('use:' + insertmap['propertyusetype'])

        insertmap['ownercode'] = extract_ownercode(rawhtml)
        # print('owner code:' + insertmap['ownercode'])

        insertmap['updatinguser'] = str(99)
        # print('updater id:' + insertmap['updatinguser'])

        print('Inserting parcelid data: %s' % (parid))
        
        try:
            # execute insert on property table
            cursor.execute(insert_sql, insertmap)
            # commit core propertytable insert
            db_conn.commit()
            propertycount = propertycount + 1
            print('----- committed property core table -----')
        except:
            print('ERROR: unable to insert base property data...skipping')
            print('********* MOVING ON ********************')
            logerror_aux(parid)
            continue
        try:
            # this try catches soup related errors
            # and sql errors bubbling up from the extraction methods that also commit
            personid = next(personidgenerator)
            extract_and_insert_person(rawhtml, propid, personid)
            connect_person_to_property(propid, personid )
            personcount = personcount + 1
        except:
            print('ERROR: unable to extract, commit, or connect person owner')
            logerror_aux(parid)
            continue
        try:
            # create standard unit number 0 for each property in system
            create_and_insert_unitzero(propid)
        except:
            print('ERROR: unable to create unit zero for property no.'+ str(propid))
            logerror(parid)
            continue
        print('--------- running totals --------')
        print('Props inserted: ' + str(propertycount))
        print('Persons inserted: ' + str(personcount))
        print('********** DONE! *************')

        # run insert with sql statement all loaded up
    cursor.close()
    db_conn.close()
    print('Count of properties inserted: ' + str(propertycount))
    print('Count of persons inserted: ' + str(personcount))

# db debugging--don't forget conn.commit!
    # newid = insertmap['id']
    #     selectsql = """
    #         SELECT * from property;
    #     """
    #     cursor.execute(selectsql)
    #     print(cursor.fetchone())

def extract_and_insert_person(rawhtml, propertyid, personid):
    # fixed values specific to keys in lookup tables


    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    notemsg = """In case of confusion, check autmated record entry with raw text from the county database:"""

    insert_sql = """
        INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18, humanverifiedby)
    VALUES (%(personid)s, cast ( 'ownercntylookup' as persontype), 
            %(muni_municode)s, %(fname)s, %(lname)s, 'Property Owner', 
            NULL, NULL, NULL, NULL, %(address_street)s, %(address_city)s, 
            %(address_state)s, %(address_zip)s, %(notes)s, now(), NULL, TRUE, 
            FALSE, NULL);
    """
    insertmap = {}
    
    # load up vars for use in SQL from each of the parse methods
    insertmap['personid'] = personid
    print("personid:"+str(insertmap['personid']))
    insertmap['muni_municode'] = str(municodemap[current_muni])

    try:
        ownernamemap = extract_owner_name(rawhtml)
        insertmap['fname'] = ownernamemap['fname']
        insertmap['lname'] = ownernamemap['lname']
        print('extracted owner:' + insertmap['fname'] + ' ' + insertmap['lname'] )

    except Exception:
        print("ERROR malformed address at:" + propertyid)

    owneraddrmap = extract_owneraddress(rawhtml)

    insertmap['address_street'] = owneraddrmap['street']
    insertmap['address_city'] = owneraddrmap['city']
    insertmap['address_state'] = owneraddrmap['state']
    insertmap['address_zip'] = owneraddrmap['zipc']   
    print('extracted owner address:' \
        + ' ' + insertmap['address_street'] \
        + ' ' + insertmap['address_city'] \
        + ', ' + insertmap['address_state'] \
        + ' ' + insertmap['address_zip'])

    insertmap['notes'] = notemsg + ownernamemap['note_namedump'] + " Raw address: " + owneraddrmap['notes_adrdump']
    print('Inserting person data for id: %s' % (insertmap['personid']))
    print('person notes:' + insertmap['notes'])
    
    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print('----- committed person owner -----')
    # commit core propertytable insert

def connect_person_to_property(propertyid, personid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    insert_sql = """
        INSERT INTO public.propertyperson(
            property_propertyid, person_personid)
    VALUES (%(prop)s, %(pers)s);
    """
    insertmap = {}
    
    # load up vars for use in SQL from each of the parse methods
    insertmap['prop'] = propertyid
    insertmap['pers'] = personid

    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print('----- connected person owner to property -----')

def create_and_insert_unitzero(propertyid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    
    insert_sql = """
        INSERT INTO public.propertyunit(
            unitid, unitnumber, property_propertyid, otherknownaddress, notes, 
            rental)
        VALUES (DEFAULT, '0', %(property_propertyid)s, NULL, 
            'robot-generated unit representing the primary habitable dwelling on a property', 
            FALSE);
    """
    insertmap = {}
    
    # load up vars for use in SQL from each of the parse methods
    insertmap['property_propertyid'] = propertyid

    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print('----- built unit zero -----')


# deprecated from Daniel--kept in as a reference
def add_prop_info(properties):
    PARCEL_ID_FIELD = 'parcelid'
    OWNER_NAME_FIELD = 'OwnerName'
    OWNER_ADDRESS_FIELD = 'OwnerAddress'
    for prop in properties:
        assert prop[PARCEL_ID_FIELD]  # Every property must have a parcel id
        parcel_id = prop[PARCEL_ID_FIELD]
        print('Processing parcel', parcel_id)

        # Get the html from the county's website
        property_html = get_county_page_for(parcel_id)

        # Wait between request, just to be nice with the county's site
        sleep_time = random.random() * 3  # Sleep at most 3 seconds
        time.sleep(sleep_time)
        print('sleeping for {:.2f} seconds'.format(sleep_time))

        # Add owner name and address to the property
        prop[OWNER_NAME_FIELD] = extract_owner_name(parcel_id, property_html)
        prop[OWNER_ADDRESS_FIELD] = extract_owner_address(
                parcel_id, property_html)
        print(prop[OWNER_NAME_FIELD], prop[OWNER_ADDRESS_FIELD])

        yield prop


county_info_cache = {}
def get_county_page_for(parcel_id):
    if parcel_id in county_info_cache:
        return county_info_cache[parcel_id]
    COUNTY_REAL_ESTATE_URL = ('http://www2.county.allegheny.pa.us/'
                              'RealEstate/GeneralInfo.aspx?')
    search_parameters = {
        'ParcelID': parcel_id,
        'SearchType': 3,
        'SearchParcel': parcel_id}
    waittime = random.uniform(0.0,1.0)
    print("waiting:" + str(waittime))
    time.sleep(waittime)
    try:
        response = requests.get(
                COUNTY_REAL_ESTATE_URL,
                params=search_parameters,
                timeout=5)
        print('Scraping data from county: %s' + parcel_id)
    except requests.exceptions.Timeout:
        # Wait 10 secs and try one more time
        time.sleep(10)
        response = requests.get(
                COUNTY_REAL_ESTATE_URL,
                params=search_parameters,
                timeout=5)
    county_info_cache[parcel_id] = response.text
    return response.text

#---------------------------------------------
#           HTML SCRAPING METHODS
#---------------------------------------------

def extract_lotandblock_fromparid(parid):
    trimmedparid = parid[0:11]
    exp = re.compile(r'([1-9]+)(\w)[0]*([1-9]+)')
    gl = re.search(exp, trimmedparid)
    if(gl):
        lob = gl.group(1) + '-' + gl.group(2) + '-' + gl.group(3)
        return lob
    else:
        print('ERROR: LOB parsing')
        return ''

def extract_owner_name(property_html):
    OWNER_NAME_SPAN_ID = 'BasicInfo1_lblOwner'
    persondict = {}

    soup = bs4.BeautifulSoup(property_html, 'lxml')
    owner_full_name = soup.find('span', id=OWNER_NAME_SPAN_ID).text
    print('owner_raw_name:' + str(owner_full_name))
    # Remove extra spaces from owner's name
    persondict['note_namedump'] = re.sub(r'\s+', ' ', owner_full_name.strip())
    
    exp = re.compile(r'(\w+|[&])\s+(\w+|[&])\s*(\w*|[&]).*')
    namegroups = re.search(exp, owner_full_name)
    # print(len(namegroups.groups()))
    persondict['lname'] = str(namegroups.group(1)).title()
    
    if len(namegroups.groups()) == 2:
        persondict['fname'] = str(namegroups.group(2)).title()
    elif len(namegroups.groups()) == 3:
        persondict['fname'] = str(namegroups.group(2)).title() + ' ' + str(namegroups.group(3)).title()

    print(str(persondict['fname']) + ' ' + str(persondict['lname']))
    
    return persondict

def extract_propertyaddress(parcel_id, property_html):
    propaddrmap = {}
    PROP_ADDR_SPAN_ID = 'BasicInfo1_lblAddress'
    # print(property_html)
    soup = bs4.BeautifulSoup(property_html, 'lxml')
    
    # this yeilds something like
    # 471&nbsp;WALNUT  ST<br>PITTSBURGH,&nbsp;PA&nbsp;15238
    prop_addr_raw = soup.find('span', id=PROP_ADDR_SPAN_ID).text
    scrapedhtml = soup.select("#BasicInfo1_lblAddress")

    print("Scraped:" + str(scrapedhtml))
    soup = bs4.BeautifulSoup(str(scrapedhtml), 'lxml')
    adrlistraw = soup.span.contents 
    # make sure we have all the parts of the address
    if len(adrlistraw) < 3:
        raise Exception

    propaddrmap['street'] = re.sub('  ', ' ', adrlistraw[0])
    print(propaddrmap['street'])
    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile('[^,]*')
    propaddrmap['city'] = exp.search(adrlistraw[2]).group()
    print("city:" + propaddrmap['city'])
    # hard-code pa
    propaddrmap['state']= 'PA'
    # zip is just the last 5 chars    
    propaddrmap['zipc'] = adrlistraw[2][-5:]
    print("Zip:" + propaddrmap['zipc'])

    return propaddrmap

def extract_owneraddress(property_html):
    print('--------- extracting owner address ------------')
    owneraddrmap = {}
    # print(property_html)
    soup = bs4.BeautifulSoup(property_html, 'lxml')
    # this yeilds something like
    # 471&nbsp;WALNUT  ST<br>PITTSBURGH,&nbsp;PA&nbsp;15238
    scrapedhtml = soup.select("#lblChangeMail")
    owneraddrmap['notes_adrdump'] = str(scrapedhtml)

    print("Scraped:" + str(scrapedhtml))
    soup = bs4.BeautifulSoup(str(scrapedhtml), 'lxml')
    # this spits out a three-item list. We throw away the <br> which is 
    # the middle item. The first is the street, the second gets chopped up
    adrlistraw = soup.span.contents 
    # make sure we have all the parts of the address
    if len(adrlistraw) < 3:
        raise Exception

    owneraddrmap['street'] = re.sub('  ', ' ', adrlistraw[0])
    print(owneraddrmap['street'])
    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile('[^,]*')
    owneraddrmap['city'] = exp.search(adrlistraw[2]).group()
    print("city:" + owneraddrmap['city'])
    # grap with string indexes (fragile if there is more than one space before zip)
    # TODO: use regexp
    exp=re.compile(r',\s*(\w\w)')
    m = re.search(exp,adrlistraw[2])
    owneraddrmap['state'] = str(m.group(1))
    
    # abandoned string slicing approach (too brittle; use regexp)
    # owneraddrmap['state']= adrlistraw[2][-13:-11]
    print("state:" + owneraddrmap['state'])
    
    # owner zips could come in as: 15218 OR 15218- OR 15218-2342
    # just lose the routing numbers and take the first digits until the -
    exp=re.compile(r'\d+')
    m = re.search(exp, adrlistraw[2])
    owneraddrmap['zipc'] = str(m.group())

    # another abandoned string slicing approach: 
    # also too brittle given range of scraped inputs
    # owneraddrmap['zip'] = adrlistraw[2][-10:]
    print("zip:" + owneraddrmap['zipc'])

    return owneraddrmap


def extact_owner_name_and_mailing(parcel_id, property_html):

    OWNER_ADDRESS_SPAN_ID = 'lblChangeMail'
    addrparts = {}

    soup = bs4.BeautifulSoup(property_html, 'lxml')
    owner_address_raw = soup.find('span', id=OWNER_ADDRESS_SPAN_ID).text
    # Remove extra spaces
    addrparts['addr'] = rs.sub('')

    owner_address = re.sub(r'\s+', ' ', owner_address.strip())
    # Remove leading spaces before commas
    owner_address = re.sub(r'\s+,', ',', owner_address)

    return owner_address


def extract_propertyusetype(property_html):
    USE_TYPE_SPAN_ID = 'lblUse'

    soup = bs4.BeautifulSoup(property_html, 'lxml')
    usetype = soup.find('span', id=USE_TYPE_SPAN_ID).text
    # Remove extra spaces
    usetype = re.sub(r'\s+', ' ', usetype.strip())
    # Remove leading spaces before commas
    usetype = str(re.sub(r'\s+,', ',', usetype)).title()
    return usetype

def extract_class(property_html):
    CLASS_SPAN_ID = 'lblState'

    soup = bs4.BeautifulSoup(property_html, 'lxml')
    propclass = soup.find('span', id=CLASS_SPAN_ID).text
    # Remove extra spaces
    propclass = re.sub(r'\s+', ' ', propclass.strip())
    # Remove leading spaces before commas
    propclass = str(re.sub(r'\s+,', ',', propclass)).title()
    return propclass

def extract_ownercode(property_html):
    OWNER_CODE_SPAN_ID = 'lblOwnerCode'

    soup = bs4.BeautifulSoup(property_html, 'lxml')
    ownercode = soup.find('span', id=OWNER_CODE_SPAN_ID).text
    # Remove extra spaces
    ownercode = re.sub(r'\s+', ' ', ownercode.strip())
    # Remove leading spaces before commas
    ownercode = str(re.sub(r'\s+,', ',', ownercode)).title()
    return ownercode




# def extract_owner_address(parcel_id, property_html):
#     OWNER_ADDRESS_SPAN_ID = 'lblChangeMail'
#     soup = bs4.BeautifulSoup(property_html)
#     owner_address = soup.find('span', id=OWNER_ADDRESS_SPAN_ID).text
#     # Remove extra spaces
#     owner_address = re.sub(r'\s+', ' ', owner_address.strip())
#     # Remove leading spaces before commas
#     owner_address = re.sub(r'\s+,', ',', owner_address)
#     return owner_address

def logerror(parcelid):
    with open(LOG_FILE, 'a', encoding=CSV_FILE_ENCODING) as outfile:
        writer = csv.writer(outfile)
        writer.writerow([parcelid])

def logerror_aux(parcelid):
    with open(LOG_FILE_AUX, 'a', encoding=CSV_FILE_ENCODING) as outfile:
        writer = csv.writer(outfile)
        writer.writerow([parcelid])

# for debugging and transferability purposes
def save_properties_as_csv(properties_with_owner_info, output_file):
    fieldnames = None
    with open(output_file, 'w', encoding=CSV_FILE_ENCODING) as outfile:
        writer = csv.writer(outfile)
        for prop in properties_with_owner_info:
            if fieldnames is None:
                fieldnames = sorted(prop.keys())
                writer.writerow(fieldnames)
            writer.writerow([prop[k] for k in fieldnames])




# def store_owner_as_person(parid):
#     db_conn = get_db_conn()
#     cursor = db_conn.cursor()
#     # Using hard-coded event type ID: 18, for code enforcement letter type
#     sql_command = """

#         INSERT INTO public.person(
#             personid, persontype, muni_municode, fname, lname, jobtitle, 
#             phonecell, phonehome, phonework, email, address_street, address_city, 
#             address_state, address_zip, notes, lastupdated, expirydate, isactive, 
#             isunder18, "humanVerifiedby")
#         VALUES (DEFAULT, CAST( 'ownercntylookup' AS persontype), %s, ?, ?, ?, 
#                 ?, ?, ?, ?, ?, ?, 
#                 ?, ?, ?, ?, ?, ?, 
#                 ?, ?);

#         INSERT INTO codeenfevent
#             (eventID, eventDate, eventDescription, letterText,
#             codeOfficer_officerID, codeEnfCase_caseID, EventTyp_codeEnfEventTypeID)
#         VALUES (%(eventID)s, %(eventDate)s, %(eventDescription)s,
#             %(letterText)s, %(codeOfficer_officerID)s, %(codeEnfCase_caseID)s, 18)
#     """
#     # Read CSV file with original Access Data
#     with open(csv_file, 'r') as infile:
#         reader = csv_utils.UnicodeReader(infile, delimiter=CSV_DELIMITER)
#         # Get header
#         header = reader.next()
#         # Sequence of eventid
#         get_event_id = itertools.count(start=1)
#         for row in reader:
#             # Build a record dict from row and header
#             record = dict(zip(header, row))
#             # Assign eventid
#             record['eventID'] = get_event_id.next()
#             # Insert the data to the Postgres table
#             cursor.execute(sql_command, record)


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
