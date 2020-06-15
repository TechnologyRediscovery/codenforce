import csv
import random
import re
import time
import psycopg2
import itertools
import requests
import bs4
from unidecode import unidecode
import traceback


county_info_cache = {}
def get_county_taxpage_for(parcel_id):
    if parcel_id in county_info_cache:
        return county_info_cache[parcel_id]
    COUNTY_REAL_ESTATE_URL = ('http://www2.county.allegheny.pa.us/'
                              'RealEstate/Tax.aspx?')
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
    print(response.text)
    

# get_county_taxpage_for('0118B00020000000')


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

# text = get_county_page_for('0118B00020000000')

def scrape_county_assessed_value(rawhtml):
    ASSESSMENTYEAR_SPAN_ID = 'LCounty'
    ASSESSMENTLANDVALUE_SPAN_ID = 'lblCountyLand'
    ASSESSMENTBUILDINGVALUE_SPAN_ID = 'lblCountyBuild'
    returnmap = {}

    soup = bs4.BeautifulSoup(rawhtml, 'lxml')
    assyearfull = soup.find('span', id=ASSESSMENTYEAR_SPAN_ID).text
    assyear = int(re.split("\s", assyearfull)[0])
    print(assyear)
    
    returnmap['assessmentyear'] = assyear
    
    asslandvalfull = soup.find('span', id=ASSESSMENTLANDVALUE_SPAN_ID).text
    asslandval = asslandvalfull.replace(",", "").lstrip("$")
    print(asslandval) 
    returnmap['assessmentlandvalue'] = asslandval
    
    assbuildingvalfull = soup.find('span', id=ASSESSMENTBUILDINGVALUE_SPAN_ID).text
    assbuildingval = assbuildingvalfull.replace(",","").lstrip("$")
    print(assbuildingval)
    returnmap['assessmentbuildingvalue'] = assbuildingval
    print(returnmap)
    
    
def scrape():
    pass

scrape_county_assessed_value(get_county_page_for('1497H00012000000')) 


# In[23]:


db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        dbname="cogdblocal",
        user="sylvia",
        password="c0d3",
        host="localhost"
    )
    return db_conn



def check_for_cecase(propid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    casechecksql = """SELECT caseid FROM public.cecase WHERE property_propertyid = %(prop)s AND propertyinfocase = true;"""
    cursor.execute(casechecksql, dict(prop=propid))
    case = cursor.fetchall()
    print(case)
    if case == []:
        return False
    elif len(case) == 1:
        return case[0][0]
    else:
        caseids = []
        for id in case:
            caseids.append(id[0])
        return caseids
    

    
casecheck = check_for_cecase(10115)
if type(casecheck) == int:
    print("It's an int")
elif type(casecheck) == list:
    print(casecheck)
    print("list")
print()


# In[ ]:


#!/usr/bin/env python
# coding: utf-8

# In[128]:


import csv
import random
import re
import time
import psycopg2
import itertools
# import csv_utils
import requests
import bs4
from unidecode import unidecode
import traceback

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
    #PARID_FILE = 'parcelidlists/parcelidstest.csv'
    #PARID_FILE = 'parcelidlists/pitcairnparids_correct.csv'
    #PARID_FILE = 'parcelidlists/eastmckeesportparids.csv'
    #PARID_FILE = 'parcelidlists/wilkinsparcelids.csv'
    #PARID_FILE = 'parcelidlists/chalfantparcelids.csv'
    #PARID_FILE = 'parcelidlists/swissvaleparcelids.csv'
    PARID_FILE = ''
    
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
    muni_idbase_map = {'chalfant':10000,'churchhill':20000, 'eastmckeesport':30000, 'pitcairn':40000, 'wilmerding':0, 'wilkins':50000, 'cogland':60000, 'swissvale':170000}

    # add these base amounts to the universal base to get starting IDs
    global person_idbase_map
    person_idbase_map = {'chalfant':10000,'churchhill':20000, 'eastmckeesport':30000, 'pitcairn':40000, 'wilmerding':0, 'wilkins':50000, 'cogland':60000, 'swissvale':170000}

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


# In[129]:


db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        dbname="cogdb",
        user="sylvia",
        password="c0d3",
        host="localhost"
    )
    return db_conn



