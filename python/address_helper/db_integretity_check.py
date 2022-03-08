from sqlalchemy import text
from sqlalchemy.engine import CursorResult

from _db import db


def check_duplicate_addresses() -> list[tuple[str, int, list[int]]]:
    """
    Returns a list of tuples.
    Each tuple contains information regarding duplicated information.
    tuple[0] -> The duplicated building number
    tuple[1] -> The duplicated street's PK
    tuple[2] -> A list of the duplicated mailing-address primary key's
    """
    with db.begin() as conn:
        duplicates = conn.execute(
            text(
                """
                SELECT bldgno, street_streetid, array_agg(addressid)
                FROM mailingaddress
                group by bldgno, street_streetid
                HAVING COUNT(*) > 1
            """
            )
        ).fetchall()
    return duplicates


def duplicate_address_info(dups):
    print(
        f"NUMBER OF UNIQUE DUPLICATIONS:\t{len(dups)}\n"
        f"NUMBER OF TOTAL DUPLICATIONS:\t{sum([len(x[2]) for x in dups])}"
    )


if __name__ == "__main__":
    d = check_duplicate_addresses()
    duplicate_address_info(d)
