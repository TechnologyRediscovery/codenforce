from dataclasses import dataclass
from typing import NamedTuple, Any, Optional, List
from colorama import init

import _parse
import _scrape

init()
import warnings
from colorama import Fore, Back, Style
from common import BOT_ID


# Todo: Weigh benefits of making a dataclass
@dataclass
class EventDetails:
    parid: str
    prop_id: int
    cecase_id: int
    # Todo: Cursor typing
    db_cursor: Any  # Although it technically isn't an event detail, passing the db_cursor makes life easier

    old: Optional[Any] = None
    new: Optional[Any] = None

    # Todo: Make Pythonic. Should these be here or should they be created dynamically?
    #   The majority of Events do not require them.
    url: Optional[str] = None
    muniname: Optional[str] = None

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
                "Error: Parcel {} appeared in public.propertyexternaldata for the first time "
                "even though the parcel ID is flagged as appearing in public.property before.\n".format(
                    parid
                ),
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

    # TODO: propertyexternaldata does not currently track municode
    #   Add a write to DifferentMunicode in the case that the column is added.

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
        return self.db_cursor.fetchone()[0]  # eventid


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
        self.eventdescription = f"Parcel {d.parid}'s {self.brief_description} should be changed from {d.old} to {d.new}"
        self.active = True
        self.notes = None  # Todo: Add notes
        self.occ_period = None


class DifferentOwner(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "owner name"
        super().__init__(details)
        self.category_id = 301


class DifferentStreet(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "street"
        super().__init__(details)
        self.category_id = 302


class DifferentCityStateZip(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "city, state, or zipcode"
        super().__init__(details)
        self.category_id = 303


class DifferentLivingArea(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "living area size"
        super().__init__(details)
        self.category_id = 304


class DifferentCondition(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "condition"
        super().__init__(details)
        self.category_id = 305


class DifferentTaxStatus(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "tax status"
        super().__init__(details)
        self.category_id = 306


class DifferentTaxCode(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "tax code"
        super().__init__(details)
        self.category_id = 307
        warnings.warn(
            "DifferentTaxCode may be deprecated soon, as TaxCode is no longer an attribute on the property table",
            DeprecationWarning,
        )


def parcel_not_in_wprdc_data(details: EventDetails) -> Event:
    """
    Factory function to be called when a parcel is in our database but not the WPRDC data.

    The function assumes the parcel did not appear in the WPRDC data.
    There is no safeguard to check for this, so be dilligent with your code.
    Only call this function if the parcel did not appear in the WPRDC data.

    Returns:
        An Event indicating whether the Allegheny County Real Estate Portal for the parcel id points to a real page.
            If the page exists: DifferentMunicode
            Else: NotInRealEstatePortal
    """
    response = _scrape.county_property_assessment(details.parid, full_response=True)
    details.url = response.url

    raw_muni = _parse.validate_county_municode_against_portal(response.text)
    if raw_muni:
        details.new, details.muniname = _parse.Municipality.from_raw(raw_muni)
        return DifferentMunicode(details)
    return NotInRealEstatePortal(details)


class DifferentMunicode(ParcelChangedEvent):
    def __init__(self, details):
        self.brief_description = "municode"
        super().__init__(details)
        self.category_id = 308
        self.notes = details.url
        self._extend_eventdescription(details.muniname)
        # Example: ... from 821 to 930 (North Versailles)

    def _extend_eventdescription(self, muniname):
        """
        Example: ... from 821 to 930 (North Versailles)

            Operations within an Event __init__ (such as concatenating a string)
            are made into methods. This makes it easy to automate tests.
            See the ReadMe for more detail.
        """
        self.eventdescription += "".join([" (", muniname, ")"])


class NotInRealEstatePortal(Event):
    def __init__(self, details):
        super().__init__(details)
        self.category_id = 309
        self.eventdescription = (
            "Parcel {} was not found in the "
            "Allegheny County Real Estate Portal.".format(self.parid)
        )
        self.active = True
        self.occ_period = None
        self.notes = details.url


def main():
    pass


if __name__ == "__main__":
    main()
