import psycopg2
import os
import datetime as d
from tqdm import tqdm
import re
import time

db_conn = None
root_path = '/home/cogconnect2/test2data/Wilkins'
current_municode = 953

error_type_100 = '100'  # unmatched_housedir_error
error_type_200 = '200'  # unmatched_filetype_error
error_type_300 = '300'  # duplicate_housedir_error

error_type_400 = '400'  # vacant_streetdir_error
error_type_500 = '500'  # impure_streetdir_error
error_type_600 = '600'  # vacant_housesdir_error

error_type_700 = '700'  # unmatched_duplicate_housedir_error
error_type_800 = '800'  # duplicate_duplicate_housedir_error

unmatched_housedir_log = 'errorlog/unmatched_housedir_error.txt'
unmatched_filetype_log = 'errorlog/unmatched_filetype_error.txt'
duplicate_housedir_log = 'errorlog/duplicate_housedir_error.txt'

vacant_streetdir_log = 'errorlog/vacant_streetdir_error.txt'
impure_streetdir_log = 'errorlog/impure_streetdir_error.txt'
vacant_housesdir_log = 'errorlog/vacant_housesdir_error.txt'

unmatched_duplicate_housedir_log = 'errorlog/unmatched_duplicate_housedir.txt'
duplicate_duplicate_housedir_log = 'errorlog/duplicate_duplicate_housedir.txt'

unmatched_housedir_dict = {}
unmatched_filetype_dict = {}
duplicate_housedir_dict = {}

vacant_streetdir_dict = {}
impure_streetdir_dict = {}
vacant_housesdir_dict = {}

duplicate_in_error_housedir_dict = {}
duplicate_duplicate_housedir_dict = {}

duplacate_housedirandpidlist = {'136 Wallace': ['0373R00250000000'],
                                    '110 Wallace': ['0373L00130000000', '0373L00128000000'],
                                    '599 Lucia': ['0372L00128000000'],
                                    '1235 Rodi': ['0542J00020000000'],
                                    '909 Alpine': ['0372P00172000000'],
                                    '348 Churchill': ['0453F00374000000', '0453F00372000000'],
                                    '344 Churchill': ['0453F00368000000', '0453F00364000000', '0453F00370000000'],
                                    '204 Churchill': ['0454B00152000000'],
                                    '119 Churchill': ['0454L00106000000'],
                                    '230 Kingston': ['0541P00174000000'],
                                    '436 Cline': ['0374D00073000000'],
                                    '429 Cline': ['0374D00228000000'],
                                    '365 Cline': ['0373S00066000000', '0373S00066000100'],
                                    '835 Railroad': ['0454L00240000000', '0454L00242000000'],
                                    '131 Curry': ['0454G00114000000'],
                                    '441 Clugston': ['0455E00190000000', '0455E00191000000'],
                                    '543 Clugston': ['0455A00063000000', '0455A00061000000'],  #
                                    '643 Mortimer': ['0454J00085000000'],
                                    '817 Thompson': ['0454K00115000000'],
                                    '635 Beaver': ['0454N00178000000'],
                                    '657 Highland': ['0454K00276000000'],
                                    '825 Elizabeth': ['0372P00307000000'],  # some different parid
                                    '718 Brown': ['0372M00255000000'],
                                    '570 Brown': ['0373H00125000000']}

def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        database="cogdb",
        user="cogconnect2",
        password="c0d3",
        host="localhost"
    )
    return db_conn


def get_error_logs():
    write_error_log(unmatched_housedir_dict, "Unmatched housesdir", error_type_100, unmatched_housedir_log)
    write_error_log(unmatched_filetype_dict, "Unmatched filetype", error_type_200, unmatched_filetype_log)
    write_error_log(duplicate_housedir_dict, "Duplicate housesdir", error_type_300, duplicate_housedir_log)
    write_error_log(vacant_streetdir_dict, "Vacant streetdir", error_type_400, vacant_housesdir_log)
    write_error_log(impure_streetdir_dict, "Impure streetdir", error_type_500, impure_streetdir_log)
    write_error_log(vacant_housesdir_dict, "Vacant housesdir", error_type_600, vacant_housesdir_log)
    write_error_log(duplicate_in_error_housedir_dict, "unmatched_duplicate housesdir", error_type_700, unmatched_duplicate_housedir_log)


