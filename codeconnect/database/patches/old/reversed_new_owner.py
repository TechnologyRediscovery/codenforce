"""
A one-off script to fix reversed owner descriptions in OwnerChanged events

Example:
    Parcel 0111A00111000000's owner name changed from {"PERSON B     "} to {"PERSON A     "}
    should instead be
    Parcel 0111A00111000000's owner name changed from {"PERSON A     "} to {"PERSON B     "}
"""
# Why are there so many comments? Comments help me think out what I am doing.

import psycopg2
import click
import re


name = re.compile(r'(.*?)({".*?"}) to ({"(.*?)"})')


def reverse_names(eventdescription):
    # Get names
    g = re.search(name, eventdescription)
    # Replace names
    return g.group(1) + g.group(3) + " to " + g.group(2)


def main():
    conn = psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", port=5432)
    with conn:
        with conn.cursor() as cursor:

            select_sql = """
                select * from event
                where creationts is not null
                and eventid >= 15977
                and category_catid = 301
                order by lastupdatedts desc;
                """
            cursor.execute(select_sql)
            selection = cursor.fetchall()

            for row in selection:
                eventid = row[0]
                eventdescription = row[4]

                # Transform description
                fixeddescription = reverse_names(eventdescription)
                update_sql = """
                    UPDATE EVENT
                    SET eventdescription = %(fixed)s,
                    lastupdatedts = now()
                    WHERE eventid = %(id)s
                """
                cursor.execute(update_sql, {"fixed": fixeddescription, "id": eventid})


if __name__ == "__main__":
    main()
