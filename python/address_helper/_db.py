from sqlalchemy.future import create_engine as __create_engine
from sqlalchemy.future.engine import Engine

from typing import Optional



# Database setup
############
def _create_engine() -> Engine:
    db_user = "sylvia"
    db_password = "temppass"
    host = "localhost"
    port = "5432"
    db_name = "cogdb"

    engine_params = (
        f"postgresql+psycopg2://{db_user}:{db_password}@{host}:{port}/{db_name}"
    )
    return __create_engine(engine_params)


db = _create_engine()


# def get_db():
#     # Dependency injection? Yield connection? hmm
#     return _engine


# Database access
############
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