def initialize_error_log(path):
    file = open(path, 'w')
    file.close()


def write_error_log(errordict, errordescription, error_type, error_file_path):
    initialize_error_log(error_file_path)
    for n in errordict.keys():
        vacant_housedir_name = os.path.basename(n)
        error = errordescription + ": " + vacant_housedir_name
        file = open(error_file_path, 'a')
        date = d.datetime.now().strftime("%Y.%m.%d-%H:%M.%S")
        file.write(error_type + ", " + date + ': ' + error + '\n')
        file.close()


def get_photofile_dict():

    # declaring a dict to store the information about photo file
    photo_dict = {}

    # retrieving the street name
    streetdirnamelist = os.listdir(root_path)

    # retrieving the house name
    for m in streetdirnamelist:

        # getting the path of street directory
        streetdirpath = os.path.join(root_path, m)

        # getting a list of the house names
        housedirnamelist = os.listdir(streetdirpath)

        if housedirnamelist:

            extrafilesinstreetdirlist = []

            # retrieving the pdf and storing the name and path into photo_dict
            for n in housedirnamelist:

                # getting the path of house directory
                housedirpath = os.path.join(streetdirpath, n)

                # finding house dir
                if os.path.isdir(housedirpath):

                    # declaring a dict to store the name and path of photos
                    photofile_path_dict = {}

                    filenum = 0

                    # retrieving all the file in house dirs
                    for root, dirs, files in os.walk(housedirpath):

                        # iterating the file in house dirs
                        for i in files:

                            filenum = filenum + 1

                            # getting the file path of the photos
                            photofile_path = os.path.join(root, i)

                            # getting the filename relative to the housedirpath
                            relative_filename = os.path.relpath(photofile_path, housedirpath)

                            # storing the name and path of the photo file into pad_path_dict
                            photofile_path_dict[relative_filename] = photofile_path

                    if filenum > 0:
                        photo_dict[housedirpath] = photofile_path_dict
                    elif filenum == 0:
                        vacant_housesdir_dict[n] = housedirpath

                else:
                    extrafilesinstreetdirlist.append(housedirpath)

            if extrafilesinstreetdirlist:
                impure_streetdir_dict[m] = extrafilesinstreetdirlist

        else:
            # recording vacant street dir
            vacant_streetdir_dict[m] = streetdirpath

    return photo_dict


def get_typetitle_list():

    # declaring a list to store typetitle
    typetitlelist = []

    # getting photo_dict
    photo_dict = get_photofile_dict()

    # getting photo_dict keys about house name
    photo_dict_key_list = photo_dict.keys()

    # getting each house name
    for n in photo_dict_key_list:

        # getting the each file path
        file_path_dict = photo_dict[n]

        # getting the file path
        for m in file_path_dict.keys():
            file_path = file_path_dict[m]

            # getting the typetitle
            typetitle = get_photofile_typetitle(file_path)

            # storing the typetitle into the list
            if typetitle not in typetitlelist:
                typetitlelist.append(typetitle)

    # returning the list
    return typetitlelist


def get_photofile_typetitle(filepath):

    # getting the photo file type
    file_extensions = os.path.splitext(filepath)[-1]
    return file_extensions


def get_dostore_photodoctype_typeid(file_path):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    typetitle = get_photofile_typetitle(file_path)

    sql_command_select_typeid = """
            SELECT typeid, donotstoretype
            FROM public.photodoctype where typetitle = %s;
        """
    cursor.execute(sql_command_select_typeid, (typetitle,))
    m = cursor.fetchone()
    if not m[1]:
        return m[0]
    else:
        return None


