FROM postgres:9

ENV POSTGRES_DB=cogdb
ENV POSTGRES_USER=changeme
ENV POSTGRES_PASSWORD=changeme

# Copy sql starters
ADD * /docker-entrypoint-initdb.d/


EXPOSE 5432