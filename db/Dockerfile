FROM postgis/postgis:9.5-2.5


ENV POSTGRES_DB=cogdb
ENV POSTGRES_USER=changeme
ENV POSTGRES_PASSWORD=changeme
ENV PGDATA=/pg-data

RUN mkdir /pg-data && chmod 777 /pg-data

EXPOSE 5432