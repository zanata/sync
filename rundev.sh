#!/usr/bin/env bash
# Author Patrick Huang <pahuang@redhat.com>

# volume mapping for sync deployment files
SYNC_DEPLOYMENT_DIR=$HOME/docker-volumes/sync-deployments
# create the directory and set permissions (SELinux)
mkdir -p ${SYNC_DEPLOYMENT_DIR} && chcon -Rt svirt_sandbox_file_t "$SYNC_DEPLOYMENT_DIR"

CONFIG_WAR=$(find . -type f -name "*config*.war")
JOBS_WAR=$(find . -type f -name "*jobs*.war")

set -x

if [ -z ${CONFIG_WAR} -a -e ${CONFIG_WAR} ]
then
    echo ">>>> copy existing config war: $(ls -ltr ${CONFIG_WAR})"
    cp ${CONFIG_WAR} ${SYNC_DEPLOYMENT_DIR}/sync.war
else
    mvn clean package -DskipTests -pl sync-config-war -am
    cp $(find . -type f -name "*config*.war") ${SYNC_DEPLOYMENT_DIR}/sync.war
fi

if [ -z ${JOBS_WAR} -a -e ${JOBS_WAR} ]
then
    echo ">>>> copy existing jobs war: $(ls -ltr ${JOBS_WAR})"
    cp ${JOBS_WAR} ${SYNC_DEPLOYMENT_DIR}/jobs.war
else
    mvn clean package -DskipTests -pl jobs-war -am
    cp $(find . -type f -name "*jobs*.war") ${SYNC_DEPLOYMENT_DIR}/jobs.war
fi

JBOSS_DEPLOYMENT_VOLUME=/opt/jboss/wildfly/standalone/deployments

docker run --rm -p 8080:8080 --link postgres-db:db --name sync-dev \
    -v ${SYNC_DEPLOYMENT_DIR}:${JBOSS_DEPLOYMENT_VOLUME} \
    zanata/sync