def get_propertyid(houseaddress):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for retrieving the propertyid corresponded with photodic
    sql_command = """
            SELECT propertyid 
            FROM public.property WHERE address ILIKE %s and municipality_municode = %s;
        """

    # reformatting the address str for sql_command
    sql_address = houseaddress.replace(' ', '_') + '%'

    # executing the sql_command
    cursor.execute(sql_command, (sql_address, current_municode))

    # returning the propertyid corresponding to the address
    result = cursor.fetchall()
    resultlength = len(result)

    if resultlength == 1:
        for m in result:
            for n in m:
                return n
    elif resultlength == 0:
        return -100
    else:
        return -300


def convert_file_to_photodocblob(file_path):
    file = open(file_path, 'rb')
    data = file.read()
    file_binary = psycopg2.Binary(data)
    return file_binary


def check_photodoctype_typetitle_exist(typetitle):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement to select any row corresponding with the pdf name
    sql_command_select_typetitle = """
                SELECT typetitle
                FROM public.photodoctype WHERE typetitle = %s ;
            """

    # executing the statement
    cursor.execute(sql_command_select_typetitle, (typetitle,))
    n = cursor.fetchone()

    # if the data has existed in photodoctype table, return 1
    if n is not None:
        return 1

    # if the data hasn't existed, return 0
    else:
        return 0


def check_photodocdescription_exist(file_name):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement to select any row corresponding with the pdf name
    sql_command_select_photodocdescription = """
            SELECT photodocdescription
            FROM public.photodoc WHERE photodocdescription = %s ;
        """

    # executing the statement
    cursor.execute(sql_command_select_photodocdescription, (file_name,))
    n = cursor.fetchone()

    # if the data has existed in photodoc table, return 1
    if n is not None:
        return 1

    # if the data hasn't existed, return 0
    else:
        return 0


def insert_propertyphotodoc_table(photodocid, propertyid):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for inserting the keys into propertyphotodoc table
    sql_command_insert_propertyphotodoc = """
        INSERT INTO public.propertyphotodoc
            (photodoc_photodocid, property_propertyid)
        VALUES (%s, %s);
    """

    # executing the statement
    cursor.execute(sql_command_insert_propertyphotodoc, (photodocid, propertyid))


def insert_photodoctype_table():

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # getting a list of photo type title
    typetitlelist = get_typetitle_list()

    # count the number of adding type
    addnum = 0

    for n in typetitlelist:

        # getting the photo type title
        typetitle = n

        # checking if the typetitle is exist or not (0 represent that the file is not exist)
        if check_photodoctype_typetitle_exist(typetitle) == 0:

            # inserting the typetitle
            loop = True
            while loop:

                try:

                    # prompting user to input the donotstoretype
                    print('- Declare donotstoretype of  ' + n)
                    choice = int(input('- Press 1: TRUE (do not store the type), Press 2: FALSE (store the type): '))

                    if choice == 1:
                        addnum = addnum + 1
                        donotstoretype = True
                        loop = False
                    elif choice == 2:
                        addnum = addnum + 1
                        donotstoretype = False
                        loop = False

                except:
                    print('[ Notice ] Please insert the correct number')

            # creating a sql statement for inserting the photodoctype table
            sql_command_insert_photodoctype = """
                    INSERT INTO public.photodoctype
                        (typeid, typetitle, donotstoretype)
                    VALUES (default, %s, %s);
                """

            # executing the statement
            cursor.execute(sql_command_insert_photodoctype, (typetitle, donotstoretype))

    if addnum == 0:
        print('- No additional donotstoretype')


def select_photodoctype_table():

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for selecting photodoctype table
    sql_command_select_photodoctype = """
                SELECT typeid, typetitle, donotstoretype
                FROM public.photodoctype;
            """

    # executing the statement
    cursor.execute(sql_command_select_photodoctype)
    result = cursor.fetchall()

    # showing the photodoctype table
    print('- Currrent photodoctype table: ')
    print("{:<15s}{:<15s}{:<15s}".format("- typeid", "typetitle", "donotstoretype"))
    for n in result:
        print("{:<15s}{:<15s}{:<15s}".format('- '+str(n[0]), str(n[1]), str(n[2])))


def get_photodocid(filename):
    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for selecting photodoctype table
    sql_command_select_photodocid = """
                   SELECT photodocid
                   FROM public.photodoc WHERE photodocdescription = %s ;
               """
    cursor.execute(sql_command_select_photodocid, (filename,))
    result = cursor.fetchone()
    return result[0]




