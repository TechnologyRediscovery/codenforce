# instructions for configuring database tables

The files in this directory are used to populate the code enforcement postgres database with enough basic data to run the actual web app. Follow these instructions to properly populate the tables

1. Install postgreSQL database and note the login creditials
2. Using PGAdminIII tool is an easy way to run SQL scripts, so configure a connection in PGADMINIII to talk to your new postgres server
3. Install python3 and the package installer called pip
4. First, run the insert script to populate the municipality table ( tableinserts_munipality_dbv1.sql ) which the python script will need to load the properties (properties have a muni numbver attached to them and so the muni table must have matching rows)
5. Then, with muni table populated, use the python script load_tables_from_csv.py to ingest the CSV file called propTest.csv. NOTE that this python script depends on a few packages, one of which is psycopg2 which is the database connectivity driver for Postgres. It also depends on a custom script called csv_utils.py which is located in this directory
6. With properties inserted into the property table, you can now populate the rest of the tables with tableinserts_dbv1.sql which postgres should ingest happily
7. You now have a working beta version of the database tables that glassfish can interact with nicely.
8. If you run into trouble with the insert scripts, read the postgres output carefully as it will tell you exactly which lines of the insert script are causing errors. You may very well need to tweak a line or two in the sql script in case there is some minor inconsistencies. Use a plain old text editor to make these changes and note them for the team to correct later.
