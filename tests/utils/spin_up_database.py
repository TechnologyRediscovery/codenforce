import os
from os import path
import psycopg2
from psycopg2 import extensions
from psycopg2.errors import *
from contextlib import contextmanager
import re

from _connection import connection_and_cursor
from parcelupdate._constants import COG_DB, TEST_DB, BOT_NAME


here = path.abspath(path.dirname(__file__))
patch_folder = path.abspath(
    path.join(here, "..", "..", "codeconnect", "database", "patches")
)

########################################################################################
# Human sort: https://nedbatchelder.com/blog/200712/human_sorting.html
########################################################################################
def _tryint(s):
    try:
        return int(s)
    except ValueError:
        return s

def _alphanum_key(s):
    """ Turn a string into a list of string and number chunks.
        "z23a" -> ["z", 23, "a"]
    """
    return [ _tryint(c) for c in re.split('([0-9]+)', s) ]

def sort_nicely(l):
    """ Sort the given list in the way that humans expect.
    """
    return sorted(l, key=_alphanum_key)
########################################################################################
########################################################################################

def _order_patches():
    return sort_nicely([f for f in os.listdir(patch_folder) if f.startswith("dbpatch_b")])
    #   Note:   The alphabetical sorting correctly places dbpatch_beta2➕.sql after
    #           dbpatch_beta2.sql and 'dbpatch_beta_RUN_ME_LAST at the end.

def write_aggregate_patch():
    patches = _order_patches()
    with open(path.join(patch_folder, "dbpatch_aggregate.sql"), "w", encoding="utf-8") as f:
        for patch in patches:
            p = open(path.abspath(path.join(patch_folder, patch)), "r")
            f.write(f"-- {p.name}\n")
            f.writelines(p.readlines())
            f.write("\n")
            p.close()

write_aggregate_patch()






# with psycopg2.connect(database="testdb", user="sylvia", password="c0d3", port=5432) as db_conn:
#     with db_conn.cursor(name="test_cursor") as cursor:
#
#         def test_datebase_has_matching_schema():
#             with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", port=5432) as db_conn:
#                 with db_conn.cursor(name="test_cursor") as cursor:



# with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", port=5432) as conn:
#     print ("type(conn):", type(conn))
#     autocommit = extensions.ISOLATION_LEVEL_AUTOCOMMIT
#     conn.set_isolation_level(autocommit)
#     cursor = conn.cursor()
#     print("Cursor opened")
#     # cursor.execute(f"DROP DATABASE {TEST_DB};")
#
#     try:
#         cursor.execute(f"CREATE DATABASE {TEST_DB};")
#     except DuplicateDatabase:
#         print("The database wasn't cleared from last time. Dropping database and trying again.")
#         cursor.execute(f"DROP DATABASE {TEST_DB};")
#         cursor.execute(f"CREATE DATABASE {TEST_DB};")
#
#     # Todo: Put this in a context manager
#     try:
#
#
#         class test_Stuff()
#
#         def test_database_build_schema(base_db, test_db, cursor):
#             # We don't check everything, but really, these are the only things that should go wrong.
#             select_sql = """
#             SELECT column_name, data_type, column_default, is_nullable
#                 FROM   information_schema.columns
#                 WHERE  table_name = %s
#                 ORDER  BY column_name;
#             """
#             assert cursor.execute(select_sql, [base_db]) == cursor.execute(test_db)
#
#
#     finally:
#         cursor.execute(f"DROP DATABASE {TEST_DB}")
#         print(f"Dropped {TEST_DB}")
#         cursor.close()
#         print("Cursor closed.")


def from_patches(patches, **kwargs):
    def test_database_build_schema(base, test, cursor):
        select_sql = """
        SELECT column_name, data_type, column_default, is_nullable
            FROM   information_schema.columns
            WHERE  table_name = %s
            ORDER  BY column_name;
        """
        with connection_and_cursor(database=COG_DB) as (base_conn, base_cursor):
            with connection_and_cursor(database=TEST_DB) as (test_conn, test_cursor):

                base_db = base_cursor.execute(select_sql, [base])
                test_db = test_cursor.execute(select_sql, [test])
                if base_db == test_db:
                    return True
                return False

    with connection_and_cursor(port=5432) as (conn, cursor):
        pass