def check_propertyphotodoc_propertyid_exisit(photodocid, propertyid):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for selecting photodoctype table
    sql_command_select_propertyphotodoc = """
                SELECT property_propertyid
                FROM public.propertyphotodoc WHERE photodoc_photodocid = %s;
            """
    cursor.execute(sql_command_select_propertyphotodoc , (photodocid,))
    checkexist = False
    result = cursor.fetchall()
    for n in result:
        for m in n:
            if m == propertyid:
                checkexist=True
    return checkexist

def update_photodoctype_table():

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for updating photodoctype table
    sql_command_update_photodoctype = """
            UPDATE public.photodoctype
            SET donotstoretype=%s
            WHERE typetitle=%s;
        """
    loop = True
    while loop:

        # prompting user to input the typetitle needed to be adjusted
        typetitle = str(input('- Enter the typetitle:'))

        if check_photodoctype_typetitle_exist(typetitle) == 1:

            # prompting user to input the donotstoretype needed to be adjusted
            choice = int(input('- Entering the donotstoretype: Press 1 for TRUE, Press 2 for FALSE:'))

            if choice == 1:
                donotstoretype = True
                loop = False
                cursor.execute(sql_command_update_photodoctype, (donotstoretype, typetitle))
            elif choice == 2:
                donotstoretype = False
                loop = False
                cursor.execute(sql_command_update_photodoctype, (donotstoretype,typetitle))
            else:
                print('[ Notice ] Please insert the correct number')

        else:
            print('[ Notice ] Please insert the correct typetitle')


def delete_photodoc_donotstoretype():

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    sql_command_delete_propertyphotodoc = """
            DELETE FROM propertyphotodoc
            USING photodoctype, photodoc
            WHERE photodoc.photodocid = propertyphotodoc.photodoc_photodocid
            AND photodoctype.typeid = photodoc.photodoctype_typeid
            AND photodoctype.donotstoretype = TRUE;
        """

    sql_command_delete_photodoc = """
            DELETE FROM photodoc
            USING photodoctype
            WHERE photodoctype.typeid = photodoc.photodoctype_typeid
            AND photodoctype.donotstoretype = TRUE;
        """

    sql_command_select_photodoc_donotstoretype = """
            SELECT photodocdescription, photodocid
            FROM photodoc,photodoctype
            WHERE photodoc.photodoctype_typeid= photodoctype.typeid
            AND photodoctype.donotstoretype = TRUE
        """
    cursor.execute(sql_command_select_photodoc_donotstoretype)

    redundancynum = 0

    for n in cursor.fetchall():
        redundancynum = redundancynum + 1

    if redundancynum == 0:
        print('- No redundancy record in photodoc table')
    else:

        loop = True
        while loop:
            print('- Delete redundancy record in photodoc table ('+str(redundancynum) +'records)')
            choice = int(input('\n- Press 1: DO Delete, 2: Do Not Delete:'))

            # delete
            if choice == 1:
                cursor.execute(sql_command_delete_propertyphotodoc)
                cursor.execute(sql_command_delete_photodoc)
                loop = False

            # not delete
            elif choice == 2:
                loop = False
                print('- Keep redundancy record in photodoc table')
            else:
                print('[ Notice ] Please insert the correct number')


