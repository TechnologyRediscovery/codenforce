"""
Unit tests for the parcelupdate package.

🛑 Under no circumstance should tests connect to the production database. 🛑

The test suite assumes you have an up to date local copy of cogdb database and writes test_data to the copy.
The database (as of August 2020) is not very large, so a subset of the data is not provided.
A link to database dumps can be provided to interested contributors.
"""

#   A note on multi-leveled classes.
#   Some test classes may only contain a single test class.
#   This is intentional:
#       It allows for more test classes to be added under the umbrella of an outer class without refactoring

# Todo: Custom Black config for 3 lines after module level classes, or just ignore file.
# Todo: Get pre-commit working for new IDE. Have tests run automatically on commit

import sys
import pytest
from copy import copy
import psycopg2
from os import path
from contextlib import contextmanager
import functools
from pyparcel._events import *
from pyparcel import _parse, _events
from pyparcel import _write as write
from pyparcel._parse import TaxStatus
from unittest import mock
from typing import NamedTuple
import pickle


### Fixtures (and similar bits of setup code)

HERE = path.abspath(path.dirname(__file__))
PICKLES = path.join(HERE, "pickles", "")  # Represents the mocks folder

# Generates a list of every eventcategory class in _events
event_categories = []
d = copy(sys.modules[__name__].__dict__)
for k in d:
    try:
        if issubclass(d[k], Event):
            # Skip over base classes
            if d[k].__name__ not in ("Event", "ParcelChangedEvent"):
                event_categories.append(d[k])
            continue
    except TypeError:
        continue


class MockedCursor(mock.MagicMixin):
    def __init__(
            self,
            new_owner=False,
            new_street=False,
            new_citystatezip=False,
            new_livingarea=False,
            new_condition=False,
    ):
        self.old_owner = '{"OWNER OLD     "}'
        self.old_street = "0 Old St "
        self.old_citystatezip = "PITTSBURGH PA 15206"
        self.old_livingarea = 1000
        self.old_condition = 4

        self.new_owner = '{"OWNER NEW     "}' if new_owner else self.old_owner
        self.new_street = "1 New St " if new_street else self.old_street
        self.new_citystatezip = "NEWSCITY PA 15090" if new_citystatezip else self.old_citystatezip
        self.new_livingarea = 2345 if new_livingarea else self.old_livingarea
        self.new_condition = 1 if new_condition else self.old_condition

    def execute(self, *args, **kwargs):
        return None

    def fetchall(self):
        return (
            [
                self.old_owner,
                self.old_street,
                self.old_citystatezip,
                self.old_livingarea,
                self.old_condition,
            ],
            [
                self.new_owner,
                self.new_street,
                self.new_citystatezip,
                self.new_livingarea,
                self.new_condition,
            ],
        )

    def fetchone(self):
        return [True]


class EventAndCursor(NamedTuple):
    event: _events.Event
    cursor: MockedCursor
EnC = EventAndCursor


# Todo: There is a lot of duplicate code. Refactor:
#   When creating a new event propertyexternaldata event, you should only have to add it once here
#   It should accept:
#       the event class,
#       a string to be passed to the MockedCursor as an attribute (Example: new_yourevent)
#       Starting data (old) of the correct type (since it will be tested by writing to a database)
#       Changed data (new)
#   Obviously EventAndCursor will need to be changed too.
def _propertyexternaldata_events_and_corresponding_cursors():
    """
    A fixture constructor.

    Contains all events

    Returns: A list of EventAndCursor tuples.
        Each tuple contains
            An event that can be triggered by the function _events.query_propertyexternaldata_for_changes
            A (mocked) cursor used to trigger passed to the event's EventDetails.
    """
    return [
        EnC(DifferentOwner, MockedCursor(new_owner=True)),
        EnC(DifferentStreet, MockedCursor(new_street=True)),
        EnC(DifferentCityStateZip, MockedCursor(new_citystatezip=True)),
        EnC(DifferentLivingArea, MockedCursor(new_livingarea=True)),
        EnC(DifferentCondition, MockedCursor(new_condition=True))
    ]


@pytest.fixture
def propertyexternaldata_events():
    return [_EnC.event for _EnC in _propertyexternaldata_events_and_corresponding_cursors()]


# Todo: During this setup phase, add CogLand objects into local db
@pytest.fixture
def taxstatus_paid():
    return TaxStatus(
        year="2020",
        paidstatus="PAID",
        tax="473",
        penalty="000",
        interest="000",
        total="473",
        date_paid="6/2/2020",
    )


@pytest.fixture
def taxstatus_unpaid():
    return TaxStatus(
        year="2020",
        paidstatus="UNPAID",
        tax="36894",
        penalty="1845",
        interest="369",
        total="39108",
        date_paid=None,
    )


@pytest.fixture
def taxstatus_balancedue():
    return TaxStatus(
        year="2020",
        paidstatus="BALANCE DUE",
        tax="069",
        penalty="003",
        interest="001",
        total="073",
        date_paid=None,
    )


@pytest.fixture
def taxstatus_none():
    return TaxStatus(
        year="2020",
        paidstatus=None,
        tax="000",
        penalty="000",
        interest="000",
        total="000",
        date_paid=None,
    )


@pytest.fixture
def person1_prop_imap():
    with open(PICKLES + "person1_prop_imap.pickle", "rb") as p:
        return pickle.load(p)


@pytest.fixture
def person1_cecase_imap():
    with open(PICKLES + "person1_cecase_imap.pickle", "rb") as p:
        return pickle.load(p)