# In[130]:


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


# In[131]:


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
    owner_full_name = soup.find('span', id=OWNER_NAME_SPAN_ID)
    print('owner_raw_name:' + str(owner_full_name))
    # Remove extra spaces from owner's name
    full_owners = re.sub(r'\s+', ' ', owner_full_name.text.strip())
    persondict['name'] = full_owners
    persondict['raw'] = str(owner_full_name)

    
    #exp = re.compile(r'(\w+|[&])\s+(\w+|[&])\s*(\w*|[&]).*')
    #namegroups = re.search(exp, owner_full_name)
    #print(len(namegroups.groups()))
    #persondict['lname'] = str(namegroups.group(1)).title()
    
    #if len(namegroups.groups()) == 2:
        #persondict['fname'] = str(namegroups.group(2)).title()
    #elif len(namegroups.groups()) == 3:
        #persondict['fname'] = str(namegroups.group(2)).title() + ' ' + str(namegroups.group(3)).title()

    #print(str(persondict['fname']) + ' ' + str(persondict['lname']))
    
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
        print("Raising Exception in extract_propertyaddress")
        raise Exception
    
    street = re.sub('  ', ' ', adrlistraw[0])
    propaddrmap['street'] = str(unidecode(street)).strip()
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
    print(adrlistraw)
    # make sure we have all the parts of the address
    if len(adrlistraw) < 3:
        print("Raising Exception")
        raise Exception
    
    owneraddrmap['thirdline'] = None
    addrlistindex = 0
    if len(adrlistraw) > 3:
        print("Extra line address")
        thirdline = re.sub('  ', ' ', adrlistraw[addrlistindex])
        owneraddrmap['thirdline'] = str(unidecode(thirdline)).strip()
        addrlistindex += 2
        
    print("dgjalksdjgdsag")
    ownerstreet = re.sub('  ', ' ', adrlistraw[addrlistindex])
    owneraddrmap['street'] = str(unidecode(ownerstreet)).strip()
    print(owneraddrmap['street'])
    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile('[^,]*')
    addrlistindex += 2
    owneraddrmap['city'] = exp.search(adrlistraw[addrlistindex]).group()
    print("city:" + owneraddrmap['city'])
    # grap with string indexes (fragile if there is more than one space before zip)
    # TODO: use regexp
    exp=re.compile(r',\s*(\w\w)')
    m = re.search(exp,adrlistraw[addrlistindex])
    owneraddrmap['state'] = str(m.group(1))
    
    # abandoned string slicing approach (too brittle; use regexp)
    # owneraddrmap['state']= adrlistraw[2][-13:-11]
    print("state:" + owneraddrmap['state'])
    
    # owner zips could come in as: 15218 OR 15218- OR 15218-2342
    # just lose the routing numbers and take the first digits until the -
    exp=re.compile(r'\d+')
    m = re.search(exp, adrlistraw[addrlistindex])
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

def check_current_owner(parcelid):
    namemap = {}
    get_propid = "SELECT propertyid FROM public.property WHERE parid=%(parcelid)s;"
    inserts = {}
    inserts['parcelid'] = parcelid
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    cursor.execute(get_propid, inserts)
    propidlist = cursor.fetchall()
    propid = propidlist[0][0]
    get_personid = "SELECT person_personid FROM public.propertyperson WHERE property_propertyid=%(property_propertyid)s;"
    inserts['property_propertyid'] = propid
    cursor.execute(get_personid, inserts)
    personidlist = cursor.fetchall()
    personid = personidlist[0][0]
    get_names = "SELECT fname, lname FROM public.person WHERE personid=%(personid)s"
    inserts['personid'] = personid
    cursor.execute(get_names, inserts)
    names = cursor.fetchall()
    print(names)
    namemap['fname'] = names[0][0]
    namemap['lname'] = names[0][1]
    return namemap


# In[132]:


