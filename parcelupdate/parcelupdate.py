#!/usr/bin/env python3

import math
import time
import click
import _fetch as fetch
from _connection import connection_and_cursor
from _update_muni import update_muni
from _constants import Tally
from _constants import DASHES, COG_DB


@click.command(context_settings=dict(help_option_names=["-h", "--help"]))
@click.argument(
    "municodes", nargs=-1, default=None,
)
@click.option("-u", nargs=1, default="sylvia")
@click.option("--password", nargs=1, default="c0d3")
@click.option(
    "--commit/--test",
    default=True,
    help="Choose whether to commit to the database or to just run as a test",
)
@click.option("--port", nargs=1, default=5432)
def main(municodes, commit, u, password, port):
    """Updates the CodeNForce database with the most recent data provided by the WPRDC."""

    commit = False

    start = time.time()
    if commit:
        click.echo("Data will be committed to the database")
    else:
        click.echo("This is a test. Data will NOT be committed.")
    click.echo(DASHES)

    with connection_and_cursor(database=COG_DB, user=u, password=password, port=port) as (conn, cursor):
        if municodes == ():
            # Update ALL municipalities.
            municodes = [muni for muni in fetch.munis(cursor)]
        for _municode in municodes:
            muni = fetch.muniname_from_municode(_municode, cursor)
            update_muni(muni, conn, commit)
            Tally.muni_count += 1
            print("Updated", Tally.muni_count, "municipalities.")
        end = time.time()
        print("Update completed in", math.ceil(end - start), "seconds")


if __name__ == "__main__":
    main()
