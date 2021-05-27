# codeconnect
Municipal government code enforcement and occupancy permitting web-app system


## Build
### Requirements:
- Java 8 w/ Java FX and Java EE

Build system is maven—`mvn package` should do it.

## Run
Running requires a postgres service and webfly server setup. This has been sufficiently automated through docker containers.

- Build containers: `docker-compose build`

- Run containers: `docker-compose up`

- Stop containers: `docker-compose down`

Instructions on initializing database contents can be found in `db/README.md`

After the containers are running (using `docker-compose up && docker-compose down`), `mvn package` can be run and the website will be updated without a server restart.
