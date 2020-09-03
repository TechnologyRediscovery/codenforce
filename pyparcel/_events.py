from typing import NamedTuple, Any, Optional
from colorama import init

import _parse
import _scrape

init()
import warnings
from colorama import Fore, Back, Style
from _constants import BOT_ID


class Changes(NamedTuple):
    """ Help organize the creation of events representing a change.

    See query_propertyexternaldata_for_changes_and_write_events
    """

    description: str
    orig: Any
    new: Any


class EventDetails:
    __slots__ = ["parid", "prop_id", "cecase_id", "old", "new", "db_cursor"]

    def __init__(self, parid, prop_id, cecase_id, db_cursor):
        self.parid = parid
        self.prop_id = prop_id
        self.cecase_id = cecase_id
        self.db_cursor = db_cursor  # Although it technically isn't an event detail, passing the db_cursor makes life easier
        self.old = None
        self.new = None

    def unpack(self, old, new):
        """ Sets the EventDetails old and new attributes to the given values.
        """
        self.old = old
        self.new = new


def query_propertyexternaldata_for_changes_and_write_events(
    parid, prop_id, cecase_id, new_parcel, db_cursor
):
    """ Checks if parcel info is different from last time. Records Changes. """
    select_sql = """
        SELECT
            ownername, address_street, address_citystatezip,
            livingarea, condition
        FROM public.propertyexternaldata
        WHERE property_propertyid = %(prop_id)s
        ORDER BY lastupdated DESC
        LIMIT 2;
    """
    details = EventDetails(parid, prop_id, cecase_id, db_cursor)
    db_cursor.execute(select_sql, {"prop_id": prop_id})
    selection = db_cursor.fetchall()
    try:
        old = selection[1]
    except IndexError:  # If this is the first time the property_propertyid occurs in propertyexternaldata
        if not new_parcel:
            # TODO: Add flag
            print(
                Fore.YELLOW,
                "Error: Parcel appeared in public.propertyexternaldata for the first time even though the parcel ID is flagged as appearing in public.property before.",
                Style.RESET_ALL,
                sep="",
            )
            return
        # If it IS a new parcel id
        print(
            Fore.YELLOW,
            "First time parcel has appeared in propertyexternaldata",
            Style.RESET_ALL,
            sep="",
        )
        NewParcelid(details).write_to_db()
        return
    new = selection[0]

    # details.unpack sets details.old and details.new
    if old[0] != new[0]:  # Todo: Clean name
        details.unpack(old[0], new[0])
        DifferentOwner(details).write_to_db()
    if old[1] != new[1]:
        details.unpack(old[1], new[1])
        DifferentStreet(details).write_to_db()
    if old[2] != new[2]:
        details.unpack(old[2], new[2])
        DifferentCityStateZip(details).write_to_db()
    if old[3] != new[3]:
        details.unpack(old[3], new[3])
        DifferentLivingArea(details).write_to_db()
    if old[4] != new[4]:
        details.unpack(old[4], new[4])
        DifferentCondition(details).write_to_db()
    # # # Todo: Since taxcodes are no longer are related to property, begin deprecating
    # if old[5] != new[5]:
    #     # Todo: Find pythonic way to put this in front of every column
    #     if old[5] is not None:  # The old value will likely only be None due to an API / Script change,
    #                             # opposed to a change in the actual change in tax status.
    #         details.unpack(old[5], new[5])
    #         DifferentTaxCode(details).write_to_db()

    if details.new:
        return True


# For simplicity sake, EVENTS ARE CURRENTLY ONLY FOR PROPERTY INFO CASES
# THE EVENT CLASS REQUIRES AN OVERHAUL FOR USE WITH PERSON INFO CASES
class Event:
    """ Base class for all events. """

    # Returned after writing event to database
    event_id: int

    # To be filled in by subclasses
    category_id: int
    eventdescription: str
    active: bool
    notes: Optional[str]
    occperiod: Optional[str]

    def __init__(self, details: EventDetails):
        self.prop_id = details.prop_id
        self.parid = details.parid
        self.cecase_id = details.cecase_id
        self.db_cursor = details.db_cursor

    def write_to_db(self):
        """ Writes an event to the database. """
        self._write_event_dunder_dict()
        self.event_id = self._write_event_to_db()  # uses self.ce_caseid
        print(Fore.RED, self.eventdescription, Style.RESET_ALL, sep="")
        if self.notes:
            print(Fore.RED, self.notes, Style.RESET_ALL, sep="")

    def _write_event_dunder_dict(self):
        assert self.category_id
        assert self.eventdescription
        assert self.active

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
                %(eventdescription)s, %(creator_userid)s, %(active)s, %(notes)s,
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
        self.eventdescription = "Parcel {} was added.".format(self.parid)
        self.active = True
        self.notes = None  # Todo: Add notes
        self.occ_period = None


class ParcelChangedEvent(Event):
    brief_description: str  # Used in self.eventdescription

    def __init__(self, d: EventDetails):
        super().__init__(d)
        self.eventdescription = f"Parcel {d.parid}'s {self.brief_description} changed from {d.old} to {d.new}"
        self.active = True
        self.notes = None  # Todo: Add notes
        self.occ_period = None


class DifferentOwner(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 301
        self.brief_description = "owner name"
        super().__init__(details)


class DifferentStreet(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 302
        self.brief_description = "street"
        super().__init__(details)


class DifferentCityStateZip(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 303
        self.brief_description = "city, state, or zipcode"
        super().__init__(details)


class DifferentLivingArea(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 304
        self.brief_description = "living area size"
        super().__init__(details)


class DifferentCondition(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 305
        self.brief_description = "condition"
        super().__init__(details)


class DifferentTaxStatus(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 306
        self.brief_description = "tax status"
        super().__init__(details)


class DifferentTaxCode(ParcelChangedEvent):
    def __init__(self, details):
        self.category_id = 307
        self.brief_description = "tax code"
        super().__init__(details)
        warnings.warn(
            "DifferentTaxCode may be deprecated soon, as TaxCode is no longer an attribute on the property table",
            DeprecationWarning,
        )


class ParcelNotInWprdcData(Event):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 308
        self.eventdescription = "Parcel {} was in the CodeNForce database but not in WPRDC dataset.".format(
            self.parid
        )
        self.active = True
        self.notes = self.write_notes()
        self.occ_period = None

    # Todo: Rename method
    def write_notes(self) -> str:
        """
        Returns:
            A message detailing if the Allegheny County Real Estate Portal url points to a parcel in their database.
        """
        response = _scrape.county_property_assessment(self.parid, full_response=True)
        if _parse.validate_county_response(response.text):
            msg = "County link:"
        else:
            msg = "Broken link:"
        return msg + response.url


def main():
    pass


if __name__ == "__main__":
    main()
