import pytest
from sqlalchemy import text

from _db import db
from __init__ import building_pk_to_address


def test_building_pk_to_address():
    with db.begin() as conn:
        cr = conn.execute(text("SELECT addressid from mailingaddress"))
    pks = [r[0] for r in cr]
    return [building_pk_to_address(pk) for pk in pks]


if __name__ == "__main__":
    addrs = test_building_pk_to_address()
    for addr in addrs:
        print(f"{addr[0]} {addr[1]}, {addr[2]}")
