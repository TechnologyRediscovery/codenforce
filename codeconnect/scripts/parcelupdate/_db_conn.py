import psycopg2
from contextlib import contextmanager
from _constants import BOT_NAME


@contextmanager
def get_db_and_cursor(
    database="cogdb",
    user=BOT_NAME,
    password="c0d3",
    host="localhost",
    port=5432,
    v=False,
):  # Todo: Add more verbosity
    """
    Context manager that connects to the database, yields a cursor, and automatically closes connections.

    Returns:
        DBConnAndCursor

    Usage:
    >>> with get_db_and_cursor() as db_cursor:
    >>>     insert_sql = "INSERT INTO example_table VALUE %(example_value)"
    >>>     insertmap = {}
    >>>     insertmap['example_value'] = 42
    >>>     cursor.execute(insert_sql, insert_map)
    >>>     cursor.commit()

        Author's note:
            This context manager works exactly how I want:
                The yeilded object has the attributes/methods of db_conn and cursor, and
                everything closes automatically.
            However, the implementation is a little whack.
            I'm always looking to learn: make a pull request if you have a more straightforward implementation!
    """
    # Todo: Option to read in username/password from secrets.json
    try:
        db_conn = psycopg2.connect(
            database=database, user=user, password=password, host=host, port=port
        )
        cursor = db_conn.cursor()

        class CursorAndDB_Conn:
            """ Contains the attributes of psycopg2.extensions.connection and psycopg2.extensions.cursor"""

            def __init__(self):
                # self.add_obj_attrs_to_instance(db_conn)
                self.add_obj_attrs_to_instance(cursor)
                self.add_obj_attrs_to_instance(db_conn)
                if v:
                    print("Connected to database", database, "as user", user)

            def add_obj_attrs_to_instance(self, obj):
                for attr in obj.__dir__():
                    try:
                        setattr(self, attr, getattr(obj, attr))
                    except TypeError:  # __class__ assignment only supported for heap types or ModuleType subclasses
                        continue

        yield CursorAndDB_Conn()
    finally:
        if v:
            print("Cursor And DB Connection", database, "closed.")
        cursor.close()
        db_conn.close()
