"""
This script is run whenever a new municipality is added to the council of government.
Modify the globals current_muni and municodemap and the script should do the rest.

Todo: Add validation that the database does not contain property from the municode we attempt to insert
"""

import csv
import re
import time
import psycopg2
import requests
import bs4
import logging
from os.path import join

from Exceptions._exceptions import MalformedDataError, MalformedGenericAddressError, MalformedOwnerError, \
    MalformedZipcodeError, MalformedStateError, MalformedLotAndBlockError


def main():
    globals_setup()
    logger_setup()
    get_db_conn() #Sets up global connection️
    insert_property_basetableinfo()


def globals_setup():
    """
    Refactored legacy code where global variables are set.
    db_conn and cursor are also globals, but are initalized in get_db_conn
    TODO: Global variables are unpythonic. Consider more refactoring
    """
    global CSV_FILE_ENCODING
    CSV_FILE_ENCODING = "utf-8"
    global current_muni
    current_muni = "foresthills"

    global logger  # logger details set in logger_setup()
    global LOG_FILE
    LOG_FILE = join("output", current_muni + "_error.log")

    # ID number of the system user connected to these original inserts
    # user 99 is sylvia, our COG robot
    global UPDATING_USER_ID
    UPDATING_USER_ID = 99

    global PARID_FILE
    PARID_FILE = join("parcelidlists", current_muni + "_parcelids.csv")

    global municodemap  # In a perfect world, this would be a single id returned from the database
    municodemap = {
        "chalfant": 814,
        "churchhill": 816,
        "eastmckeesport": 821,
        "pitcairn": 847,
        "wilmerding": 867,
        "wilkins": 953,
        "cogland": 999,
        "swissvale": 111,
        "foresthills": 828,
    }


def logger_setup():
    logging.basicConfig(
        handlers=[logging.FileHandler(LOG_FILE), logging.StreamHandler()],
        level=logging.WARNING,
    )
    global logger
    logger = logging.getLogger(__name__)
    logger.info("Logger initialized")


# def get_nextpropertyid(munioffset):
#     # consider a range multiplier by municipality to generate starting
#     # at, say 110000 and the next at 120000 which allows for non-overlapping
#     # ids for munis with a property count of up to 10000
#     for i in list(
#         range(PROP_ID_BASE + munioffset + BUMP_UP, PROP_ID_BASE + munioffset + 9999)
#     ):
#         yield i
#
#
# def get_nextpersonid(munioffset):
#     for i in list(
#         range(PERSON_ID_BASE + munioffset + BUMP_UP, PERSON_ID_BASE + munioffset + 9999)
#     ):
#         yield i


def get_nextparcelid(input_file):
    # Read the CSV file and iterate through every property
    with open(input_file, "r", encoding=CSV_FILE_ENCODING) as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            parid = str(row["parcelid"]).strip()
            yield parid


def insert_property_basetableinfo():
    with db_conn:
        with cursor:
            parcelgenerator = get_nextparcelid(PARID_FILE)
            # Todo: Add properly formatted SQL for tables that have moved. For example, propertyusetype

            # Todo: Refactor property insertion into its own function

            propertycount = 0
            personcount = 0
            # the main delegator loop
            for parid in parcelgenerator:
                rawhtml = get_county_page_for(parid)
                property_id = extract_and_insert_property(parid, rawhtml)
                create_ce_case(property_id) #Todo: Add custom error
                propertycount += 1

                # # Todo: Should try/except be on the inside of the function like in extract_and_insert_property?
                try:
                    person_id = extract_and_insert_person(rawhtml)
                    connect_person_to_property(property_id, person_id)
                    personcount += 1
                except MalformedDataError as e:
                    log_error(e, parid)

                create_and_insert_default_unit(property_id, default_unit=-1)

                # except MalformedDataError:
                #     logger.warning(
                #         "This code SHOULD be unreachable. If this is in your error logs, "
                #         "update insertPropDataFromCntyUsingParid.py to catch the try/except earlier."
                #     )
                # print("--------- running totals --------")
                print("Props inserted: " + str(propertycount))
                print("Persons inserted: " + str(personcount))
                print("********** DONE! *************")

            print("Total properties inserted: " + str(propertycount))
            print("Total persons inserted: " + str(personcount))


        # db debugging--don't forget conn.commit!
        # newid = insertmap['id']
        #     selectsql = """
        #         SELECT * from property;
        #     """
        #     cursor.execute(selectsql)
        #     print(cursor.fetchone())


