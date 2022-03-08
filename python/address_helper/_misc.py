# Database access
############
from typing import Optional


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
