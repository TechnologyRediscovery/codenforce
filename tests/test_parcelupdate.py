import psycopg2
import pytest
from tests.utils import spin_up_database
from _constants import BASE_DB, TEST_DB



@pytest.fixture()
def base():
    pass

@pytest.fixture()
def


def test_database_build_schema(BASE_DB, test, cursor):
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