def extract_and_insert_property(parid, rawhtml):
    """
    Parses the RawHTML and updates commits a property to the database.
    Returns the property_id read from the database
    """
    insert_sql = """    
        INSERT INTO public.property(
            municipality_municode, parid, address, 
            notes, addr_city, addr_state, addr_zip,
            lotandblock, propclass, ownercode,
            lastupdated, lastupdatedby)
        VALUES (%(muni)s, %(parcelid)s, %(addr)s, 
                %(notes)s, %(city)s, %(state)s, %(zipcode)s,
                %(lotandblock)s, %(propclass)s, %(ownercode)s,
                now(), %(updatinguser)s)
        RETURNING propertyid;

    """
    insertmap = {}
    # load up vars for use in SQL from each of the parse methods
    # propid = insertmap["propid"]
    # print("newid:" + str(propid))
    # parid comes from the iterated item variable parid
    insertmap["parcelid"] = parid
    insertmap["muni"] = municodemap[current_muni]

    try:
        addrmap = extract_propertyaddress(
            rawhtml
        )
        insertmap["addr"] = addrmap["street"]
        insertmap["notes"] = "Core data scraped from county site"
        insertmap["city"] = addrmap["city"]
        insertmap["state"] = addrmap["state"]
        insertmap["zipcode"] = addrmap["zipc"]
    except MalformedGenericAddressError as e:
        log_error(e, parid)
        insertmap["addr"] = ""
        insertmap["notes"] = "Error when extracting the address"
        insertmap["city"] = ""
        insertmap["state"] = ""
        insertmap["zipcode"] = ""

    try:
        insertmap["lotandblock"] = extract_lotandblock_fromparid(parid)
    except MalformedLotAndBlockError as e:
        log_error(e, parid)
        insertmap["lotandblock"] = ''

    # None of these methods SHOULD throw an error. If they did, I don't know what the error would be
    insertmap["propclass"] = str(extract_class(rawhtml))
    insertmap["propertyusetype"] = extract_propertyusetype(rawhtml)
    insertmap["ownercode"] = extract_ownercode(rawhtml)
    insertmap["updatinguser"] = str(99)     # user 99 is the cog robot, Sylvia

    print("Inserting parcelid data: %s" % (parid))
    cursor.execute(insert_sql, insertmap)

    # commit core propertytable insert
    db_conn.commit()
    return cursor.fetchone()[0] # Returns the property_id



def create_ce_case(property_id):
    """
    Inserts a new code enforcement case into the cecase table.
    """
    insert_sql = """INSERT INTO public.cecase(
        caseid, cecasepubliccc, property_propertyid, propertyunit_unitid,
        login_userid, casename, casephase, originationdate,
        closingdate, creationtimestamp, notes, paccenabled,
        allowuplinkaccess, propertyinfocase, personinfocase_personid, bobsource_sourceid,
        active
    )
    VALUES(
        DEFAULT, 111111, %(propid)s, NULL,
        %(updater)s, %(casename)s, cast ('Closed' as casephase), now(),
        now(), now(), %(notes)s, FALSE,
        NULL, TRUE, NULL, NULL,
        TRUE
    )
    """
    # Note: casephase is being deprecated
    # TODO: CHECK DEFAULT VALUES WITH ERIC. Should active be true?
    insertmap = {}
    insertmap['propid'] = property_id
    insertmap['updater'] = UPDATING_USER_ID
    insertmap['casename'] = "Import from county site"
    # insertmap['casephase'] = 'CountySiteImport'
    insertmap['notes'] = "Initial case for each property"
    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print("---- Created CE Case ----")


