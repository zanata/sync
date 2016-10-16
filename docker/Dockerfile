FROM jboss/wildfly:10.1.0.Final
MAINTAINER Patrick Huang <pahuang@redhat.com>

EXPOSE 8080

ENV POSTGRES_MODULE_DIR=/opt/jboss/wildfly/modules/org/postgresql/jdbc/main CUSTOMIZATION_DIR=/opt/jboss/wildfly/customization/ \
    DB_HOSTNAME=syncdb DB_USERNAME=postgres DB_PASSWORD=postgres DB_NAME=sync_local

# Wildfly modules
RUN curl --remote-name --location --silent https://dl.bintray.com/sflanigan/generic/wildfly-module-hibernate-main-4.2.20.Final.zip \
    && unzip -q wildfly-module-hibernate-main-4.2.20.Final.zip -d /opt/jboss/wildfly \
    && rm -rf wildfly-module-hibernate-main-4.2.20.Final.zip \
    && mkdir -p $POSTGRES_MODULE_DIR \
    && curl --location --silent --output $POSTGRES_MODULE_DIR/postgresql-jdbc3.jar \
    "http://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/9.2-1004-jdbc3/postgresql-9.2-1004-jdbc3.jar"

# add the postgresql module file
COPY modules/org/postgresql/jdbc/main/module.xml $POSTGRES_MODULE_DIR

# add customization scripts
COPY customization/ $CUSTOMIZATION_DIR
# run customization script (the copied files are owned by root https://github.com/docker/docker/issues/6119)
USER root
RUN chown -R jboss:jboss $CUSTOMIZATION_DIR
USER jboss
RUN $CUSTOMIZATION_DIR/change-config.sh

VOLUME /opt/jboss/wildfly/standalone/deployments/

# for override default java security store
VOLUME /etc/pki/ca-trust/extracted/