def setting_initializing():

    print('\n[ STEP-1: Insert photodoctype table ]\n')
    # inserting photodoctype table
    insert_photodoctype_table()


    loop_adjust = True
    while loop_adjust:

        print('--------------------------------------------------------------------------------------')
        print('\n[ STEP-2: Adjust photodoctype table ]\n')
        # showing photodoctype table
        select_photodoctype_table()

        loop_insert = True
        while loop_insert:
            try:
                # choose whether to update or not
                choice = int(input('\n- Press 1: Do Update, Press 2: Do Not Update: '))

                if choice == 1:

                    # updating photodoctype table
                    update_photodoctype_table()

                elif choice == 2:

                    # stopping updating
                    loop_insert = False
                    print('- Finishing Updating')
            except:
                print('[ Notice ] Please insert the correct number')

        print('--------------------------------------------------------------------------------------')
        print('\n[ STEP-3: Confirm photodoctype table ]\n')

        # showing photodoctype table
        select_photodoctype_table()

        # confirm
        confirm_loop = True
        while confirm_loop:
            try:
                confirm = int(input('\n- Press 1: Confirm, Press 2: Adjusting again: '))

                if confirm == 1:
                    loop_adjust = False
                    confirm_loop = False
                elif confirm == 2:
                    loop_adjust = True
                    confirm_loop = False

            except:
                print('[ Notice ] Please insert the correct number')
                confirm_loop = True
    print('--------------------------------------------------------------------------------------')
    print('\n[ STEP-4: Delete redundancy record ]\n')
    # delete redundancy record in photodoc table
    delete_photodoc_donotstoretype()

    print('--------------------------------------------------------------------------------------')
    print('\n[ STEP-5: Starting inserting  data ]\n')
    assemble_photodoc_propertyphotodoc_photodoctype_table()

    print('\n[ STEP-6: Dealing with error log ]\n')
    akaerror_loop = True
    while akaerror_loop:
        try:
            print('- Dealing with aka unmatched housedir ?')
            akaerror_choice = int(input('- Press 1: YES, Press 2: NO: '))

            if akaerror_choice == 1:
                error_housedir_disposal(1, unmatched_housedir_dict)
                akaerror_loop = False
            elif akaerror_choice == 2:
                akaerror_loop = False

        except:
            print('[ Notice ] Please insert the correct number')
            akaerror_loop = True
    print('--------------------------------------------------------------------------------------')
    dasherror_loop = True
    while dasherror_loop:
        try:
            print('- Dealing with dash unmatched housedir ?')
            dasherror_choice = int(input('- Press 1: YES, Press 2: NO: '))

            if dasherror_choice == 1:
                error_housedir_disposal(2, unmatched_housedir_dict)
                dasherror_loop = False
            elif dasherror_choice == 2:
                dasherror_loop = False

        except:
            print('[ Notice ] Please insert the correct number')
            dasherror_loop = True
    print('--------------------------------------------------------------------------------------')
    duplicate_loop = True
    while duplicate_loop:
        try:
            print('- Dealing with duplicate housedir ?')
            duplicate_choice = int(input('- Press 1: YES, Press 2: NO: '))

            if duplicate_choice == 1:
                error_housedir_disposal(3, duplicate_housedir_dict)
                duplicate_loop = False
            elif duplicate_choice == 2:
                duplicate_loop = False

        except:
            print('[ Notice ] Please insert the correct number')
            duplicate_loop = True


def insert_photodoc_propertyphotodoc_table(photodoc_description, typeid, photodoc_blob, propertyid):
    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for inserting the data into photodoc table
    sql_command_insert_photodoc = """
            INSERT INTO public.photodoc
                (photodocid, photodocdescription, photodocdate, photodoctype_typeid, photodocblob, photodoccommitted)
            VALUES (Default, %s, CURRENT_TIMESTAMP(3), %s, %s, TRUE) RETURNING photodocid;
        """
    cursor.execute(sql_command_insert_photodoc, (photodoc_description, typeid, photodoc_blob))

    # getting the last insert photodocid
    for photodocid in cursor.fetchone():
        # inserting propertyphotodoc table
        insert_propertyphotodoc_table(photodocid, propertyid)


def insert_akaproperty_table(property_propertyid, akaaddress):
    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for inserting the data into akaproperty table
    sql_command_insert_akaproperty = """
            INSERT INTO public.akaproperty
                (akapropertyid, property_propertyid, akaaddress)
            VALUES (Default, %s, %s);
        """
    cursor.execute(sql_command_insert_akaproperty, (property_propertyid, akaaddress))


