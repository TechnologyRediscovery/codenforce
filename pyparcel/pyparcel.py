#!/usr/bin/env python3
import time
from datetime import timedelta

import click
import psycopg2

import _fetch as fetch
from _update_muni import (
    create_events_for_parcels_in_db_but_not_in_records,
    download_and_read_records_from_Wprdc,
    update_database,
)
from common import Tally, DASHES, COG_DB


@click.command(context_settings=dict(help_option_names=["-h", "--help"]))
@click.argument(
    "municodes", nargs=-1, default=None,
)
@click.option("-u", nargs=1, default="sylvia")
@click.option("--password", nargs=1, default="c0d3")
@click.option("--port", nargs=1, default=5432)
@click.option(
    "--main/--skip-main",  # Todo: Find more descriptive name
    default=True,
    help="Choose whether to run the main functionality of the script "
    "(Adding new properties and updating propertyexternaldata based on the latest information from the WPRDC.)",
)
@click.option(
    "--diff/--skip-diff",
    default=True,
    help="Choose whether to check for parcels in our database that aren't in the WPRDC records",
)
@click.option(
    "--commit/--test",
    default=False,
    help="Choose whether to commit to the database or to just run as a test",
)
def main(municodes, u, password, port, main, diff, commit):
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
                    records = download_and_read_records_from_Wprdc(muni)

                    # Skip muni if the records are invalid (for example, for the test muni COG Land),
                    if not records:
                        click.echo("Skipping {}: Invalid JSON".format(muni.name))
                        click.echo(DASHES)
                        continue

                    if main:
                        for record in records:
                            update_database(record, conn, cursor, commit)
                        click.echo(DASHES)

                    if diff:
                        create_events_for_parcels_in_db_but_not_in_records(
                            records, muni.municode, conn, cursor, commit
                        )
                        click.echo(DASHES)

                    Tally.muni_count += 1
                    click.echo("Updated {} municipalities.".format(Tally.muni_count))
                    click.echo(DASHES)

    finally:
        try:
            click.echo("Current muni {}:".format(muni.name))
        except NameError:
            pass
        end = time.time()
        click.echo(
            "Total time: {}".format(
                # Strips milliseconds from elapsed time
                str(timedelta(seconds=(end - start))).split(".")[0]
            )
        )


if __name__ == "__main__":
    main()
