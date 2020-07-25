import sys
import pytest
from copy import copy
import psycopg2

from parcelupdate._events import *


# Generates a list of every eventcategory in events
event_categories = []
d = copy(sys.modules[__name__].__dict__)
for k in d:
    try:
        if issubclass(d[k], Event):
            if d[k].__name__ not in ("Event", "ParcelChangedEvent"):
                event_categories.append(d[k])
            continue
    except TypeError:
        continue

columns = ["title", "description", "active"]

# categories_and_columns = []
# for category in event_categories:
#     for column in columns:
#         categories_and_columns.append((category, column))
# print(categories_and_columns)



details = EventDetails(None, None, None, None)
details.changes = Changes(None, None, None)
with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", host="localhost", port="5432") as conn:

    @pytest.mark.parametrize("event", event_categories)
    def test_event_base(event):
        """ Compares the class's name to the database's event category's title.
        """
        with conn.cursor() as cursor:
            instance = event(details)
            info = {}
            info["column"] = event.__name__
            info["category_id"] = instance.category_id
            select_sql = """
                    SELECT %(column)s
                    FROM eventcategory
                    WHERE categoryid = %(category_id)s;
                    """
            cursor.execute(select_sql, info)
            row = cursor.fetchone()
            assert event.__name__ == row[0]






if __name__ == "__main__":
    pytest.main()