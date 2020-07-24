import os
from os import path
import datetime
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT # <-- ADD THIS LINE
from psycopg2.errors import *
from contextlib import contextmanager
import re

from _connection import connection_and_cursor
from parcelupdate._constants import BASE_DB, TEST_DB, BOT_NAME


HERE = path.abspath(path.dirname(__file__))
DATABASE_FOLDER = path.abspath(
    path.join(HERE, "..", "..", "codeconnect", "database")
)
PATCH_FOLDER = path.abspath(
    path.join(DATABASE_FOLDER, "patches")
)
SCHEMA_FOLDER = path.abspath(
    path.join(DATABASE_FOLDER, "schema_dumps")
)
AGGREGATE = "dbpatch_aggregate.sql"

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
    return sort_nicely([f for f in os.listdir(PATCH_FOLDER) if f.startswith("dbpatch_b")])
    #   Note:   The alphabetical sorting correctly places dbpatch_beta2➕.sql after
    #           dbpatch_beta2.sql and 'dbpatch_beta_RUN_ME_LAST at the end.

def write_aggregate_patch():
    patches = _order_patches()
    with open(path.join(PATCH_FOLDER, AGGREGATE), "w", encoding="utf-8") as f:
        f.write(f"-- Aggregate patch written on {datetime.datetime.now()}\n")
        for patch in patches:
            p = open(path.abspath(path.join(PATCH_FOLDER, patch)), "r")
            f.write(f"-- {path.basename(p.name)}\n")
            f.writelines(p.readlines())
            # for line in p.readlines():
            #     if line.upper().startswith("BEGIN;"):
            #         continue
            #     if line.upper().startswith("COMMIT;"):
            #         continue
            #     f.write(f"{line}\n")

            p.close()

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


# def from_patches(**kwargs):
#     """ Writes an empty database from the patches for testing.
#     """
#     write_aggregate_patch()
#     with open(path.join(PATCH_FOLDER, AGGREGATE), "r") as f:
#         sql = "".join(f.readlines())
#     with connection_and_cursor(port=5432) as (conn, cursor):
#         conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
#         try:
#             cursor.execute("CREATE DATABASE testdb;")
#             print("Created new testdb")
#         except DuplicateDatabase:
#             cursor.execute("DROP DATABASE testdb;")
#             print("Dropped old testdb")
#             cursor.execute("CREATE DATABASE testdb;")
#             print("Created new testdb")
#     with connection_and_cursor(port=5432, database=TEST_DB) as (t_conn, test_cursor):
#         test_cursor.execute(sql)

        # try:
        #     cursor.execute("DROP DATABASE testdb;")
        #     print("Dropped table")
        # except InvalidCatalogName as e:
        #     print(e)
        # conn.autocommit = True
        # auto_cursor = conn.cursor()
        # auto_cursor.execute(sql)

# from_patches()
# write_aggregate_patch()
# with open(path.join(PATCH_FOLDER, AGGREGATE), "r") as f:
#     sql = "".join(f.readlines())
# with connection_and_cursor(port=5432) as (conn, cursor):
#     cursor.execute(sql)

# TODO: Automatically update schema name. Write a shell script that executes pg_dump.
SCHEMA_FILE = path.join(SCHEMA_FOLDER, "2020.24.07_cogdb_schema.sql")

def from_dumped_schema():
    with open(SCHEMA_FILE, "r") as schema:
        sql = "".join(schema.readlines())
        with connection_and_cursor(port=5432) as (conn, cursor):
            conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
            try:
                cursor.execute("CREATE DATABASE testdb;")
                print("Created new testdb")
            except DuplicateDatabase:
                cursor.execute("DROP DATABASE testdb;")
                print("Dropped old testdb")
                cursor.execute("CREATE DATABASE testdb;")
                print("Created new testdb")
    with connection_and_cursor(port=5432, database=TEST_DB) as (t_conn, test_cursor):
        test_cursor.execute(sql)
        print("Filled in test schema")

