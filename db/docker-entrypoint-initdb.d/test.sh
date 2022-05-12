#!/bin/bash
db_name='cogdb'
db_user='changeme'

function execute_sql() {
psql --tuples-only -U $db_user -d $db_name -c "$@"
}

cat provision-init.sql | docker exec -i bohoo.postgres bash -c 'psql -U $[POSTGRES_USER} -w -a -q -f -'
