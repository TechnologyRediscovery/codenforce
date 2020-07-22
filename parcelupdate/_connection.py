import psycopg2
from contextlib import contextmanager
from _constants import COG_DB, BOT_NAME

@contextmanager
def connection_and_cursor(
        database=COG_DB,
        user=BOT_NAME,
        password="c0d3",
        host="localhost",
        port=5432,
        cursor_name="cursor"
):
    try:
        conn = psycopg2.connect(
            database=database, user=user, password=password, host=host, port=port
        )
        cursor = conn.cursor(name=cursor_name)
        yield conn, cursor
    finally:
        cursor.close()
        conn.close()