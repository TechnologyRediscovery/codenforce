import psycopg2
import click


func_list = [

    """
        INSERT INTO property(
            a, b, c, d
        )
        VALUES(
            1, 2, 3, 4
        );
    """,

    """
        INSERT INTO person(
            a, b, c, d
        )
        VALUES(
            1, 2, 3, 4
        );
    """,


    """
        INSERT INTO taxstatus(
            a, b, c, d
        )
        VALUES(
            1, 2, 3, 4
        );
    """,
]


@click.option("--port", nargs=1, default=5432)
def main(port):
    with psycopg2.connect(port=port) as conn:
        with conn.cursor() as cursor:
            [cursor.execute(sql) for sql in func_list]


if __name__ == "__main__":
    main()
