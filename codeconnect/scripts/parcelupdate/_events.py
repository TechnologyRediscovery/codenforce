from collections import namedtuple
from colorama import init
init()
from colorama import Fore, Back, Style
from _constants import BOT_ID


# These structures help organize the creation of events. See query_propertyexternaldata_for_changes_and_write_events
Changes = namedtuple("flag", ["name", "orig", "new"])
class EventDetails:
    __slots__ = ["parid", "prop_id", "cecase_id", "changes", "db_cursor"]
    def __init__(self, parid, prop_id, cecase_id, db_cursor):
        self.parid = parid
        self.prop_id = prop_id
        self.cecase_id = cecase_id
        self.db_cursor = db_cursor  # Although it technically isn't an event detail, passing the db_cursor makes life easier
        self.changes = None


def query_propertyexternaldata_for_changes_and_write_events(parid, prop_id, cecase_id, new_parcel, db_cursor):
    """ Checks if parcel info is different from last time. Records Changes. """
    select_sql = """
        SELECT
            property_propertyid, ownername, address_street, address_citystatezip,
            livingarea, condition, taxcode
        FROM public.propertyexternaldata
        WHERE property_propertyid = %(prop_id)s
        ORDER BY lastupdated DESC
        LIMIT 2;
    """
    details = EventDetails(parid, prop_id, cecase_id, db_cursor)
    db_cursor.execute(select_sql, {"prop_id": prop_id})
    selection = db_cursor.fetchall()
    old = selection[0]
    try:
        new = selection[1]
    except IndexError:  # If this is the first time the property_propertyid occurs in propertyexternaldata
        print(Fore.YELLOW, "First time parcel has appeared in propertyexternaldata", sep="")
        print(Style.RESET_ALL, end="")
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
        details.changes = Changes("tax code", old[5], new[5])
        DifferentTaxCode(details).write_to_db()



# For simplicity sake, EVENTS ARE CURRENTLY ONLY FOR PROPERTY INFO CASES
# THE EVENT CLASS REQUIRES AN OVERHAUL FOR USE WITH PERSON INFO CASES
class Event:
    """ Abstract base class for all events. """

    def __init__(self, details):
        self.prop_id = details.prop_id
        self.parid = details.parid
        self.cecase_id = details.cecase_id
        self.db_cursor = details.db_cursor

        # Returned after writing event to database
        self.event_id = None

        # To be filled in by subclasses
        self.category_id = None
        self.description = None
        self.active = None
        self.notes = None
        self.occperiod = None


    def write_to_db(self):
        """ Writes an event to the database. """
        self._write_event_dunder_dict()
        self.event_id = self._write_event_to_db()  # uses self.ce_caseid
        print(Fore.RED, self.description, sep="")
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


    def _write_event_dunder_dict(self):
        assert self.category_id
        assert self.description
        assert self.active
        assert self.notes

        self.cecase_caseid = self.cecase_id
        self.creator_userid = BOT_ID
        self.lastupdatedby_userid = BOT_ID
        self.occperiod_periodid = None


    def _write_event_to_db(self):
        insert_sql = """
            INSERT INTO event(
                eventid, category_catid, cecase_caseid, creationts,
                eventdescription, creator_userid, active, notes,
                occperiod_periodid, timestart, timeend, lastupdatedby_userid,
                lastupdatedts
            )
            VALUES(
                DEFAULT, %(category_id)s, %(cecase_caseid)s, now(),
                %(description)s, %(creator_userid)s, %(active)s, %(notes)s,
                %(occ_period)s, now(), now(), %(lastupdatedby_userid)s,
                now()
            )
            RETURNING eventid;"""
        self.db_cursor.execute(insert_sql, self.__dict__)
        return self.db_cursor.fetchone()[0]


class NewParcelid(Event):
    """ Indicates a parcel id was added to the database that wasn't in the database before. """

    def __init__(self, details):
        super().__init__(details)
        self.category_id = 300
        self.description = "Parcel {} was added.".format(self.parid)
        self.active = True
        self.ce_notes = " "
        self.notes = " "
        self.occ_period = None


class ParcelChangedEvent(Event):
    def __init__(self, d):  # details
        super().__init__(d)
        self.eventdescription = f"Parcel {d.parid}'s {d.changes.name} changed from {d.orig} to {d.new}"
        self.active = True
        self.ce_notes = " "
        self.event_notes = " "
        self.occ_period = None


class DifferentOwner(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 301


class DifferentStreet(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 302


class DifferentCityStateZip(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 303


class DifferentLivingArea(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 304


class DifferentCondition(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 305


class DifferentTaxStatus(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 306


class DifferentTaxCode(ParcelChangedEvent):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 307


def main():
    pass


if __name__ == "__main__":
    main()