def assemble_photodoc_propertyphotodoc_photodoctype_table():

    # getting photo_dict
    photo_dict = get_photofile_dict()

    # getting photo_dict keys about house name
    photo_dict_key_list = photo_dict.keys()

    # getting each house name
    for n in tqdm(photo_dict_key_list):

        # getting a dict including the name and path of the photo pdf
        filename_path_dict = photo_dict[n]

        housedirpath = n
        housedirname = os.path.basename(n)

        # getting propertyid
        propertyid = get_propertyid(housedirname)

        unmatched_housedir_photoname_path_map = {}

        duplicate_housedir_photoname_path_map = {}

        unmatched_filetype_photoname_path_map = {}

        # getting the pdf name and pdf path
        for m in filename_path_dict.keys():
            pdf_name = m
            pdf_path = filename_path_dict[m]

            # getting the photodocdescription
            photodoc_description = pdf_name

            # checking if the the corresponding propertyid is exist or not
            if check_photodocdescription_exist(m) == 0:

                typeid = get_dostore_photodoctype_typeid(pdf_path)

                if typeid is not None:

                    # getting the photodocblob
                    photodoc_blob = convert_file_to_photodocblob(pdf_path)

                    # checking propertyid
                    if propertyid != (-100) and propertyid != (-300):

                        # inserting photodoc table and getting the last insert photodocid
                        insert_photodoc_propertyphotodoc_table(photodoc_description, typeid, photodoc_blob, propertyid)

                    elif propertyid == (-100):
                        unmatched_housedir_photoname_path_map[pdf_name] = pdf_path

                    elif propertyid == (-300):
                        duplicate_housedir_photoname_path_map[pdf_name] = pdf_path

                else:
                    unmatched_filetype_photoname_path_map[pdf_name] = pdf_path

        if unmatched_housedir_photoname_path_map:
            unmatched_housedir_dict[housedirpath] = unmatched_housedir_photoname_path_map

        if duplicate_housedir_photoname_path_map:
            duplicate_housedir_dict[housedirpath] = duplicate_housedir_photoname_path_map

        if unmatched_filetype_photoname_path_map:
            unmatched_filetype_dict[housedirpath] = unmatched_filetype_photoname_path_map


def get_similar_smart_search_str(photodoc_housedirname):

    # removing all the '-'
    housename = photodoc_housedirname.replace('-', ' ')

    # getting smart searching str key words by first two words of the house dir name
    smart_str = housename.split()[0]+" " + housename.split()[1]

    return smart_str


def get_dash_smart_search_str(photodoc_housedirname,num):

    smart_str = str(num) + " " + photodoc_housedirname.split()[1]

    return smart_str


def get_property_addressandpropertyid_from_address(address):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for retrieving the propertyid and address corresponded with photodic
    sql_command = """
                SELECT address, propertyid 
                FROM public.property WHERE address ILIKE %s and municipality_municode = %s;
            """
    # reformatting the address str for sql_command
    sql_address = address.replace(' ', '%') + '%'

    # executing the statement
    cursor.execute(sql_command, (sql_address, current_municode))

    # returning the propertyid corresponding to the address
    result = cursor.fetchall()
    resultlength = len(result)

    if resultlength == 1:
        for m in result:
            return m
    elif resultlength == 0:
        return -100
    else:
        return -300


def get_property_addressandpropertyid_from_parid(address, parid):

    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement for retrieving the propertyid and address corresponded with photodic
    sql_command = """
                SELECT address, propertyid 
                FROM public.property WHERE address ILIKE %s and municipality_municode = %s and parid = %s;
            """
    # reformatting the address str for sql_command
    sql_address = address.replace(' ', '_') + '%'

    # executing the statement
    cursor.execute(sql_command, (sql_address, current_municode,parid))

    # returning the propertyid corresponding to the address
    result = cursor.fetchall()
    resultlength = len(result)

    if resultlength == 1:
        for m in result:
            return m
    elif resultlength == 0:
        return -100
    else:
        return -300


