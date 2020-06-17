import psycopg2
from collections import namedtuple
from contextlib import contextmanager

@ contextmanager
def get_cursor():
    """
    Context manager that connects to the database, yields a cursor, and automatically closes connections.

    Use:
    >>> with get_cursor() as cursor:
    >>>     insert_sql = "INSERT INTO example_table VALUE %(example_value)"
    >>>     insertmap = {}
    >>>     insertmap['example_value'] = 42
    >>>     cursor.execute(insert_sql, insert_map)
    """
    try:
        DB_Conn = namedtuple('DatabaseConnection', ['db_conn', 'Cursor'])
        db_conn = psycopg2.connect(
            database="cogdb", user="sylvia", password="c0d3", host="localhost"
        )
        cursor = db_conn.cursor()
        yield cursor
    finally:
        cursor.close()
        db_conn.close()

