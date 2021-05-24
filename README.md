# codeconnect
Municipal government code enforcement and occupancy permitting web-app system


## Build
### Requirements:
Java 8 w/ Java FX and Java EE

Build system is maven—`mvn compile` should do it.

## Run
Running requires a postgres service and webfly server setup. This has been sufficiently automated through docker containers.

Build containers: `docker-compose build`

Run containers: `docker-compose up`

Stop containers: `docker-compose down`

To update the database in the container, copy a .sql file into the db folder.