def extract_and_insert_person(rawhtml):

    # fixed values specific to keys in lookup tables
    notemsg = """In case of confusion, check automated record entry with raw text from the county database: """
    insert_sql = """
        INSERT INTO public.person(
            persontype, muni_municode, fname, lname, 
            jobtitle, phonecell, phonehome, phonework, 
            email, address_street, address_city, address_state, 
            address_zip, notes, lastupdated, expirydate, 
            isactive, isunder18, humanverifiedby)
    VALUES (cast ( 'ownercntylookup' as persontype), %(muni_municode)s, %(fname)s, %(lname)s, 
            'Property Owner', NULL, NULL, NULL, 
            NULL, %(address_street)s, %(address_city)s, %(address_state)s, 
            %(address_zip)s, %(notes)s, now(), NULL, 
            TRUE, FALSE, NULL)
            RETURNING personid;
    """
    insertmap = {}

    # load up vars for use in SQL from each of the parse methods
    insertmap["muni_municode"] = str(municodemap[current_muni])

    ownernamemap = extract_owner_name(rawhtml)  # Potentially raises MalformedOwnerError
    insertmap["fname"] = ownernamemap["fname"]
    insertmap["lname"] = ownernamemap["lname"]
    print("extracted owner:" + insertmap["fname"] + " " + insertmap["lname"])

    owneraddrmap = extract_owneraddress(
        rawhtml
    )  # Potentially raises MalformedAddressError

    insertmap["address_street"] = owneraddrmap["street"]
    insertmap["address_city"] = owneraddrmap["city"]
    insertmap["address_state"] = owneraddrmap["state"]
    insertmap["address_zip"] = owneraddrmap["zipc"]
    print(
        "extracted owner address:"
        + " "
        + insertmap["address_street"]
        + " "
        + insertmap["address_city"]
        + ", "
        + insertmap["address_state"]
        + " "
        + insertmap["address_zip"]
    )

    # Todo: Refactor into 2 new columns: raw_name and raw_address
    insertmap["notes"] = (
        notemsg
        + ownernamemap["note_namedump"]
        + " Raw address: "
        + owneraddrmap["notes_adrdump"]
    )
    print("Inserting person data for id: %s" % ownernamemap['fname'])
    print("person notes: " + insertmap["notes"])

    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print("----- committed person owner -----")
    return cursor.fetchone()[0] # Returns the person_id



def connect_person_to_property(propertyid, personid):
    insert_sql = """
        INSERT INTO public.propertyperson(
            property_propertyid, person_personid)
    VALUES (%(prop)s, %(pers)s);
    """
    insertmap = {}

    # load up vars for use in SQL from each of the parse methods
    insertmap["prop"] = propertyid
    insertmap["pers"] = personid

    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print("----- connected person owner to property -----")


def create_and_insert_default_unit(propertyid, default_unit=-1):
    insert_sql = """
        INSERT INTO public.propertyunit(
            unitid, unitnumber, property_propertyid, otherknownaddress, notes, 
            rental)
        VALUES (DEFAULT, %(default_unit)s, %(property_propertyid)s, NULL, 
            'robot-generated unit representing the primary habitable dwelling on a property', 
            FALSE);
    """
    insertmap = {}

    # load up vars for use in SQL from each of the parse methods
    insertmap["property_propertyid"] = propertyid
    insertmap["default_unit"] = default_unit

    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print("----- built unit zero -----")


def get_county_page_for(parcel_id):
    COUNTY_REAL_ESTATE_URL = (
        "http://www2.county.allegheny.pa.us/" "RealEstate/GeneralInfo.aspx?"
    )
    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }
    # # Todo: Why are we pretending to be a human? It takes a lot of time. Let's see if it breaks if we just request data
    # waittime = random.uniform(0.0,1.0)
    # print("waiting:" + str(waittime))
    # time.sleep(waittime)
    try:
        response = requests.get(
            COUNTY_REAL_ESTATE_URL, params=search_parameters, timeout=5
        )
        print("Scraping data from county: %s" + parcel_id)
    except requests.exceptions.Timeout:
        # Todo: Error handaling if this also fails
        # Wait 10 secs and try one more time
        time.sleep(10)
        response = requests.get(
            COUNTY_REAL_ESTATE_URL, params=search_parameters, timeout=5
        )
    # Todo: See if county_info_cache is a good thing.
    # county_info_cache[parcel_id] = response.text
    return response.text


