import re
from typing import NamedTuple, Optional

from sqlalchemy import text, String, bindparam
from sqlalchemy.engine import CursorResult

from _db import db


class Address(NamedTuple):
    # Should this have autocorrect to change P.O. BOX to PO BOX?
    # Should this auto-capitalize everything?
    building: str
    street: str
    city: str
    state: str
    zip: str

    def to_string(self):
        return f"{self.building} {self.street}, {self.city}, {self.state} {self.zip}"

    # def __str__(self):
    #     return self.to_string()


address_regex = re.compile(
    r"(.*\d+)\s"  # Group 1 capture:
    #   Description:    Everything up to the space where the building number's number is
    #   Assumptions:    Each address has a building number
    #                   There aren't any places like 1-40 Example Ave
    #   Example:        "PO Box 1234",
    r"(.*),\s"  # Group 2 capture:
    #   Description: The street's name
    #   Assumptions: The streets Name has no commas
    r"(.*),\s"  # Group 3 capture:
    #   Description: The city's name
    r"(\w\w)\s"  # Group 4 capture:
    #   Description:    The state
    #   Assumption:     2 letters
    r"(.*)\s*"  # Group 5 capture
    #   Description:    The zip code
    #   Assumption:     Everything else is the zip code
)


def address_from_string(address: str) -> Address:
    """
    >>> address_from_string("PO Box 1234 Example Ave, Cityname, PA 16342")
    Address(building='PO Box 1234', street='Example Ave', city='Cityname', state='PA', zip='16342')
    """
    m = re.match(pattern=address_regex, string=address)
    return Address(*m.groups())


def building_pk_to_address(pk: int) -> Address:
    with db.begin() as conn:
        x = conn.execute(
            text(
                """
                SELECT address.bldgno, street.name, csz.city, csz.state_abbr, csz.zip_code
                    FROM mailingaddress address
                    JOIN mailingstreet street ON (address.street_streetid = street.streetid)
                    JOIN mailingcitystatezip csz ON (street.citystatezip_cszipid = csz.id)
                    WHERE address.addressid = :pk;"""
            ).bindparams(bindparam("pk", type_=String)),
            {"pk": pk},
        ).fetchone()
    return Address(*x)


def query_address(address: str) -> Optional[int]:
    """
    Returns:
        The primary key of the building number, which represents a structure on a street
        None if the address was not found in the database
    """


def ensure_address(address: str) -> int:
    """
    Returns:
        The primary key of the building number, which represents a structure on a street
    """


def query_street(address: str) -> Optional[int]:
    """
    Returns:
        The primary key representing the street
        None if the street was not found in the database
    """


def write_street(db, street_name, zipcode):
    """
    Returns:
        The primary key representing the street
    """


def write_building(db, building_num, street_pk):
    """of
    Returns:
        The primary key representing the building number,
    """
