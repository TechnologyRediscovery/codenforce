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
# Todo: Get auto-commit working for new IDE. Have tests run automatically on commit

import sys
import pytest
from copy import copy
import psycopg2
from os import path
from parcelupdate._events import *
from parcelupdate import _parse
from parcelupdate._parse import TaxStatus
import pickle


### Fixtures (and similar bits of setup code)

HERE = path.abspath(path.dirname(__file__))
MOCKS = path.join(HERE, "mocks", "")    # Represents the mocks folder

# Generates a list of every eventcategory class in _events
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

details = EventDetails(None, None, None, None)
details.changes = Changes(None, None, None)

# Todo: During this setup phase, add CogLand objects into local db


class TestEventsTrigger:
    """
    """
    def test_NewParcelid_trigger(self):
        pass

    def test_DifferentOwner_trigger(self):
        pass

    def testDifferentStreet_trigger(self):
        pass

    def testDifferentCityStateZip(self):
        pass

    def testDifferentLivingArea(self):
        pass

    def testDifferentCondition(self):
        pass

    def testDifferentTaxStatus(self):
        pass

    def testDifferentTaxCode(self):
        pass



class TestParse:
    # Todo: pickled objects to fixtures?
    class TestParseTaxFromSoup:
        def test_paid(self):
            with open(MOCKS + "paid.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus='PAID', tax='473', penalty='000', interest='000', total='473', date_paid='6/2/2020', blank=None)

        def test_unpaid(self):
            with open(MOCKS + "unpaid.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus='UNPAID', tax='36894', penalty='1845', interest='369', total='39108', date_paid=None, blank=None)

        def test_balancedue(self):
            with open(MOCKS + "balancedue.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus='BALANCE DUE', tax='069', penalty='003', interest='001', total='073', date_paid=None, blank=None)

        def test_none(self):
            # Todo: Does the truely represent no taxes, or is it representative of blank data?
            with open(MOCKS + "none.pickle", "rb") as p:
                soup = pickle.load(p)
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus=None, tax='000', penalty='000', interest='000', total='000', date_paid=None, blank=None)


    class TestParseOwnerFromSoup:
        pass


with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", host="localhost", port="5432") as conn:

    class TestWrites():
        """
        These tests ensure that the code write to the database properly.

        They write to a local copy of the cogdb database.
        Changes to the database persist between tests, so these are not "pure" unittests.
        """
        def test_create_property_imap(self):
            pass

        def test_write_property(self):
            pass

        def test_create_cecase_imap(self):
            pass

        def test_write_cecase(self):
            pass

        def test_create_owner_imap(self):
            pass

        def test_write_person(self):
            pass

        def test_connect_prop_to_person(self):
            pass

        def test_create_tax_status_imap(self):
            pass

        def test_write_tax_status(self):
            pass

        def test_create_propertyexternaldataimap(self):
            pass

        def test_write_propertyexternaldata(self):
            pass


    class TestIntegrity():
        """
        These tests ensure the versions of objects created in the scripts match the version in the database.

        They read from a local copy of the cogdb database.
        """
        class TestEventCategories:
            """ Ensures events in _events.py share the same attributes of their counterpart in the database.
            """

            @pytest.mark.parametrize("event", event_categories)
            def test_name_integrity(self, event):
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


            # Todo: Refactor and do a little currying
            @pytest.mark.parametrize("event", event_categories)
            def test_active_integrity(self, event):
                """ Compares the class's default active status to the database's.
                """
                with conn.cursor() as cursor:
                    instance = event(details)
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