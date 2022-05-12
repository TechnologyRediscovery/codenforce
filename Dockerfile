#syntax=docker/dockerfile:1
FROM ubuntu:16.04 
WORKDIR /codenforce
#RUN docker exec -ti pwd 
#RUN docker exec -ti ls
CMD sudo docker exec --rm -P --name jl_test codenforce_db  
COPY ./init.sql .
#RUN docker exec run --rm -P --name codenforce_db 
#RUN docker exec ps
#RUN docker run codenforce_db
#RUN psql -h localhost -p 5432 -d cogdb  -U changeme --password changeme
CMD sudo docker exec psql -h localhost -p 5432 -d cogdb  -U changeme --password changeme 
#sudo docker exec -ti jl_test  psql -h localhost -p 5432 -d cogdb  -U changeme --password changeme -c \"$FUN_SQL2\" ??
CMD sudo docker exec ./codenforce/init.sql