def extract_and_insert_person(rawhtml, propertyid, propinserts):
    # fixed values specific to keys in lookup tables
    

    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    notemsg = """In case of confusion, check autmated record entry with raw text from the county database:"""

    insert_sql = """
        INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18, humanverifiedby,compositelname, mailing_address_street, mailing_address_city,
            mailing_address_state, mailing_address_zip, useseparatemailingaddr, mailing_address_thirdline,
            ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, referenceperson, rawtext)
    VALUES (%(personid)s, cast ( 'ownercntylookup' as persontype), 
            %(muni_municode)s, NULL, %(lname)s, 'Property Owner', 
            NULL, NULL, NULL, NULL, %(address_street)s, %(address_city)s, 
            %(address_state)s, %(address_zip)s, %(notes)s, now(), NULL, TRUE, 
            FALSE, NULL, true, %(mailing_street)s, %(mailing_city)s, %(mailing_state)s, %(mailing_zip)s,
            %(mailingsameasres)s, %(thirdline)s, NULL, NULL, NULL, NULL, NULL, NULL, NULL, %(rawhtml)s);
    """

    insertmap = {}
    
    # load up vars for use in SQL from each of the parse methods
   
    insertmap['muni_municode'] = str(municodemap[current_muni])
    
    insertmap['mailing_street'] = None
    insertmap['mailing_city'] = None
    insertmap['mailing_state'] = None
    insertmap['mailing_zip'] = None
    insertmap['thirdline'] = None
    insertmap['mailingsameasres'] = 'true'

    try:
        ownername = extract_owner_name(rawhtml)
        insertmap['lname'] = ownername['name']
        insertmap['rawhtml'] = ownername['raw']
        print('extracted owner:' + insertmap['lname'] )
        print('extracted owner raw:' + insertmap['rawhtmls'] )


    except Exception:
        print("Malformed owner name at e and i person")
        print("ERROR malformed address at:" + str(propertyid))
    
    try:
        owneraddrmap = extract_owneraddress(rawhtml)
        print("ARE THEY THE SAME??? " + str(owneraddrmap['street'] == propinserts['street']) + owneraddrmap['street'] + propinserts['street'])
        print(propinserts)
        if owneraddrmap['street'] == propinserts['street']:
            print("SAME ADDRESS")
            insertmap['address_street'] = owneraddrmap['street']
            insertmap['address_city'] = owneraddrmap['city']
            insertmap['address_state'] = owneraddrmap['state']
            insertmap['address_zip'] = owneraddrmap['zipc']
            insertmap['notes'] = notemsg + ownername + " Raw Address: " + owneraddrmap['notes_adrdump']
        else:
            print("DIFFERENT MAILING")
            insertmap['address_street'] = None
            insertmap['address_city'] = None
            insertmap['address_state'] = None
            insertmap['address_zip'] = None
            insertmap['mailing_street'] = owneraddrmap['street']
            insertmap['mailing_city'] = owneraddrmap['city']
            insertmap['mailing_state'] = owneraddrmap['state']
            insertmap['mailing_zip'] = owneraddrmap['zipc']
            print(11)
            insertmap['thirdline'] = owneraddrmap['thirdline']
            print(22)
            insertmap['mailingsameasres'] = 'false'
            insertmap['notes'] = notemsg + ownername + " Raw address: " + owneraddrmap['notes_adrdump']
            print(insertmap['thirdline'])
            print(insertmap['notes'])

    except Exception:
        print("NO MAILING")
        insertmap['address_street'] = propinserts['street']
        insertmap['address_city'] = propinserts['city']
        insertmap['address_state'] = propinserts['state']
        insertmap['address_zip'] = propinserts['zipc']
        insertmap['notes'] = "Owner does not have mailing address"

        
    print(insertmap['mailing_zip'])
    if insertmap['address_street']:
        print('extracted owner address:' + ' '
        + insertmap['address_street'] + ' ' +
        insertmap['address_city'] + ', ' +
        insertmap['address_state']
        + ' ' + insertmap['address_zip'])
    if insertmap['mailing_street']:
        print('extracted mailing address:' 
        + ' ' + insertmap['mailing_street']
        + ' ' + insertmap['mailing_city']
        + ', ' + insertmap['mailing_state']
        + ' ' + insertmap['mailing_zip'])
    
    print('Inserting person data for id: %s' % (insertmap['personid']))
    print('person notes:' + insertmap['notes'])
    
    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print('----- committed person owner -----')
    # commit core propertytable insert



# In[133]:

