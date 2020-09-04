import requests
import json
import os
from typing import List

# TODO: Refactor these imports somewhere else
#   These files should not read from each other.
#   If not refactored, this file should somehow be designated a higher level than the others
import _create
import _write
import _parse
from common import PARCEL_ID_LISTS, DEFAULT_PROP_UNIT


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
        return _parse.Municipality(*row)
    except TypeError as e:
        if row is None:
            raise TypeError(
                "The municode given as an argument likely does not exist in the database"
            )
        else:
            raise e


def muni_data_and_write_to_file(muni: _parse.Municipality):
    # Note: The WPRDC limits 50,000 parcels
    script_dir = os.path.dirname(__file__)
    rel_path = os.path.join(PARCEL_ID_LISTS, muni.name + "_parcelids.json")
    abs_path = os.path.join(script_dir, rel_path)

    with open(abs_path, "w") as f:
        wprdc_url = """https://data.wprdc.org/api/3/action/datastore_search_sql?sql=
        SELECT * FROM "518b583f-7cc8-4f60-94d0-174cc98310dc" WHERE "MUNICODE" = '{}'""".format(
            muni.municode
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
    """ Fetches a property's unit's id (the primary key) from the database. If a property does not have a unit, one is created.
    """
    select_sql = """
        SELECT unitid FROM propertyunit
        WHERE property_propertyid = %s"""
    cursor.execute(select_sql, [prop_id])
    try:
        return cursor.fetchone()[0]  # unit id
    except TypeError:
        # TODO: Raise Warning: Property exists without property unit
        _unit_id = _write.unit(
            {"unitnumber": DEFAULT_PROP_UNIT, "property_propertyid": prop_id}, cursor,
        )
        return _unit_id


def cecase_id(
    prop_id, cursor,
):
    """ Fetches a property's cecase's id from the database. If a property does not have a cecase, one is created.
    """
    select_sql = """
        SELECT caseid FROM cecase
        WHERE property_propertyid = %s
        ORDER BY creationtimestamp DESC;"""
    cursor.execute(select_sql, [prop_id])
    try:
        return cursor.fetchone()[0]  # Case ID
    except TypeError:  # 'NoneType' object is not subscriptable:
        # TODO: ERROR: Property exists without cecase
        _unit_id = unit_id(prop_id, cursor)  # Function _fetch.unit_id
        cecase_map = _create.cecase_imap(prop_id, _unit_id)
        case_id = _write.cecase(cecase_map, cursor)
        return case_id


def all_parids_in_muni(municdode, cursor) -> List[str]:
    select_sql = "SELECT parid FROM property WHERE municipality_municode = %s;"
    cursor.execute(select_sql, [municdode])
    all_parcels = cursor.fetchall()
    # Each parcel returned in all_parcels is a tuple rather than the string we want, so we unpack it.
    return [p[0] for p in all_parcels]


def valid_json(file_name):
    with open(file_name, "r") as file:
        f = json.load(file)
        if not (f["success"] and len(f["result"]["records"]) > 0):
            # Check and see if it's a test municipality
            _, tail = os.path.split(file_name)
            if tail.startswith("COG Land"):
                return False
            else:
                os.rename(file_name, file_name + "_corrupt")
                # Note: This doesn't actually work, since we get a WinError 32
                raise ValueError("{} not valid".format(file.name))
        return True
