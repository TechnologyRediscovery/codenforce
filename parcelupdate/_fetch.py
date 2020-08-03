import requests
import json

import os

from collections import namedtuple


from _constants import PARCEL_ID_LISTS


Municipality = namedtuple("Municipalicty", ["municode", "name"])


def munis(cursor):
    select_sql = "SELECT municode FROM municipality;"
    cursor.execute(select_sql)
    munis = cursor.fetchall()
    for row in munis:
        yield row[0]


def muniname_from_municode(municode, cursor):
    select_sql = "SELECT municode, muniname FROM municipality where municode = %s"
    cursor.execute(select_sql, [municode])
    row = cursor.fetchone()
    try:
        return Municipality(*row)
    except TypeError as e:
        if row is None:
            raise TypeError(
                "The municode given as an argument likely does not exist in the database"
            )
        else:
            raise e


def muni_data_and_write_to_file(Municipality):
    # Note: The WPRDC limits 50,000 parcels
    script_dir = os.path.dirname(__file__)
    rel_path = os.path.join(PARCEL_ID_LISTS, Municipality.name + "_parcelids.json")
    abs_path = os.path.join(script_dir, rel_path)

    with open(abs_path, "w") as f:
        wprdc_url = """https://data.wprdc.org/api/3/action/datastore_search_sql?sql=
        SELECT * FROM "518b583f-7cc8-4f60-94d0-174cc98310dc" WHERE "MUNICODE" = '{}'""".format(
            Municipality.municode
        )
        req = requests.get(wprdc_url)
        try:
            f.write(req.text)
        except IOError as e:
            # Todo: log_error
            raise e
        print("Written {}".format(abs_path))
    return abs_path


def prop_id(parid, cursor):
    select_sql = """
        SELECT propertyid FROM public.property
        WHERE parid = %s;"""
    cursor.execute(select_sql, [parid])
    return cursor.fetchone()[0]  # property id


def unit_id(prop_id, cursor):
    select_sql = """
        SELECT unitid FROM propertyunit
        WHERE property_propertyid = %s"""
    cursor.execute(select_sql, [prop_id])
    try:
        return cursor.fetchone()[0]  # unit id
    except TypeError:
        return None

def cecase_id(prop_id, cursor):
    select_sql = """
        SELECT caseid FROM cecase
        WHERE property_propertyid = %s
        ORDER BY creationtimestamp DESC;"""
    cursor.execute(select_sql, [prop_id])
    try:
        return cursor.fetchone()[0]  # Case ID
    except TypeError:  # 'NoneType' object is not subscriptable:
        return None


def valid_json(file_name):
    with open(file_name, "r") as file:
        f = json.load(file)
        if not (f["success"] and len(f["result"]["records"]) > 0):
            # Check and see if it's a test municipality
            if file.name.startswith(os.path.join(PARCEL_ID_LISTS, "COG Land")):
                return False
            os.rename(file_name, file_name + "_corrupt")
            raise ValueError("{} not valid".format(file.name))
        return True
