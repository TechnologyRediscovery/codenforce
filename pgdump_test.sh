#!/bin/bash
/usr/bin/pg_dump --host localhost --port 5432 --username "changeme"  --format plain --section pre-data --section post-data --encoding UTF8 --verbose --file "/home/techred4/codenforce/search/schematestjill.sql" "cogdb" 
