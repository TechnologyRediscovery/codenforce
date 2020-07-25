# import psycopg2
# import pytest
# from tests.utils import spin_up_database
# from parcelupdate import _update_muni
# from parcelupdate._connection import connection_and_cursor
# from _constants import COG_DB, TEST_DB
#
#
#
#
# spin_up_database.from_dumped_schema()
#
#
# def test_database_build_schema():
#     select_sql = """
#             SELECT column_name, data_type, column_default, is_nullable
#                 FROM   information_schema.columns
#                 WHERE  table_name = %s
#                 ORDER  BY column_name;
#             """
#     with connection_and_cursor(database=COG_DB) as (base_conn, base_cursor):
#         with connection_and_cursor(database=TEST_DB) as (test_conn, test_cursor):
#             base_schema = base_cursor.execute(select_sql, [COG_DB])
#             test_schema = test_cursor.execute(select_sql, [TEST_DB])
#             assert base_schema == test_schema
#             print('yay')
#
#
# class test_update_muni():
#     def test_paid(self):
#         pass
#
#     def test_unpaid(self):
#         pass
#
#     def test_balance_due(self):
#         pass
#
#     def test_none_paid(self):
#         pass
# """
# Paid status = None, no tax
# 0
# tax_status(year='2020', paidstatus=None, tax='000', penalty='000', interest='000', total='000', date_paid=None, blank=None)
#
# Normal Paid
# 1
# tax_status(year='2020', paidstatus='PAID', tax='473', penalty='000', interest='000', total='473', date_paid='6/2/2020', blank=None)
#
# Unpaid
# 11
# 11
# tax_status(year='2020', paidstatus='UNPAID', tax='36894', penalty='1845', interest='369', total='39108', date_paid=None, blank=None)
#
# Balance Due
# 19
# tax_status(year='2020', paidstatus='BALANCE DUE', tax='069', penalty='003', interest='001', total='073', date_paid=None, blank=None)
# """
#
#
#
#
# # class test_events():
# #     def __init__(self):
# #         events = self.grab_events()
# #
# #         for e in events:
# #             # Dynamically create functions for testing.
# #             pass
# #
# #         def pytest_generate_test(self, metafunc):
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#     # def test_DifferentOwner(self):
#     #     pass
#     # def test_DifferentStreet(self):
#     #     pass
#     # def test_DifferentCityStateZip(self):
#     #     pass
#     # def test_DifferentLivingArea(self):
#     #     pass
#     # def test_DifferentCondition(self):
#     #     pass
#     # def test_DifferentTaxStatus(self):
#     #     pass
#     # def test_DifferentTaxCode(self):
#     #     pass
#
#     ##### PSUEDO CODE
#     pass
#     # Maybe user pytest.
#     """
#     for event in inspect.(event that derives from event):
#
#
#
#     """
#
#
#
#
#
# if __name__ == "__main__":
#     pytest.main()
#
#
#
#
