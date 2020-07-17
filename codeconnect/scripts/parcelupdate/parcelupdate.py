#!/usr/bin/env python3

import math
import time

import click

import fetch
from _update_muni import update_muni
from _db_conn import get_db_and_cursor
from _constants import DASHES


@click.command(context_settings=dict(help_option_names=["-h", "--help"]))
@click.argument(
    "municodes", nargs=-1, default=None,
)
@click.option("-u", nargs=1, default="sylvia")
@click.option("-password", nargs=1, default="c0d3")
@click.option(
    "--commit/--test",
    default=True,
    help="Choose whether to commit to the database or to just run as a test",
)
@click.option("-port", nargs=1, default=5432)
def main(municodes, commit, u, password, port):
    """Updates the CodeNForce database with the most recent data provided by the WPRDC."""
    start = time.time()
    if commit:
        click.echo("Data will be committed to the database")
    else:
        click.echo("This is a test. Data will NOT be committed.")
    click.echo(DASHES)

    # Calls the core functionality for each municipality in the argument
    with get_db_and_cursor(user=u, password=password, port=port) as db_cursor:
        muni_count = 0
        if municodes == ():
            # Update ALL municipalities
            for muni in fetch.get_munis(db_cursor):
                update_muni(muni, db_cursor, commit)
                muni_count += 1

        else:
            for _municode in municodes:
                muni = fetch.get_muniname_from_municode(_municode, db_cursor)
                update_muni(muni, db_cursor, commit)
                muni_count += 1
                print("Updated", muni_count, "municipalities.")
        end = time.time()

        print("Update completed in", math.ceil(end - start), "seconds")


if __name__ == "__main__":
    main()
