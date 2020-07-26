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


class TestParse:
    # TODO: pickled objects to fixtures
    # We COULD parametrize these tests, but ultimately explicit is better than implicit.
    # It's not a lot of repeated code for just 4 test cases.
    class TestTaxStatus:

        def test_paid(self):
            with open(MOCKS + "paid.pickle", "rb") as p:
                data = pickle.load(p)
                soup = data["Tax"]
            assert _parse.parse_tax_from_soup(soup) ==  TaxStatus(year='2020', paidstatus='PAID', tax='473', penalty='000', interest='000', total='473', date_paid='6/2/2020', blank=None)

        def test_unpaid(self):
            with open(MOCKS + "unpaid.pickle", "rb") as p:
                data = pickle.load(p)
                soup = data["Tax"]
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus='UNPAID', tax='36894', penalty='1845', interest='369', total='39108', date_paid=None, blank=None)

        def test_balancedue(self):
            with open(MOCKS + "balancedue.pickle", "rb") as p:
                data = pickle.load(p)
                soup = data["Tax"]
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus='BALANCE DUE', tax='069', penalty='003', interest='001', total='073', date_paid=None, blank=None)

        def test_none(self):
            # Todo: Does the truely represent no taxes, or is it representative of blank data?
            with open(MOCKS + "none.pickle", "rb") as p:
                data = pickle.load(p)
                soup = data["Tax"]
            assert _parse.parse_tax_from_soup(soup) == TaxStatus(year='2020', paidstatus=None, tax='000', penalty='000', interest='000', total='000', date_paid=None, blank=None)


with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", host="localhost", port="5432") as conn:

    class TestIntegrity():
        """ These tests ensure the versions of objects created in the scripts match the version in the database.
        """

        class TestEventCategories:
            """ Ensures events in _events.py share the same attributes of their counterpart in the database.
            """

            @pytest.mark.parametrize("event", event_categories)
            def test_name(self, event):
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
            def test_active(self, event):
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


# with psycopg2.connect(database="testdb", user="sylvia", password="c0d3", host="localhost", port="5432") as conn:
#
#     class TestWrites():
#         """
#         """
#
#         class TestTaxStatus:
#             """ Tests data scraped from Allegheny County. Also compares it to the record.
#             """
#
#             def test_paid(self):
#                 pass
#                 # tax_status(year='2020', paidstatus='PAID', tax='473', penalty='000', interest='000', total='473', date_paid='6/2/2020', blank=None)
#
#             def test_unpaid(self):
#                 pass
#                 # 11
#                 # tax_status(year='2020', paidstatus='UNPAID', tax='36894', penalty='1845', interest='369', total='39108', date_paid=None, blank=None)
#
#             def test_balancedue(self):
#                 pass
#                 # 19
#                 # tax_status(year='2020', paidstatus='BALANCE DUE', tax='069', penalty='003', interest='001', total='073', date_paid=None, blank=None)
#
#             def test_none(self):
#                 # Todo: Does the truely represent no taxes, or is it representative of blank data?
#                 pass
#                 # 0
#                 # tax_status(year='2020', paidstatus=None, tax='000', penalty='000', interest='000', total='000', date_paid=None, blank=None)
#
#
#         class TestEventsTrigger:
#             """
#             """
#
#             def test_TriggerCoverage(self):
#                 """
#                 """
#                 pass
#             def test_NewParcelid_trigger(self):
#                 pass
#             def test_DifferentOwner_trigger(self):
#                 pass
#             def testDifferentStreet_trigger(self):
#                 pass
#             def testDifferentCityStateZip(self):
#                 pass
#             def testDifferentLivingArea(self):
#                 pass
#             def testDifferentCondition(self):
#                 pass
#             def testDifferentTaxStatus(self):
#                 pass
#             def testDifferentTaxCode(self):
#                 pass









if __name__ == "__main__":
    pytest.main()