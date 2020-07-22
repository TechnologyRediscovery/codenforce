import os
import psycopg2
from psycopg2 import extensions
from psycopg2.errors import *
from parcelupdate._constants import TEST_DB


here = os.path.abspath(os.path.dirname(__file__))
patch_folder = os.path.abspath(
    os.path.join(here, "..", "..", "codeconnect", "database", "patches")
)




# def from_patches():
#     # Todo: Refactor back into a function?
#     with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", port=5432) as db_conn:
#         with db_conn.cursor(name="test_cursor") as cursor:
#             patches = _get_patches()
#             for patch in patches:
#                 cursor.execute(patch)


def _alphabatize(_list):
    # Todo: Implement using quicksort?
    pass
    return []


def _get_patches():
    mega_patch = ""
    patches = [f for f in os.listdir(patch_folder) if f.startswith("dbpatch_")]
    numbers = [n.lstrip("dbpatch_beta").rstrip(".sql") for n in patches]
    print(patches)
    print(numbers)


# with psycopg2.connect(database="testdb", user="sylvia", password="c0d3", port=5432) as db_conn:
#     with db_conn.cursor(name="test_cursor") as cursor:
#
#         def test_datebase_has_matching_schema():
#             with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", port=5432) as db_conn:
#                 with db_conn.cursor(name="test_cursor") as cursor:



with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", port=5432) as conn:
    print ("type(conn):", type(conn))
    autocommit = extensions.ISOLATION_LEVEL_AUTOCOMMIT
    conn.set_isolation_level(autocommit)
    cursor = conn.cursor()
    print("Cursor opened")
    # cursor.execute(f"DROP DATABASE {TEST_DB};")

    try:
        cursor.execute(f"CREATE DATABASE {TEST_DB};")
    except DuplicateDatabase:
        print("The database wasn't cleared from last time. Dropping database and trying again.")
        cursor.execute(f"DROP DATABASE {TEST_DB};")
        cursor.execute(f"CREATE DATABASE {TEST_DB};")

    # Todo: Put this in a context manager
    try:


        class test_Stuff()

        def test_database_build_schema(base_db, test_db, cursor):
            # We don't check everything, but really, these are the only things that should go wrong.
            select_sql = """ 
            SELECT column_name, data_type, column_default, is_nullable
                FROM   information_schema.columns
                WHERE  table_name = %s
                ORDER  BY column_name;
            """
            assert cursor.execute(select_sql, [base_db]) == cursor.execute(test_db)


    finally:
        cursor.execute(f"DROP DATABASE {TEST_DB}")
        print(f"Dropped {TEST_DB}")
        cursor.close()
        print("Cursor closed.")
