#!/usr/bin/env python3

import time
import click
import psycopg2

import _fetch as fetch
from _update_muni import update_muni
from _constants import Tally, DASHES, COG_DB


@click.command(context_settings=dict(help_option_names=["-h", "--help"]))
@click.argument(
    "municodes", nargs=-1, default=None,
)
@click.option("-u", nargs=1, default="sylvia")
@click.option("--password", nargs=1, default="c0d3")
@click.option(
    "--commit/--test",
    default=False,
    help="Choose whether to commit to the database or to just run as a test",
)
@click.option("--port", nargs=1, default=5432)
def main(municodes, commit, u, password, port):
    """Updates the CodeNForce database with the most recent data provided by the WPRDC."""
    start = time.time()
    if commit:
        click.echo("Data will be committed to the database")
    else:
        click.echo("This is a test. Data will NOT be committed.")
    click.echo("Port = {}".format(port))
    click.echo(DASHES)

    try:
        with psycopg2.connect(
            database=COG_DB, user=u, password=password, port=port
        ) as conn:
            with conn.cursor() as cursor:
                if municodes == ():
                    # Update ALL municipalities.
                    municodes = [muni for muni in fetch.munis(cursor)]
                for _municode in municodes:
                    muni = fetch.muniname_from_municode(_municode, cursor)
                    update_muni(muni, conn, commit)
                    Tally.muni_count += 1
                    print("Updated", Tally.muni_count, "municipalities.")
    finally:
        try:
            print("Current muni:", muni.name)
        except NameError:
            pass
        end = time.time()
        print("Total time: {:.0f} seconds".format(end - start))


if __name__ == "__main__":
    main()
