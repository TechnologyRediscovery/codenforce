FROM jboss/wildfly:14.0.1.Final

# server configuration
COPY codeconnect/server/standalone-full-docker.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

# get postgresql plugin
RUN curl https://jdbc.postgresql.org/download/postgresql-42.2.5.jar -o postgresql-42.2.5.jar
RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/
RUN mv postgresql-42.2.5.jar /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/
COPY codeconnect/server/module.xml /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/


EXPOSE 8080
EXPOSE 9990
EXPOSE 8787