#!/bin/bash
# This script will restore binary backups with the .bak file extension and .sql text-based backups.
# Run after starting the container, and the data should be persistent between restarts.

container=codenforce_db_1
dbname=cogdb
username=changeme


find . -maxdepth 1 -type f -name '*.tar' -exec bash -c "docker exec -i $container pg_restore < {}" \;

find . -maxdepth 1 -type f -name '*.sql' -exec bash -c "docker exec -i $container psql -U $username -d $dbname < {}" \;