# ---------------------------------------------
#           HTML SCRAPING METHODS
# ---------------------------------------------


def extract_lotandblock_fromparid(parid):
    trimmedparid = parid[0:11]
    exp = re.compile(r"([1-9]+)(\w)[0]*([1-9]+)")
    gl = re.search(exp, trimmedparid)
    if gl:
        lob = gl.group(1) + "-" + gl.group(2) + "-" + gl.group(3)
        return lob

    # Todo: Should this return -1?
    else:
        raise MalformedLotAndBlockError


def extract_owner_name(property_html):
    OWNER_NAME_SPAN_ID = "BasicInfo1_lblOwner"
    persondict = {}

    soup = bs4.BeautifulSoup(property_html, "lxml")
    owner_full_name = soup.find("span", id=OWNER_NAME_SPAN_ID).text
    print("owner_raw_name:" + str(owner_full_name))
    # Remove extra spaces from owner's name
    persondict["note_namedump"] = re.sub(r"\s+", " ", owner_full_name.strip())

    exp = re.compile(r"(\w+|[&])\s+(\w+|[&])\s*(\w*|[&]).*")
    namegroups = re.search(exp, owner_full_name)
    # print(len(namegroups.groups()))
    try:
        persondict["lname"] = str(namegroups.group(1)).title()

        if len(namegroups.groups()) == 2:
            persondict["fname"] = str(namegroups.group(2)).title()
        elif len(namegroups.groups()) == 3:
            persondict["fname"] = (
                str(namegroups.group(2)).title()
                + " "
                + str(namegroups.group(3)).title()
            )
        print(str(persondict["fname"]) + " " + str(persondict["lname"]))
    except AttributeError:
        raise MalformedOwnerError

    return persondict


def extract_propertyaddress(property_html):
    propaddrmap = {}
    PROP_ADDR_SPAN_ID = "BasicInfo1_lblAddress"
    # print(property_html)
    soup = bs4.BeautifulSoup(property_html, "lxml")

    # this yeilds something like
    # 471&nbsp;WALNUT  ST<br>PITTSBURGH,&nbsp;PA&nbsp;15238
    prop_addr_raw = soup.find("span", id=PROP_ADDR_SPAN_ID).text
    scrapedhtml = soup.select("#BasicInfo1_lblAddress")

    print("Scraped:" + str(scrapedhtml))
    soup = bs4.BeautifulSoup(str(scrapedhtml), "lxml")
    adrlistraw = soup.span.contents
    # make sure we have all the parts of the address
    if len(adrlistraw) < 3:
        raise MalformedGenericAddressError


    propaddrmap["street"] = re.sub("  ", " ", adrlistraw[0])
    print(propaddrmap["street"])
    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile("[^,]*")
    propaddrmap["city"] = exp.search(adrlistraw[2]).group()
    print("city:" + propaddrmap["city"])
    # hard-code pa
    propaddrmap["state"] = "PA"
    # zip is just the last 5 chars
    propaddrmap["zipc"] = adrlistraw[2][-5:]
    print("Zip:" + propaddrmap["zipc"])

    return propaddrmap


