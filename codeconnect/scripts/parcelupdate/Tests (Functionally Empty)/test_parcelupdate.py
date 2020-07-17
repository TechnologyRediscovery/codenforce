import parcelupdate as pu
from parcelupdate._db_conn import get_db_and_cursor
import unittest


class ParcelUpdateTestCase(unittest.TestCase):
    def test_clean_multiple_names(self) -> None:
        #
        #   Particularly interesting parcels to check:
        #       2 names, one has 2 spaces
        #       "0082R00080000000": "{"SOFFIETTI NIA BATON &  SOFFIETTI   ANDREW BATON (H)   ","WOLFE JUDITH "}"
        #
        #       3 names
        #       "0123N00292000000", "{"MUDD LOUIS H     ","GOWDER MARVA N","WILLIAMS SHELTON "}"
        with get_db_and_cursor(v=True) as db_cursor:
            select_sql = """
                    select property.parid, person.fullname from person 
                        inner join propertyperson on (person.personid = propertyperson.person_personid)
                        inner join property on (propertyperson.property_propertyid = property.propertyid)
                    where muni_municode = 111
                    and fullname like '{"%'
                    and person.lastupdated > timestamp '2020-07-12 09:11:01.273762-07'
                    """
            db_cursor.execute(select_sql)
            x = db_cursor.fetchone()
            print(x)
            return x

            # pu._scrape_and_parse

    def test_second(self):
        return True


if __name__ == "__main__":
    global db_cursor
    with get_db_and_cursor(v=True) as db_cursor:
        unittest.main(
            module="test_parcelupdate", verbosity=2,
        )