@pytest.fixture
def person1_owner_imap():
    with open(PICKLES + "person1_owner_imap.pickle", "rb") as p:
        return pickle.load(p)


@pytest.fixture
def person1_propertyexternaldata_imap():
    with open(PICKLES + "person1_propertyexternaldata_imap.pickle", "rb") as p:
        return pickle.load(p)

class TestEventTriggers:
    """ These tests ensure that an event calls write_to_db when it is supposed to
    """

    @pytest.mark.parametrize(
        "event,mocked_cursor",
        _propertyexternaldata_events_and_corresponding_cursors()
    )
    def test_propertyexternaldata(self, event, mocked_cursor):
        """ Tests events resulting from function _events.query_propertyexternaldata_for_changes_and_write_events are raised correctly.
        """
        event.write_to_db = mock.MagicMock(spec=True)
        query_propertyexternaldata_for_changes_and_write_events(
            parid=None,
            prop_id=None,
            cecase_id=None,
            new_parcel=None,
            db_cursor=mocked_cursor
        )
        event.write_to_db.assert_called_once()
        event.write_to_db.reset_mock()


    def test_multiple_propertyexternaldata_events(self, propertyexternaldata_events):
        """ This test ensures that a cursor triggering multiple events actually result in writing multiple events.
        """
        mocked_cursor = MockedCursor(
            new_owner=True,
            new_street=True,
            new_citystatezip=True,
            new_livingarea=True,
            new_condition=True
        )
        query_propertyexternaldata_for_changes_and_write_events(
            parid=None,
            prop_id=None,
            cecase_id=None,
            new_parcel=None,
            db_cursor=mocked_cursor
        )
        for event in propertyexternaldata_events:
            event.write_to_db.assert_called_once()
            event.write_to_db.reset_mock()


class TestParse:
    class TestParseTaxFromSoup:
        """ Assert parse_tax_from_soup returns the correct TaxStatus, given a BeautifulSoup object
        """

        def test_paid(self, taxstatus_paid):
            with open(PICKLES + "paid.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_paid

        def test_unpaid(self, taxstatus_unpaid):
            with open(PICKLES + "unpaid.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_unpaid

        def test_balancedue(self, taxstatus_balancedue):
            with open(PICKLES + "balancedue.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_balancedue

        def test_none(self, taxstatus_none):
            # Todo: Does the truely represent no taxes, or is it representative of blank data?
            with open(PICKLES + "none.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == taxstatus_none

    class TestParseOwnerFromSoup:
        pass


try:
    conn = psycopg2.connect(
        database="cogdb", user="sylvia", password="c0d3", host="localhost", port="5432"
    )
except psycopg2.OperationalError:
    conn = mock.MagicMock()
    warnings.warn("A database connection could not be established. Skipping tests that require a connection...")


with conn:

    def transaction(func):
        """ transaction is a decorator that allows each unittest to be run in its own transaction
        """

        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            cursor = conn.cursor()
            try:
                cursor.execute("BEGIN;")
                func(*args, **kwargs)
            finally:
                cursor.execute("ROLLBACK;")
                cursor.close()

        return wrapper

    def db_connection_established():
        """ db_connection_established is a flag representing if a database connection could be made.
        """
        if isinstance(conn, psycopg2.extensions.connection):
            return True


    @pytest.mark.skipif(
        not db_connection_established(), reason="Requires a database connection"
    )
    class TestsRequiringADatabaseConnection:
        class TestWrites:
            """ TestWrites tests check that the code write to the database properly.
            """

            @transaction
            def test_property(self, person1_prop_imap):
                with conn.cursor() as cursor:
                    write.property(person1_prop_imap, cursor)

            # # Requires a property id
            # @transaction
            # def test_unit(self):
            #     with conn.cursor() as cursor:
            #         write.unit(unit_imap, cursor)
            #
            # @transaction
            # def test_person(self, person1_owner_imap):
            #     with conn.cursor() as cursor:
            #         write.person(person1_owner_imap, cursor)
            #
            # # def test_connect_property_to_person(self):
            # #     with conn.cursor() as cursor:
            # #         write.connect_property_to_person(prop_id, person_id, cursor)
            #
            # def test_taxstatus(self):
            #     with conn.cursor() as cursor:
            #         write.taxstatus(tax_status, cursor)
            #
            # @transaction
            # def test_propertyexternaldata(self, person1_propertyexternaldata_imap):
            #     with conn.cursor() as cursor:
            #         write.propertyexternaldata(person1_propertyexternaldata_imap, cursor)


        class TestEventCategories:
            """ Ensures events in _events.py share the same attributes of their counterpart in the database.
            """

            @pytest.mark.parametrize("event", event_categories)
            def test_name_integrity(self, event):
                """ Compares the class's name to the database's event category's title.
                """
                with conn.cursor() as cursor:
                    instance = event(mock.MagicMock())
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

            # Todo: Refactor
            @pytest.mark.parametrize("event", event_categories)
            def test_active_integrity(self, event):
                """ Compares the class's default active status to the database's.
                """
                with conn.cursor() as cursor:
                    instance = event(mock.MagicMock())
                    info = {}
                    info["column"] = instance.active
                    info["category_id"] = instance.category_id
                    select_sql = """
                            SELECT %(column)s
                            FROM eventcategory
                            WHERE categoryid = %(category_id)s;
                            """
                    cursor.execute(select_sql, info)
                    row = cursor.fetchone()
                    assert instance.active == row[0]


if __name__ == "__main__":
    pytest.main()
