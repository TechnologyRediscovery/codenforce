import psycopg2
import pytest
from tests.utils import spin_up_database
from parcelupdate import _update_muni
from parcelupdate._connection import connection_and_cursor
from _constants import COG_DB, TEST_DB




spin_up_database.from_dumped_schema()


def test_database_build_schema():
    select_sql = """
            SELECT column_name, data_type, column_default, is_nullable
                FROM   information_schema.columns
                WHERE  table_name = %s
                ORDER  BY column_name;
            """
    with connection_and_cursor(database=COG_DB) as (base_conn, base_cursor):
        with connection_and_cursor(database=TEST_DB) as (test_conn, test_cursor):
            base_schema = base_cursor.execute(select_sql, [COG_DB])
            test_schema = test_cursor.execute(select_sql, [TEST_DB])
            assert base_schema == test_schema
            print('yay')


def test_updatemuni():
    _update_muni.update_muni(

    )



if __name__ == "__main__":
    pytest.main()




