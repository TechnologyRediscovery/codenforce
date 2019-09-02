import os

import psycopg2

db_conn = None


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


def select_property_address(houseaddress):
    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # reformatting the address str for sql_command
    sql_address = houseaddress.split()[0] + "_" + houseaddress.split()[1] + '%'

    # creating a sql statement to select any row that match with address in property table
    sql_command_select_address = """
                    SELECT propertyid, address, parid
                    FROM public.property WHERE address ILIKE %s ;
                """

    # executing the statement
    cursor.execute(sql_command_select_address, (sql_address,))
    result = cursor.fetchall()
    return result


def select_property_propertyid(propertyid):
    # connecting to the database
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    # creating a sql statement to select any row that match with address in property table
    sql_command_select_propertyid = """
                    SELECT propertyid, address, parid
                    FROM public.property WHERE propertyid = %s ;
                """

    # executing the statement
    cursor.execute(sql_command_select_propertyid, (propertyid,))
    result = cursor.fetchone()
    return result

def get_photofile_dict(dirpath):

    photo_dict={}

    # retrieving all the file in house dirs
    for root, dirs, files in os.walk(dirpath):

        # iterating the file in house dirs
        for i in files:

            # getting the file path of the photos
            photofile_path = os.path.join(root, i)

            # getting the filename relative to the housedirpath
            relative_filename = os.path.relpath(photofile_path, dirpath)

            # storing the name and path of the photo file into pad_path_dict
            photo_dict[relative_filename] = photofile_path

    return photo_dict


def convert_file_to_photodocblob(file_path):
    file = open(file_path, 'rb')
    data = file.read()
    file_binary = psycopg2.Binary(data)
    return file_binary


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


def assemble_photodoc_propertyphotodoc_photodoctype_table(dirpath, propertyid):

    # getting photo_dict
    photo_dict = get_photofile_dict(dirpath)

    for m in photo_dict.keys():
        file_name = m
        file_path = photo_dict[m]

        # getting the photodocdescription
        photodoc_description = file_name

        # checking if the the corresponding propertyid is exist or not
        if check_photodocdescription_exist(m) == 0:

            typeid = get_dostore_photodoctype_typeid(file_path)

            if typeid is not None:

                # getting the photodocblob
                photodoc_blob = convert_file_to_photodocblob(file_path)

                # inserting photodoc table and getting the last insert photodocid
                insert_photodoc_propertyphotodoc_table(photodoc_description, typeid, photodoc_blob, propertyid)
            else:
                print('- Wrong type: ' + file_name)
        else:
            print('- File exist: ' + file_name)


def record_photodoc():
    # prompting user to input the house address
    print('--------------------------------------------------------------------------------------')
    print('[ STEP-1: Input the house address: ]')
    inputhouseaddress = input()
    address_resultlist = select_property_address(inputhouseaddress)
    address_resultlistlen = len(address_resultlist)
    if address_resultlistlen == 0:
        print('- Do not find the address in the database')
    elif address_resultlistlen != 0:
        count = 0
        print('- Result:')
        for n in address_resultlist:
            count = count + 1
            propertyid = str(n[0])
            address = str(n[1])
            parid = str(n[2])
            print('- ' + str(count) + ' - propertyid: ' + propertyid + ' house address: ' + address + ' parid: ' + parid)

        print('[ STEP-2: Select the matched address: ]')

        loop = True
        while loop:
            try:
                print('- Input the propertyid:')
                inputpropertyid = input()
                propertyid_resultlist = select_property_propertyid(inputpropertyid)

                m = propertyid_resultlist
                comfirm_propertyid = str(m[0])
                comfirm_address = str(m[1])
                comfirm_parid = str(m[2])
                print('- propertyid: ' + comfirm_propertyid + ' house address: ' + comfirm_address + ' parid: ' + comfirm_parid)

                confirm = int(input('- Press 1: Confirm, Press 2: Adjusting again: '))
                if confirm == 1:
                    loop = False
                    print('[ STEP-3: Input the path of directory that contain photo file: ]')
                    dirpath = input('- Input the dir path:\n')
                    assemble_photodoc_propertyphotodoc_photodoctype_table(dirpath, comfirm_propertyid)
                else:
                    loop = True

            except:
                print('[ Notice ] Please insert the correct number\n')
                loop = True


def main():
    record_photodoc()
    db_conn.commit()
    db_conn.close()

main()