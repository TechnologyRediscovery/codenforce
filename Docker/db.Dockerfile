#syntax=docker/dockerfile:1
FROM:codenforce_db-present-repo
COPY init.sql /docker-entrypoint-initdb.d/
#docker run --rm -P --name codenforce_db
#sudo docker ps
#sudo docker run codenforce_db
RUN sudo psql -h localhost -p 5432 -d cogdb  
# Create Trigger to keep track of the audits and operations performed
#Create the Patch audit table
sudo docker-compose down

