# codeconnect
Municipal government code enforcement and occupancy permitting web-app system

## Build
Build system is maven—`mvn compile` should do it.

## Run
Running requires a postgres service and webfly server. This has been automated through docker containers.

Build containers: `docker-compose build`

Run containers: `docker-compose up`

Stop containers: `docker-compose down`