def create_ce_event(caseid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    insertmap = {}
    insertsql = """INSERT INTO public.ceevent(
    eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, eventtimestamp,
    eventdescription, owner_userid, disclosetomunicipality, disclosetopublic, activeevent,
    requiresviewconfirmation, hidden, notes, responsetimestamp, actionrequestedby_userid,
    respondernotes, responderintended_userid, requestedeventcat_catid, requestedevent_eventid,
    rejeecteventrequest, responderactual_userid, responseevent_eventid, directrequesttodefaultmuniceo)
    VALUES (DEFAULT, 211, %(caseid)s, now(), now(), 'Update event from county', 99, TRUE, FALSE, FALSE, FALSE, FALSE, NULL,
    now(), NULL, NULL, NULL, NULL, NULL, FALSE, NULL, NULL, FALSE);"""
    insertmap['caseid'] = caseid
    cursor.execute(insertsql, insertmap)
    db_conn.commit()


def create_cecase(propid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    #selectPropCase = """SELECT caseid FROM public.cecase WHERE """


    insertsql = """INSERT INTO public.cecase(
    caseid, cecasepubliccc, property_propertyid, propertyunit_unitid,
    login_userid, casename, casephase, originationdate, closingdate,
    creationtimestamp, notes, paccenabled, allowuplinkaccess, propertyinfocase)
    VALUES (DEFAULT, 111111, %(propid)s, NULL, %(updater)s, %(casename)s, cast ('CountySiteImport' as casephase), now(),
    now(), now(), %(notes)s, FALSE, NULL, TRUE);"""

    imap = {}
    imap['propid'] = propid
    imap['casename'] = "Import from county site"
    imap['notes'] = "Initial case for each property"
    imap['updater'] = UPDATING_USER_ID
    cursor.execute(insertsql, imap)
    print("Executed")
    db_conn.commit()

def get_caseid(propid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    caseidsql = """SELECT caseid FROM public.cecase WHERE property_propertyid=%(prop)s AND propertyinfocase=TRUE"""
    cursor.execute(caseidsql, dict(prop=propid))
    props = cursor.fetchall()
    print(props)
    return props[0]


def insert_property_basetableinfo():
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    parcelgenerator = get_nextparcelid(PARID_FILE)
    propertyidgenerator = get_nextpropertyid(muni_idbase_map[current_muni])
    personidgenerator = get_nextpersonid(person_idbase_map[current_muni])

    cursor.close()
    cursor = db_conn.cursor()

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
            print("MA base table")
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
            #execute insert on property table
            cursor.execute(insert_sql, insertmap)
                # commit core propertytable insert
            db_conn.commit()
            propertycount = propertycount + 1
            print('----- committed property core table -----')
        except:
            print('ERROR: unable to insert base property data...skipping\n')
            print('********* MOVING ON ********************')
            logerror_aux(parid)
            continue
        try:
        # this try catches soup related errors
        # and sql errors bubbling up from the extraction methods that also commit
            personid = next(personidgenerator)
            #print(1)
            extract_and_insert_person(rawhtml, propid, personid, addrmap)
            #print(2)
            connect_person_to_property(propid, personid)
            #print(3)
            personcount = personcount + 1
        except:
            print('ERROR: unable to extract, commit, or connect person owner\n')
            logerror_aux(parid)
            continue
        try:
            # create standard unit number 0 for each property in system
            create_and_insert_unitzero(propid)
        except:
            print('ERROR: unable to create unit zero for property no.\n'+ str(propid))
            logerror(parid)
            continue
        try:
            create_cecase(propid)
            print("-------cecase inserted-------")
            caseid = get_caseid(propid)
            print("-----found case id-----")
            try:
                create_ce_event(caseid)
                print("-------ce event inserted-------")
            except:
                print("ERROR: unable to insert new ce event")
                break
        except:
            traceback.print_last()
            print("ERROR: unable to insert new cecase")
            break

        print('--------- running totals --------')
        print('Props inserted: ' + str(propertycount))
        print('Persons inserted: ' + str(personcount))
        print('********** DONE! *************\n')

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



# In[134]:


if __name__ == '__main__':
    main()


# In[ ]:

"""Add event for each update/ change in occupancy"""




