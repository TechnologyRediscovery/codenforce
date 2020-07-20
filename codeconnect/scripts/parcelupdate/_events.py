from collections import namedtuple
import _fetch as fetch
import _insert as insert
from colorama import init
init()
from colorama import Fore, Back, Style

from _constants import DEFAULT_PROP_UNIT
from _constants import BOT_ID


# These structures help organize the creation of events. See check_for_changes_and_write_events
Changes = namedtuple("flag", ["name", "orig", "new"])
class EventDetails:     # Unfortenetly, eventdetails has to be a class (instead of a tuple) because self.changes mutates
    __slots__ = ["parid", "prop_id", "changes", "db_cursor"]
    def __init__(self, parid, prop_id, db_cursor):
        self.parid = parid
        self.prop_id = prop_id
        self.db_cursor = db_cursor  # Although it technically isn't an event detail, passing the db_cursor makes life easier
        self.changes = None

def check_for_changes_and_write_events(parid, prop_id, new_parcel, db_cursor):
    """ Checks if parcel info is different from last time. Records Changes. """
    select_sql = """
        SELECT
            property_propertyid, ownername, address_street, address_citystatezip,
            livingarea, condition, taxstatus, taxcode
        FROM public.propertyexternaldata
        WHERE property_propertyid = %(prop_id)s
        ORDER BY lastupdated DESC
        LIMIT 2;
    """
    details = EventDetails(parid, prop_id, db_cursor)
    db_cursor.execute(select_sql, {"prop_id": prop_id})
    selection = db_cursor.fetchall()
    old = selection[0]
    try:
        new = selection[1]
    except IndexError:  # If this is the first time the property_propertyid occurs in propertyexternaldata
        print("First time parcel has appeared in propertyexternaldata")
        if not new_parcel:
            # TODO: Add flag
            print(
                "Error: Parcel appeared in public.propertyexternaldata for the first time even though the parcel ID is flagged as appearing in public.property before."
            )
            NewParcelid(details).write_to_db()
        return

    if old[0] != new[0]:
        details.changes = Changes("owner name", old[0], new[0])
        DifferentOwner(details).write_to_db()
    if old[1] != new[1]:
        details.changes = Changes("street", old[1], new[1])
        DifferentStreet(details).write_to_db()
    if old[2] != new[2]:
        details.changes = Changes("city, state, or zipcode", old[2], new[2])
        DifferentCityStateZip(details).write_to_db()
    if old[3] != new[3]:
        details.changes = Changes("living area size", old[3], new[3])
        DifferentLivingArea(details).write_to_db()
    if old[4] != new[4]:
        details.changes = Changes("condition", old[4], new[4])
        DifferentCondition(details).write_to_db()
    if old[5] != new[5]:
        details.changes = Changes("tax status", old[5], new[5])
        DifferentTaxStatus(details).write_to_db()
    if old[6] != new[6]:
        details.changes = Changes("tax code", old[6], new[6])
        DifferentTaxCode(details).write_to_db()



