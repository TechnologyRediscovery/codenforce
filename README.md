# codNforce
Municipal government code enforcement and occupancy permitting web-app system; publicly funded, and maybe even citizen directed


## Build
### Requirements:
- Java 8 w/ Java FX and Java EE

Build system is maven—`mvn package` should do it.

## Run
Running requires a postgres service and webfly server setup. This has been sufficiently automated through docker containers.
0. Stop your local postgresql with `sudo service postgresql stop`
1. Start by navigating your console into the location where the Dockerfile and docker-compose.yml lives. This is probably the directory of the repository you cloned the repostiroy into. Once you are in the directory, you should be able to say `ls` and see `docker-compose.yml` and `Dockerfile`

- Build containers: `docker-compose build` You may need to have super powers with `sudo docker-compose build`

- Run containers: `docker-compose up`You may need to have super powers with `sudo docker-compose up`

- Stop containers: `docker-compose down`

Instructions on initializing database contents can be found in `db/README.md`

After the containers are running (using `docker-compose up && docker-compose down`), `mvn package` can be run and the website will be updated without a server restart.