def error_housedir_disposal(disposalchoice, errordict):

    # getting photo_dict keys about house name
    photo_dict_key_list = errordict.keys()

    # getting each record of house dir names in error_record_match_log
    for n in photo_dict_key_list:

        # geeting house dir name
        housedirname = os.path.basename(n)

        # dealing with aka error
        if disposalchoice == 1:

            # searching record in property table where address is similar to the house dir name by searching first two words
            housedirsmartname = get_similar_smart_search_str(housedirname)

            # Select records matching with housedirsmartname
            resultlist = get_property_addressandpropertyid_from_address(housedirsmartname)

            # Finding the correct record in database and store it
            insert_unmatched_housedir(errordict, n, housedirname, resultlist, disposalchoice)

        # dealing with dash error
        elif disposalchoice == 2:

            if re.match(r'\d+-\d+', housedirname):
                result = re.findall(r'\d+', housedirname)

                start_housedirname_num = int(result[0])
                end_housedirname_num = int(result[1])

                while start_housedirname_num <= end_housedirname_num:
                    housedirsmartname = get_dash_smart_search_str(housedirname, start_housedirname_num)
                    resultlist = get_property_addressandpropertyid_from_address(housedirsmartname)
                    insert_unmatched_housedir(errordict, n, housedirname, resultlist, disposalchoice)
                    start_housedirname_num = start_housedirname_num + 1

        elif disposalchoice == 3:
            housedirkeyslist = duplacate_housedirandpidlist.keys()
            for m in housedirkeyslist:
                paridlist = duplacate_housedirandpidlist[m]
                if len(paridlist) == 1:
                    resultlist = get_property_addressandpropertyid_from_parid(housedirname, paridlist[0])
                    insert_unmatched_housedir(errordict, n, housedirname, resultlist, disposalchoice)


def update_error():
    unmatched_housedir_dict.clear()
    unmatched_filetype_dict.clear()
    duplicate_housedir_dict.clear()
    print("Updating the error log.......")
    assemble_photodoc_propertyphotodoc_photodoctype_table()


def insert_unmatched_housedir(unmatched_dict, housedirpath, housedirname, resultlist, disposalchoice):

    duplicate_housedir_in_errorhousedir_map = {}
    # Finding the unique result matching
    if resultlist != (-100) and resultlist != (-300):
        loop = True
        while loop:
            try:
                if disposalchoice == 3:
                    choice = 1
                else:
                    print(' - Is ', housedirname, " as known as ", resultlist[0], "?")
                    choice = int(input('- Press 1: YES, Press 2: NOT: '))

                if choice == 1:
                    loop = False
                    if disposalchoice != 3:
                        insert_akaproperty_table(resultlist[1], housedirname)

                    # getting a dict including the name and path of the photo pdf
                    filename_path_dict = unmatched_dict[housedirpath]

                    # getting the pdf name and pdf path
                    for g in filename_path_dict.keys():
                        pdf_name = g
                        pdf_path = filename_path_dict[g]

                        # getting the photodocdescription
                        photodoc_description = pdf_name

                        # checking if the the corresponding propertyid is exist or not
                        if check_photodocdescription_exist(g) == 0:

                            typeid = get_dostore_photodoctype_typeid(pdf_path)

                            if typeid is not None:
                                # getting the photodocblob
                                photodoc_blob = convert_file_to_photodocblob(pdf_path)

                                insert_photodoc_propertyphotodoc_table(photodoc_description, typeid, photodoc_blob, resultlist[1])
                        else:
                            if disposalchoice == 2:
                                photodocid = get_photodocid(pdf_name)
                                if not check_propertyphotodoc_propertyid_exisit(photodocid, resultlist[1]):
                                    insert_propertyphotodoc_table(photodocid, resultlist[1])

                elif choice == 2:
                    loop = False
            except:
                print('[ Notice ] Please insert the correct number')

    elif resultlist == (-300):

        # getting a dict including the name and path of the photo pdf
        filename_path_dict = unmatched_dict[housedirpath]

        # getting the pdf name and pdf path
        for g in filename_path_dict.keys():
            pdf_name = g
            pdf_path = filename_path_dict[g]
            duplicate_housedir_in_errorhousedir_map[pdf_name] = pdf_path

    if duplicate_housedir_in_errorhousedir_map:
        duplicate_in_error_housedir_dict[housedirname] = duplicate_housedir_in_errorhousedir_map


def main():

    setting_initializing()
    time.sleep(2)
    update_error()
    db_conn.commit()
    db_conn.close()
    get_error_logs()


main()