# For simplicity sake, EVENTS ARE CURRENTLY ONLY FOR PROPERTY INFO CASES
# THE EVENT CLASS REQUIRES AN OVERHAUL FOR USE WITH PERSON INFO CASES
class Event:
    """ Abstract base class for all events. """

    def __init__(self, db_cursor, prop_id=None, parid=None, unit_id=None):
        self.prop_id = prop_id
        self.parid = parid
        self.unit_id = unit_id
        self.default_unit = DEFAULT_PROP_UNIT

        if not self.prop_id:
            self.prop_id = fetch.propid(self.parid, db_cursor)

        self.ce_caseid = self._get_cecase_id(db_cursor)  # Uses self.prop_id

    def write_to_db(self):
        """ Writes an event to the database. If necessary, it also writes a cecase. """

        if self.ce_caseid is None:
            self.unit_id = self._get_unitid(self.db_cursor)
            if not self.unit_id:
                self.unit_id = insert.unit(
                    {
                        "unitnumber": DEFAULT_PROP_UNIT,
                        "property_propertyid": self.prop_id,
                    },
                    self.db_cursor,
                )
            # Side effect: Modifies the attributes of self to be read by the next func
            self._create_cecase_dunder_dict(prop_id=self.prop_id, unit_id=self.unit_id)
            self.ce_caseid = insert.cecase(self.__dict__, self.db_cursor)
        self._write_event_dunder_dict()
        self.event_id = self._write_event_to_db(self.db_cursor)  # uses self.ce_caseid
        print(Fore.RED, self.eventdescription, sep="")
        print(Style.RESET_ALL, end="")

    def _get_cecase_id(self, db_cursor):
        select_sql = """
            SELECT caseid FROM cecase
            WHERE property_propertyid = %s
            ORDER BY creationtimestamp DESC;"""
        db_cursor.execute(select_sql, [self.prop_id])
        try:
            return db_cursor.fetchone()[0]  # Case ID
        except TypeError:  # 'NoneType' object is not subscriptable:
            return None

    def _create_cecase_dunder_dict(self, prop_id, unit_id):
        # TODO: This is essentially a duplicate function (create.cecase_imap)
        # These should be filled out by the subclass
        assert self.casename
        assert self.ce_notes
        assert self.category_id
        self.category_catid = self.category_catid

        self.cecasepubliccc = 111111
        self.property_propertyid = prop_id
        self.propertyunit_unitid = unit_id
        self.login_userid = BOT_ID
        self.casename = self.casename
        self.casephase = None
        self.paccenabled = False
        self.allowuplinkaccess = None
        self.propertyinfocase = True
        self.personinfocase_personid = None
        self.bobsource_sourceid = None
        self.active = True
        self.category_catid = self.c

    def _get_unitid(self, db_cursor):
        if self.unit_id:
            return self.unit_id
        select_sql = """
            SELECT unitid FROM propertyunit
            WHERE property_propertyid = %s"""
        db_cursor.execute(select_sql, [self.prop_id])
        try:
            return db_cursor.fetchone()[0]  # unit id
        except TypeError:
            return None

    def _write_event_dunder_dict(self):
        assert self.category_id or self.category_catid
        assert self.description or self.eventdescription
        assert self.active
        assert self.event_notes

        self.cecase_caseid = self.ce_caseid
        self.creator_userid = BOT_ID
        self.lastupdatedby_userid = BOT_ID
        self.occperiod_periodid = None

        try:
            self.eventdescription
        except AttributeError:
            self.eventdescription = self.description

        try:
            self.category_catid
        except AttributeError:
            self.category_catid = self.category_id

    def _write_event_to_db(self, db_cursor):
        insert_sql = """
            INSERT INTO event(
                eventid, category_catid, cecase_caseid, creationts,
                eventdescription, creator_userid, active, notes,
                occperiod_periodid, timestart, timeend, lastupdatedby_userid,
                lastupdatedts
            )
            VALUES(
                DEFAULT, %(category_catid)s, %(cecase_caseid)s, now(),
                %(eventdescription)s, %(creator_userid)s, %(active)s, %(event_notes)s,
                %(occperiod_periodid)s, now(), now(), %(lastupdatedby_userid)s,
                now()
            )
            RETURNING eventid;"""
        db_cursor.execute(insert_sql, self.__dict__)
        return db_cursor.fetchone()[0]


class NewParcelid(Event):
    """ Indicates a parcel id was added to the database that wasn't in the database before. """

    def __init__(self, parid, prop_id, db_cursor):
        super().__init__(db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 300
        self.description = "Parcel {} was added.".format(self.parid)
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occperiod_periodid = None


# If class was updated:
# TODO: As I worked, I realized they were all the same. Thus, these should all subclass from the same thing. Make pretty later


class DifferentOwner(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 301
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentStreet(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 302
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentCityStateZip(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 303
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentLivingArea(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 304
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentCondition(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 305
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentTaxStatus(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 306
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentTaxCode(Event):
    def __init__(self, parid, prop_id, flag, db_cursor):
        super().__init__(db_cursor=db_cursor, parid=parid, prop_id=prop_id)
        self.category_id = 307
        self.description = (
            f"Parcel {parid}'s {flag.name} changed from {flag.orig} to {flag.new}"
        )
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


def main():
    pass


if __name__ == "__main__":
    main()