def extract_owneraddress(property_html):
    print("--------- extracting owner address ------------")
    owneraddrmap = {}
    # print(property_html)
    soup = bs4.BeautifulSoup(property_html, "lxml")
    # this yeilds something like
    # 471&nbsp;WALNUT  ST<br>PITTSBURGH,&nbsp;PA&nbsp;15238
    scrapedhtml = soup.select("#lblChangeMail")
    owneraddrmap["notes_adrdump"] = str(scrapedhtml)

    print("Scraped:" + str(scrapedhtml))
    soup = bs4.BeautifulSoup(str(scrapedhtml), "lxml")
    # this spits out a three-item list. We throw away the <br> which is
    # the middle item. The first is the street, the second gets chopped up
    adrlistraw = soup.span.contents
    # make sure we have all the parts of the address
    if len(adrlistraw) < 3:
        # Todo: This raises a generic error. Should it be more specific? How would you know what the actual problem is?
        raise MalformedGenericAddressError


    owneraddrmap["street"] = re.sub("  ", " ", adrlistraw[0])
    print(owneraddrmap["street"])
    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile("[^,]*")
    owneraddrmap["city"] = exp.search(adrlistraw[2]).group()
    print("city:" + owneraddrmap["city"])
    exp = re.compile(r",\s*(\w\w)")
    m = re.search(exp, adrlistraw[2])
    try:
        owneraddrmap["state"] = str(m.group(1))
        print("state:" + owneraddrmap["state"])
    except AttributeError:
        raise MalformedStateError


    # owner zips could come in as: 15218 OR 15218- OR 15218-2342
    # just lose the routing numbers and take the first digits until the -
    exp = re.compile(r"\d+")
    m = re.search(exp, adrlistraw[2])
    try:
        owneraddrmap["zipc"] = str(m.group())
    except AttributeError:
        raise MalformedZipcodeError


    # another abandoned string slicing approach:
    # also too brittle given range of scraped inputs
    # owneraddrmap['zip'] = adrlistraw[2][-10:]
    print("zip:" + owneraddrmap["zipc"])

    return owneraddrmap


def extract_propertyusetype(property_html):
    USE_TYPE_SPAN_ID = "lblUse"

    soup = bs4.BeautifulSoup(property_html, "lxml")
    usetype = soup.find("span", id=USE_TYPE_SPAN_ID).text
    # Remove extra spaces
    usetype = re.sub(r"\s+", " ", usetype.strip())
    # Remove leading spaces before commas
    usetype = str(re.sub(r"\s+,", ",", usetype)).title()
    return usetype


def extract_class(property_html):
    CLASS_SPAN_ID = "lblState"

    soup = bs4.BeautifulSoup(property_html, "lxml")
    propclass = soup.find("span", id=CLASS_SPAN_ID).text
    # Remove extra spaces
    propclass = re.sub(r"\s+", " ", propclass.strip())
    # Remove leading spaces before commas
    propclass = str(re.sub(r"\s+,", ",", propclass)).title()
    return propclass


def extract_ownercode(property_html):
    OWNER_CODE_SPAN_ID = "lblOwnerCode"

    soup = bs4.BeautifulSoup(property_html, "lxml")
    ownercode = soup.find("span", id=OWNER_CODE_SPAN_ID).text
    # Remove extra spaces
    ownercode = re.sub(r"\s+", " ", ownercode.strip())
    # Remove leading spaces before commas
    ownercode = str(re.sub(r"\s+,", ",", ownercode)).title()
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

# # Todo: deprecate and replace with log_error_NEW()
# def logerror(parcelid):
#     with open(LOG_FILE, "a", encoding=CSV_FILE_ENCODING) as outfile:
#         writer = csv.writer(outfile)
#         writer.writerow([parcelid])


def log_error(error, parcelID):
    if error.subtype:
        logger.warning(
            "Malformed %s at parcel ID %s: %s could not be parsed",
            error.type,
            parcelID,
            error.subtype,
            exc_info=True
        )
    else:
        logger.warning(
            "Malformed %s at parcel ID %s", error.type, parcelID, exc_info=True
        )


# for debugging and transferability purposes
def save_properties_as_csv(properties_with_owner_info, output_file):
    fieldnames = None
    with open(output_file, "w", encoding=CSV_FILE_ENCODING) as outfile:
        writer = csv.writer(outfile)
        for prop in properties_with_owner_info:
            if fieldnames is None:
                fieldnames = sorted(prop.keys())
                writer.writerow(fieldnames)
            writer.writerow([prop[k] for k in fieldnames])


db_conn = None


def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    # Todo: Read connection credentials from a secrets.json
    db_conn = psycopg2.connect(
        database="cogdb", user="sylvia", password="c0d3", host="localhost"
    )
    global cursor
    cursor = db_conn.cursor()


if __name__ == "__main__":
    main()
