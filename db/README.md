# Docker Database Management

This is a folder that contains utilities and information related to the management of the docker containerized database.

After running the container, the folder `pg-data` will be created, which contains the postgres container's data, allowing it to be persistent between builds/runs. `pg-data` must be deleted to run a fresh database container--this can be done easily using `cleardb.sh`.

The script `loaddb.sh` is provided for loading database backups into the running container automatically. It supports .sql plaintext backups and .tar binary backups.

To make a backup from a repository, the following command works well:
`pg_dump --clean --create --no-owner --no-privileges --encoding UTF-8 --verbose -f cogdb-$(date --iso) cogdb`

`-F tar` can be added optionally for a binary format dump.

These backups should be restored to a completely empty database (delete pg-data and restart the container to empty the database). 

DROP and CREATE errors are normal in the beginning of the run log, I am unsure if it is possible to prevent these.
