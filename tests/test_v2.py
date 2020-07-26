import sys
import pytest
from copy import copy
import psycopg2
from parcelupdate._events import *
import parcelupdate as pu


# Generates a list of every eventcategory in events
from tests.utils import spin_up_database

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
with psycopg2.connect(database="cogdb", user="sylvia", password="c0d3", host="localhost", port="5432") as conn:

    class TestEvents():
        """
        """

        class TestEventCategories:
            """
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



class TestParse:
    class TestTaxStatus:
        def test_paid(self):
            pass
            # tax_status(year='2020', paidstatus='PAID', tax='473', penalty='000', interest='000', total='473', date_paid='6/2/2020', blank=None)

        def test_unpaid(self):
            pass
            # 11
            # tax_status(year='2020', paidstatus='UNPAID', tax='36894', penalty='1845', interest='369', total='39108', date_paid=None, blank=None)

        def test_balancedue(self):
            pass
            # 19
            # tax_status(year='2020', paidstatus='BALANCE DUE', tax='069', penalty='003', interest='001', total='073', date_paid=None, blank=None)

        def test_none(self):
            # Todo: Does the truely represent no taxes, or is it representive of blank data?
            pass
            # 0
            # tax_status(year='2020', paidstatus=None, tax='000', penalty='000', interest='000', total='000', date_paid=None, blank=None)



spin_up_database.from_dumped_schema()
# Todo: ATM, we are connecting multiple times for no reason. Make less stupid.
with psycopg2.connect(database="testdb", user="sylvia", password="c0d3", host="localhost", port="5432") as conn:

    class TestWrites():
        """
        """

        class TestTaxStatus:
            """ Tests data scraped from Allegheny County. Also compares it to the record.
            """

            def test_paid(self):
                pass
                # tax_status(year='2020', paidstatus='PAID', tax='473', penalty='000', interest='000', total='473', date_paid='6/2/2020', blank=None)

            def test_unpaid(self):
                pass
                # 11
                # tax_status(year='2020', paidstatus='UNPAID', tax='36894', penalty='1845', interest='369', total='39108', date_paid=None, blank=None)

            def test_balancedue(self):
                pass
                # 19
                # tax_status(year='2020', paidstatus='BALANCE DUE', tax='069', penalty='003', interest='001', total='073', date_paid=None, blank=None)

            def test_none(self):
                # Todo: Does the truely represent no taxes, or is it representative of blank data?
                pass
                # 0
                # tax_status(year='2020', paidstatus=None, tax='000', penalty='000', interest='000', total='000', date_paid=None, blank=None)


        class TestEventsTrigger:
            """
            """

            def test_TriggerCoverage(self):
                """
                """
                pass
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









if __name__ == "__main__":
    pytest.main()